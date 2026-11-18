# Contribuindo com o backend

Este guia vale só para a API, que fica em `backend/`. O fluxo de Git e de Pull Request comum ao repositório está no [CONTRIBUTING da raiz](../CONTRIBUTING.md). O que a API faz está no [README do backend](README.md).

## Sumário

- [Ambiente](#ambiente)
- [Executar localmente](#executar-localmente)
- [Testes](#testes)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Tratamento de erros](#tratamento-de-erros)
- [Ferramentas de qualidade](#ferramentas-de-qualidade)
- [Git e Pull Requests](#git-e-pull-requests)
- [Padrão de código](#padrão-de-código)

## Ambiente

| Ferramenta | Versão | Para quê |
|---|---|---|
| JDK | 17 ou mais novo (o `pom.xml` compila para 17) | Compilar e rodar |
| Maven | O wrapper `./mvnw` baixa a versão certa | Build e testes |
| Docker | Qualquer versão com `docker compose` | Subir o PostgreSQL 17 com `pg_cron` e o Mailpit (`compose.yaml`) |

Os testes não precisam de Docker nem de banco instalado: eles sobem um PostgreSQL embutido.

## Executar localmente

```bash
cd backend
docker compose up -d                 # PostgreSQL em localhost:5432 e Mailpit (e-mails em http://localhost:8025)
./mvnw -DskipTests package
java -jar target/visura-api-0.0.1-SNAPSHOT.jar
```

A API lê a configuração de variáveis de ambiente, que estão listadas em [README.md](README.md#configuração-e-execução). Para o banco do `compose.yaml`, use:

```text
DB_URL=jdbc:postgresql://localhost:5432/visura
DB_USERNAME=visura
DB_PASSWORD=visura
MAIL_HOST=localhost
MAIL_PORT=1025
```

O Mailpit não usa autenticação nem TLS. Por isso, suba a API com estes argumentos:

```text
--spring.mail.properties.mail.smtp.auth=false
--spring.mail.properties.mail.smtp.starttls.enable=false
--spring.mail.properties.mail.smtp.starttls.required=false
```

No Git Bash do Windows, defina `MSYS2_ENV_CONV_EXCL='*'` antes de subir a API. Sem isso, uma chave Base64 que começa com `/` é convertida em caminho do Windows, e a API não inicia.

## Testes

```bash
./mvnw test                                   # suíte completa
./mvnw test -Dtest=InspectionFlowTest         # uma classe
```

- **Testes unitários** ficam no mesmo pacote da classe testada, em `src/test/java/com/visura/<pacote>`.
  - Os serviços usam repositórios simulados com Mockito.
  - A cadeia de segurança real roda com MockMvc: `support/WebSecurityTest`.
- **Testes de integração** são as classes `*FlowTest`, que estendem `support/ApplicationTest`. Elas sobem a aplicação inteira contra um PostgreSQL embutido (`io.zonky.test:embedded-postgres`), com as migrations aplicadas.
- **Procedures e triggers** são testados direto no PostgreSQL embutido, em `src/test/java/com/visura/database`.
- **Funcionalidade nova:** inclua um teste de serviço. Se a funcionalidade tiver regra no banco ou uma rota nova, inclua também um teste de fluxo.
- **Windows:** o teste que recusa anexos por link simbólico é pulado, porque criar links simbólicos exige permissão de administrador. Ele roda normalmente no Linux.

## Banco de dados e migrations

- O schema pertence ao Flyway, e o Hibernate roda com `ddl-auto: validate`. Toda mudança de banco entra numa migration nova em `src/main/resources/db/migration`, com o próximo número (`V17__descricao.sql`).
- **Nunca altere uma migration que já rodou em algum ambiente.** O Flyway confere o checksum e recusa a subida.
- Use o prefixo `rn_` nas regras de negócio aplicadas no banco e lance o erro com `rotinas.violar`. A API converte o nome da regra no status HTTP.
- A role da aplicação, `vsr_app` (`docs/db/app-role.sql`), não apaga linhas nem altera o schema. Quando criar um schema ou uma rotina, inclua os `grant` correspondentes.
- O prefixo `vsr`, que aparece na role, nas propriedades `vsr.*` e nas migrations, vem do nome antigo da API. Ele fica assim de propósito.

## Tratamento de erros

- Os erros de domínio são exceções de `exception/custom`, ligadas a um enum de problema em `exception/enums/problem`, que define o status e a mensagem.
- Os handlers de `exception/handler` convertem essas exceções numa resposta RFC 9457 (`ProblemDetail`).
- Não devolva `500` para erro do cliente nem esconda a falha com um `catch` genérico. Os erros `4xx` geram uma linha `WARN`, e os erros inesperados geram `ERROR` com o stack trace.
- Uma resposta não pode revelar se uma conta, CPF ou e-mail existe quando a regra pede sigilo, como na recuperação de senha e na inclusão de membro.

## Ferramentas de qualidade

O projeto não tem linter nem formatador configurado no Maven (Checkstyle, Spotless e afins). O padrão de código é garantido de duas formas:

- O `ServicePatternTest` confere a forma dos serviços e roda junto com `./mvnw test`.
- A revisão do Pull Request confere o resto, com base nas regras da seção abaixo.

O ArchUnit está declarado no `pom.xml`, mas ainda não há testes de arquitetura.

## Git e Pull Requests

Siga o fluxo do [CONTRIBUTING da raiz](../CONTRIBUTING.md#fluxo-de-trabalho), com estes cuidados do backend:

- Faça a mudança de banco num commit próprio, quando for possível: `feat: adiciona migration V15 com ...`.
- Rode `./mvnw test` antes de abrir o PR e informe o resultado na descrição.
- Se a mudança alterar uma rota, um payload ou um código HTTP, atualize a seção [API do README](README.md#api) e confira se o app (`app/src/main/java/com/visura/data/remote/`) continua compatível.

---

## Padrão de código

Regras que valem para todo o código de produção. As regras de forma dos serviços são garantidas pelo `ServicePatternTest`; o glossário é conferido na revisão.

### Princípios

1. **Um nível de indentação por método.**
2. **Um nível de abstração por método:** o público lista passos; cada passo é um método com uma intenção.
3. **Um verbo, um sentido:** o mesmo verbo nunca significa coisas diferentes no projeto (ver [Glossário](#glossário)).

### Métodos

- O público só chama passos. Todo desvio de fluxo (`if`, `try`, `throw`, `orElseThrow`, laço que valida) fica num auxiliar próprio.
- Cada passo fica no seu método, inclusive o que só repassa para outro serviço.
- **O privado que repassa para outro serviço usa o mesmo verbo do método chamado** (`issue` → `refreshTokenService.issue`). Se esse nome já existir na classe, ele recebe o substantivo do que devolve (`tokens` → `tokenService.issue`).
- O privado que chama repositório, mapper ou framework traduz o nome técnico para o domínio (`clear` → `sessionRepository.revokeDevice`).
- Guardas são `void`, têm o nome da condição exigida (`present`, `fresh`, `unexpired`) e o corpo do `if` só sai cedo (`throw` ou `return`).
- O `TokenBucket` é exceção declarada: segue o formato do commit b9f9420 (ver [Rate limit](#rate-limit)).

### Nomes

- **Uma palavra** por padrão. **Duas palavras** só quando nenhuma palavra sozinha diz a coisa sem ambiguidade. Vale também para campos de record, que viram métodos.
- Booleanos sem prefixo `is`/`has`/`can`, sempre na **forma positiva**. A negação fica no `if` (`if (!current(...))`), sem criar um booleano gêmeo negado (`wrong`, `unused`).
- Passo que devolve valor pode ter nome de substantivo (`person`, `user`, `token`); ação tem nome de verbo.
- Nunca dois métodos com o mesmo nome na mesma classe.

### Ordem no arquivo

Públicos → passos na ordem em que são chamados → auxiliares de desvio de fluxo → booleanos.

### Glossário

#### Acesso e conta

| Verbo | Sentido | Onde |
|---|---|---|
| `register` | cadastrar um usuário | `RegisterService` |
| `open` | criar algo com ciclo de vida (conta, sessão) | `AccountService`, `SessionService` |
| `authenticate` | conferir e-mail e senha pelo Spring Security | `LoginAttemptService` |
| `verify` | conferir um segredo (senha, credenciais, JWT) e lançar se não bater | `LocalCredentialService`, `*AttemptService`, `TokenService` |
| `grant` | dar acesso a um usuário já conferido: dispositivo, sessão e tokens | `AccessService` |
| `enroll` | registrar ou atualizar o dispositivo do usuário | `DeviceService` |

#### Senha

| Verbo | Sentido | Onde |
|---|---|---|
| `change` | caso de uso de troca de senha | `ChangePasswordService` |
| `commit` | efetivar, numa transação, uma troca já validada (revoga as sessões, grava, emite tokens) | `PasswordService` |
| `replace` | gravar a nova senha no lugar da atual | `LocalCredentialService` |
| `hash` | calcular o hash de um segredo | `LocalCredentialService`, `RefreshTokenService` |
| `rehash` | conferir que a nova senha é diferente da atual e calcular o hash dela | `LocalCredentialService` |
| `upgrade` | gravar o hash recalculado com os parâmetros atuais do Argon2, depois de um login correto | `LocalCredentialService`, `AuthenticationConfig` |
| `swap` | `replace` que desiste em silêncio se outra troca de senha gravou primeiro | `LocalCredentialService` |

#### Recuperação de senha e login com Google

| Verbo | Sentido | Onde |
|---|---|---|
| `request` | caso de uso do pedido de código: gasta o limite da conta e, se a conta existir, emite e envia o código | `RecoveryService` |
| `confirm` | caso de uso de conferir o código sem trocar a senha | `RecoveryService` |
| `reset` | caso de uso da nova senha pelo código / gravar a nova senha de quem recuperou a conta | `RecoveryService`, `LocalCredentialService` |
| `checked` | a recuperação aberta, depois de gastar uma tentativa e conferir o código | `RecoveryService` |
| `deliver` | emitir o código para o usuário e enviar o e-mail | `RecoveryService` |
| `restore` | efetivar, numa transação, a senha nova da recuperação (encerra o código, grava, revoga as sessões) | `PasswordService` |
| `spend` | gastar uma tentativa do código, só se ainda houver saldo | `RecoveryCodeService`, `PasswordRecoveryRepository` |
| `redeem` | encerrar o código usado; recusa se outro pedido o usou primeiro | `RecoveryCodeService`, `PasswordRecoveryRepository` |
| `linked` / `bind` / `link` | a conta já vinculada ao Google / achar a conta pelo e-mail confirmado / gravar o vínculo | `GoogleLoginService` |

#### Tokens

| Verbo | Sentido | Onde |
|---|---|---|
| `issue` | gerar um token novo | `RefreshTokenService`, `TokenService`, `JwtToken` |
| `sign` | gerar o access token assinado (JWT) | `TokenService` |
| `renew` | gerar o próximo refresh token no lugar do atual | `RefreshTokenService`, `RefreshService` |
| `rotate` | trocar as credenciais em uso por novas | `AccessService`, `RefreshService`, `RefreshTokenService` |
| `reused` | o refresh token apresentado já foi trocado | `RefreshTokenService` |
| `revokeReused` | revogar a sessão quando um refresh token reutilizado aparece | `RefreshService` |
| `tokens` | o par access + refresh token (passo que devolve `TokenResponse`) | `AccessService` |

#### Sessão

| Verbo | Sentido | Onde |
|---|---|---|
| `access` | registrar o uso de uma sessão do dono (valida e toca) | `SessionService` |
| `resume` | retomar a sessão do refresh token e devolver o dono | `SessionService` |
| `touch` | atualizar o último acesso | `SessionService` |
| `active` | exigir sessão não revogada, não expirada e não ociosa | `SessionPolicyService` |
| `revoke` | revogar uma sessão do dono (logout); o sufixo diz o alcance (`revokeAll`, `revokeReused`) | `SessionService` |
| `invalidate` | revogar uma sessão pelo id, sem conferir o dono | `SessionService`, `RefreshService` |
| `close` | revogar uma sessão já carregada | `SessionService` |
| `revokeAll` | conferir a sessão do dono e revogar todas as sessões dele | `SessionService`, `PasswordService` |
| `dismiss` | revogar todas as sessões do usuário | `SessionService` |
| `clear` | revogar as sessões de um dispositivo | `SessionService` |
| `cap` | manter o usuário dentro do máximo de sessões | `SessionService` |

#### Rate limit

O algoritmo fica inteiro no `TokenBucket`, a mesma conta do script Lua (formato do commit b9f9420), com uma chave genérica e intocado; em volta dele ficam só o limite de chaves e a limpeza; com os records `Limit`, `Allowance` e `Bucket` dentro dele. Os records só carregam dados: a validação do `Limit` e o cálculo do `retry` ficam no `TokenBucket`. Este bloco segue esse formato em vez das regras de passos acima.

| Nome | Sentido | Onde |
|---|---|---|
| `allow` | recarregar e gastar uma ficha da chave dentro do `compute`; `allow(key, now)` recebe o instante (gancho de teste) | `TokenBucket` |
| `evict` | remover os saldos ociosos (`findIdleKeys` acha, `removeKeys` remove só se ainda estiver ocioso) | `TokenBucket` |
| `refillInterval` | o `period` do `Limit` em segundos, calculado uma vez | `TokenBucket` |
| `ttl` | tempo para encher do zero: `ceil(capacity / refillRate)` intervalos; depois dele o saldo é `idle` | `TokenBucket` |
| `lastRefill` | instante da última recarga | `TokenBucket.Bucket` |
| `reset` | segundos até a próxima recarga | `TokenBucket.Allowance` |
| `retry` | o `reset` arredondado para cima, com mínimo de 1 (`TokenBucket.retry(allowance)`) | `TokenBucket` |
| `validate` | exigir capacidade, taxa e período maiores que zero ao montar o `TokenBucket` | `TokenBucket` |
| `cleaner` / `schedule` | a thread de limpeza compartilhada por todos os `TokenBucket` / agendar o `evict` periódico | `TokenBucket` |
| `crowded` | o limite de chaves (200 mil) está cheio e a chave é nova; antes de recusar, chama o `evict` no máximo uma vez por segundo (`due`) | `TokenBucket` |
| `capacity` / `rate` / `period` | fichas máximas / fichas por recarga / tempo entre recargas (`Duration`), como no `rate-limit.yaml` | `TokenBucket.Limit` |
| `pattern` | o caminho da rota em qualquer versão (`/api/*` + caminho) | `Route` |
| `resolve` | escolher o `TokenBucket` da requisição, ou o fallback `api` | `RateLimitPolicy` |
| `account` / `ceiling` | o `TokenBucket` da conta (conta e rede no login; usuário na troca de senha) / o do teto da conta | `RateLimitPolicy` |
| `from` | montar as rotas da API com o limite de cada uma | `Routes` |
| `deadline` | instante (epoch) da próxima recarga | `RateLimitFilter` |
| `advise` | informar no `Retry-After` quando tentar de novo | `RateLimitFilter` |
| `within` | exigir que o consumo esteja dentro do limite, senão `429` | `LoginAttemptService`, `PasswordAttemptService` |
| `network` / `account` | no login, gastar da conta naquela rede / do teto da conta (só se a rede ainda tinha saldo) | `LoginAttemptService` |

Classes do rate limit:

| Classe | Responsabilidade |
|---|---|
| `TokenBucket` | o algoritmo e os saldos de um limite, a validação do limite e o `retry`, com `Limit` (configuração), `Allowance` (resultado) e `Bucket` (saldo) aninhados como records só de dados |
| `Route` | record no estilo dos VOs: método e caminho validados |
| `RateLimitPolicy` | dona de todos os `TokenBucket`: o de cada rota, o fallback, o da conta e o do teto; fecha todos no `close()` |
| `Routes` | as rotas da API e o limite de cada uma |

#### Rede

| Verbo | Sentido | Onde |
|---|---|---|
| `address` | ler o endereço do cliente como literal IP, sem DNS | `ClientNetwork` |
| `literal` | exigir que o texto seja um literal IPv4 ou IPv6 | `ClientNetwork` |
| `ip` | o texto tem forma de literal IP | `ClientNetwork` |
| `parse` | converter o literal em `InetAddress` | `ClientNetwork` |
| `invalid` | a exceção de endereço inválido | `ClientNetwork` |
| `network` | a rede (`Network`) do endereço já lido | `ClientNetwork` |
| `from` | escolher a família (`Ipv4`, `Ipv6`) pelo endereço | `Network` |
| `key` | chave da rede do cliente (ex.: `203.0.113.7`, `2001:db8::/56`), parte da chave do saldo | `Network`, `Mask`, `ClientNetwork` |
| `prefix` | bits que identificam a rede na família | `Ipv4`, `Ipv6` |
| `format` | formato da chave da família (`%s`, `%s/%d`) | `Ipv4`, `Ipv6` |
| `valid` | o texto tem a sintaxe de um endereço da família | `Ipv4`, `Ipv6` |
| `syntax` | a regex que define a sintaxe da família (no IPv6, montada a partir das formas abaixo) | `Ipv4`, `Ipv6` |
| `expanded` | a forma do IPv6 com os 8 hextets escritos | `Ipv6` |
| `compressed` | a forma do IPv6 com `::` depois de um número de hextets | `Ipv6` |
| `head` / `tail` | os hextets antes / depois do `::` | `Ipv6` |
| `apply` | aplicar o prefixo e devolver o endereço da rede | `Mask` |
| `truncate` | cortar o endereço no prefixo, zerando os bits depois dele | `Mask` |

#### Gerais

| Verbo | Sentido |
|---|---|
| `create` | montar uma entidade sem gravar |
| `save` | gravar |
| `find` | buscar e lançar se não existir (nunca devolve `Optional`) |
| `show` | converter para o DTO de resposta |
| `send` / `sendAttachments` | enviar e-mail sem anexos / com anexos |
| `load` | conferir e ler os anexos uma vez, pelo caminho real e sem seguir link | `MailService` |
| `plain` / `multipart` | montar a mensagem de texto / com partes |
| `view` | consultar um recurso do dono e devolver a resposta (`ProfileService`, `PropertyService`) |

#### Imóveis e endereços

| Verbo | Sentido | Onde |
|---|---|---|
| `add` | caso de uso de cadastro de um recurso do usuário (imóvel) | `PropertyService` |
| `list` | consultar, paginada, a coleção do dono | `PropertyService` |
| `edit` | alteração parcial: campo ausente fica como está | `PropertyService` |
| `amend` | aplicar no entity os campos enviados na edição | `PropertyService`, `PropertyMapper` |
| `remove` | remoção lógica (`removido_em`): a aplicação nunca apaga linhas | `PropertyService` |
| `discard` | chamar a procedure de remoção lógica | `PropertyService` |
| `owned` | buscar o recurso pelo dono e lançar `NOT_FOUND` se não for dele ou se foi removido | `PropertyService` |
| `locate` | localizar o endereço pela cadeia estado, cidade, bairro, logradouro e endereço, criando o que faltar | `AddressService` |
| `results` | converter a página de entidades para `PageResponse` | `PropertyService`, `InspectionService` |
| `find` (público) | entregar a entidade do dono a outro serviço, lançando `NOT_FOUND` | `PropertyService`, `InspectionService`, `RoomService` |
| `store` | gravar já (`saveAndFlush`), para o banco validar a alteração dentro do caso de uso | `InspectionService`, `RoomService`, `ItemService` |

#### Vistorias e catálogo

| Verbo | Sentido | Onde |
|---|---|---|
| `start` | iniciar uma vistoria (entrada ou saída) | `InspectionService` |
| `copy` | copiar ambientes e itens da vistoria de entrada para a de saída (procedure) | `InspectionService` |
| `finish` | caso de uso de finalização da vistoria | `InspectionService`, `InspectionRepository` |
| `conclude` | chamar a finalização no banco | `InspectionService` |
| `entry` | a vistoria de entrada do dono, ou `null` sem entrada | `InspectionService` |
| `fill` | pôr na entidade nova a coleção recém-gravada, para a resposta | `RoomService` |
| `standard` | itens típicos do tipo de ambiente, criados no ambiente novo | `RoomService`, `ItemMapper` |
| `rooms` / `items` | o catálogo de tipos de ambiente / de item que o usuário vê | `CatalogService` |
| `room` / `item` | o tipo de ambiente / de item do catálogo que o usuário vê, ou `NOT_FOUND` | `CatalogService` |
| `contents` | os itens de um tipo de ambiente que o usuário vê | `CatalogService` |
| `accessible` | o tipo do catálogo é padrão ou do próprio usuário | `CatalogService` |

#### Pessoas

| Verbo | Sentido | Onde |
|---|---|---|
| `individual` / `company` | cadastrar pessoa física / jurídica no cadastro privado do usuário | `PersonService` |
| `natural` / `legal` | montar a pessoa física / jurídica a partir do pedido | `PersonService` |
| `rename` / `retitle` | trocar o nome (validado pelo subtipo) / o nome fantasia; nulo mantém o atual | `Person`, `Individual`, `Company` |
| `lookup` | o contato da pessoa, ou `null` sem contato | `ContactService` |
| `index` | os contatos de várias pessoas, por id da pessoa | `ContactService`, `PersonService` |
| `keep` | gravar o contato só se tiver telefone ou e-mail | `ContactService` |
| `link` / `unlink` | vincular / desvincular (logicamente) uma pessoa a um imóvel ou a uma vistoria | `OwnerService`, `ParticipantService` |
| `bind` / `release` | chamar o vínculo / o desvínculo no banco | `OwnerService`, `ParticipantService` |

#### Empresas e catálogo personalizado

| Verbo | Sentido | Onde |
|---|---|---|
| `open` (empresa) | cadastrar a empresa e vincular quem cadastra como administrador | `CompanyService`, `MembershipService` |
| `verify` (vínculo) | exigir que o usuário seja membro ativo da empresa, senão `404` | `MembershipService`, `CompanyService` |
| `admin` / `administrator` | exigir papel de administrador (sócio, representante, MEI) | `MembershipService`, `CompanyService` |
| `removable` | o membro sai sozinho; remover outro exige administrar | `MembershipService` |
| `companies` | os vínculos ativos do usuário | `MembershipService` |
| `addRoom` / `addItem` | criar tipo de ambiente / de item personalizado | `CatalogService` |
| `chosen` | os tipos de item escolhidos, conferidos um a um | `CatalogService` |
| `attach` | ligar os itens ao tipo de ambiente personalizado | `CatalogService`, `RoomTypeRepository` |

#### Evidências

| Verbo | Sentido | Onde |
|---|---|---|
| `upload` | caso de uso de envio de evidência: confere o formato, grava o arquivo e o registro | `EvidenceService` |
| `download` | caso de uso de leitura do arquivo da evidência do dono | `EvidenceService` |
| `media` | ler o arquivo enviado e conferir o formato pelos bytes (`Media.of`) | `EvidenceService` |
| `save` (arquivo) | gravar o arquivo com nome gerado e apagá-lo se a transação for desfeita | `StorageService` |
| `load` (arquivo) | conferir e ler o arquivo uma vez, pelo caminho real e sem seguir link | `StorageService` |
| `cleanup` | registrar a remoção do arquivo no rollback da transação | `StorageService` |
| `record` | gravar o registro da evidência numa transação curta e, se recusado, apagar o arquivo | `EvidenceService` |
| `intact` | exigir que o arquivo lido confira com o SHA-256 gravado no envio | `EvidenceService` |
| `delete` / `erase` | apagar o arquivo, só registrando a falha no log | `StorageService` |
| `stale` | arquivos do diretório mais antigos que a idade informada | `StorageService`, `OrphanService` |
| `sweep` / `purge` | limpeza periódica de órfãos / apagar um arquivo sem registro | `OrphanService` |
