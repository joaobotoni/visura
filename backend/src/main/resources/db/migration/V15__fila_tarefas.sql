-- Fila de tarefas em segundo plano (transcrição de áudio, análise de foto e de vídeo pela IA, geração de laudo).
-- Fica no próprio PostgreSQL: a reserva usa "for update skip locked", então várias instâncias da API dividem a fila
-- sem pegar a mesma tarefa, e não entra infraestrutura nova (Redis, Kafka).
--
-- Ciclo: pendente -> executando -> concluida; numa falha volta a pendente com espera crescente, até esgotar as
-- tentativas (falhou). Uma tarefa "executando" além do prazo (a instância caiu no meio) é reservada de novo. Por isso
-- quem processa precisa ser idempotente: a mesma tarefa pode rodar mais de uma vez.
-- A aplicação não apaga linhas: a tarefa concluída fica como histórico.

create schema processamento;

create type processamento.status_tarefa as enum ('pendente', 'executando', 'concluida', 'falhou');

create table processamento.tarefa
(
    id_tarefa         int generated always as identity,
    uuid              uuid                         not null default gen_random_uuid(),
    tipo              text                         not null,
    referencia        uuid                         not null,
    status            processamento.status_tarefa  not null default 'pendente',
    tentativas        int                          not null default 0,
    maximo_tentativas int                          not null default 5,
    disponivel_em     timestamptz                  not null default now(),
    iniciada_em       timestamptz,
    concluida_em      timestamptz,
    erro              text,
    created_at        timestamptz                  not null default now(),
    updated_at        timestamptz                  not null default now(),
    constraint pk_tarefa primary key (id_tarefa),
    constraint uq_tarefa_uuid unique (uuid),
    constraint ck_tarefa_tipo check (tipo ~ '^[a-z][a-z0-9_]{0,49}$'),
    constraint ck_tarefa_tentativas check (tentativas >= 0 and maximo_tentativas between 1 and 20),
    constraint ck_tarefa_executando check ((status = 'executando') = (iniciada_em is not null)),
    constraint ck_tarefa_concluida check ((status = 'concluida') = (concluida_em is not null)),
    constraint ck_tarefa_erro check (erro is null or length(erro) <= 2000)
);

-- Só as tarefas ainda vivas entram no índice da fila.
create index ix_tarefa_fila on processamento.tarefa (disponivel_em) where status in ('pendente', 'executando');
create index ix_tarefa_referencia on processamento.tarefa (referencia);

create trigger tg_tarefa_updated_at
    before update
    on processamento.tarefa
    for each row
execute function public.tg_set_updated_at();

-- Põe uma tarefa na fila e devolve o uuid dela.
create function processamento.enfileirar_tarefa(
    p_tipo text,
    p_referencia uuid
)
    returns uuid
    language sql
as
$$
insert into processamento.tarefa (tipo, referencia)
values (p_tipo, p_referencia)
returning uuid;
$$;

-- Reserva a próxima tarefa disponível (a mais antiga), ou nenhuma. Uma tarefa "executando" há mais que p_prazo
-- segundos é considerada abandonada: volta para a fila se ainda tem tentativas, ou falha.
create function processamento.reservar_tarefa(
    p_prazo int
)
    returns setof processamento.tarefa
    language plpgsql
as
$$
begin
    update processamento.tarefa
    set status      = 'falhou',
        iniciada_em = null,
        erro        = 'Prazo de execução esgotado em todas as tentativas.'
    where status = 'executando'
      and iniciada_em < now() - make_interval(secs => p_prazo)
      and tentativas >= maximo_tentativas;

    return query
        update processamento.tarefa t
            set status = 'executando',
                tentativas = t.tentativas + 1,
                iniciada_em = now()
            where t.id_tarefa = (select p.id_tarefa
                                 from processamento.tarefa p
                                 where (p.status = 'pendente' and p.disponivel_em <= now())
                                    or (p.status = 'executando' and p.iniciada_em < now() - make_interval(secs => p_prazo))
                                 order by p.disponivel_em, p.id_tarefa
                                 limit 1 for update skip locked)
            returning t.*;
end;
$$;

-- Encerra a tarefa reservada com sucesso.
create procedure processamento.concluir_tarefa(
    p_id_tarefa int
)
    language sql
as
$$
update processamento.tarefa
set status       = 'concluida',
    iniciada_em  = null,
    concluida_em = now(),
    erro         = null
where id_tarefa = p_id_tarefa
  and status = 'executando';
$$;

-- Devolve a tarefa reservada para a fila depois de p_espera segundos, guardando o erro; sem tentativas restantes, ela falha.
create procedure processamento.adiar_tarefa(
    p_id_tarefa int,
    p_erro text,
    p_espera int
)
    language sql
as
$$
update processamento.tarefa
set status        = case when tentativas >= maximo_tentativas then 'falhou' else 'pendente' end::processamento.status_tarefa,
    iniciada_em   = null,
    disponivel_em = now() + make_interval(secs => p_espera),
    erro          = left(p_erro, 2000)
where id_tarefa = p_id_tarefa
  and status = 'executando';
$$;
