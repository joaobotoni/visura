-- =====================================================================================================================
-- Recuperação de senha: um código de 6 dígitos enviado por e-mail. Só o HMAC do código é gravado. Cada usuário tem no
-- máximo uma recuperação aberta; pedir um código novo encerra o anterior. O código vence em 15 minutos e aceita poucas
-- tentativas (contadas pela aplicação antes de conferir).
-- =====================================================================================================================
create table usuarios.recuperacao_senha
(
    id_recuperacao int generated always as identity,
    id_usuario     int         not null,
    codigo_hash    bytea       not null,
    tentativas     int         not null default 0,
    expira_em      timestamptz not null,
    encerrada_em   timestamptz,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    constraint pk_recuperacao_senha primary key (id_recuperacao),
    constraint fk_recuperacao_senha_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint ck_recuperacao_senha_tentativas check (tentativas >= 0),
    constraint ck_recuperacao_senha_expira_em check (expira_em > created_at),
    constraint ck_recuperacao_senha_encerrada_em check (encerrada_em is null or encerrada_em >= created_at)
);

create unique index uq_recuperacao_senha_aberta on usuarios.recuperacao_senha (id_usuario) where encerrada_em is null;

create trigger tg_recuperacao_senha_updated_at
    before update
    on usuarios.recuperacao_senha
    for each row
execute function public.tg_set_updated_at();

-- =====================================================================================================================
-- Cor do item, antes guardada pelo app na primeira linha das observações.
-- =====================================================================================================================
alter table vistorias.item
    add column cor text,
    add constraint ck_item_cor check (cor is null or documento.texto_valido(cor, 1, 50));

-- A cópia da vistoria de entrada para a de saída leva também a cor, que descreve o item e não a avaliação.
create or replace procedure vistorias.copiar_vistoria(
    p_id_vistoria int
)
    language plpgsql
as
$$
declare
    v_entrada int;
begin
    select id_vistoria_entrada
    into v_entrada
    from vistorias.vistoria
    where id_vistoria = p_id_vistoria;

    if v_entrada is null then
        return;
    end if;

    insert into vistorias.ambiente (id_vistoria, id_tipo_ambiente, nome, id_ambiente_origem)
    select p_id_vistoria, a.id_tipo_ambiente, a.nome, a.id_ambiente
    from vistorias.ambiente a
    where a.id_vistoria = v_entrada
      and a.removido_em is null
    order by a.id_ambiente;

    insert into vistorias.item (id_ambiente, id_tipo_item, nome, descricao, material, cor, id_item_origem)
    select n.id_ambiente, i.id_tipo_item, i.nome, i.descricao, i.material, i.cor, i.id_item
    from vistorias.item i
             join vistorias.ambiente n on n.id_ambiente_origem = i.id_ambiente
    where n.id_vistoria = p_id_vistoria
      and i.removido_em is null
    order by i.id_item;
end;
$$;
