# Visura

Sistema de vistoria imobiliária. O app Android registra vistorias de entrada e de saída, ambiente por ambiente e item por item, com fotos, e gera o laudo em PDF. Os dados ficam numa API REST própria.

O repositório é um monorepo com duas partes, cada uma com seu próprio build:

| Pasta | O que é | Tecnologias |
|---|---|---|
| raiz (`app/`) | App Android | Kotlin, Jetpack Compose, Navigation Compose, Retrofit, OkHttp, Credential Manager |
| `backend/` | API REST | Java 17, Spring Boot 4, PostgreSQL 17 com `pg_cron`, Flyway, JWT |

A documentação completa da API, com regras de negócio, endpoints, segurança e variáveis de ambiente, está em [backend/README.md](backend/README.md).

---

## Branch `feature/pendencias-e-correcoes`

Esta branch resolve a lista de pendências e limitações conhecidas do projeto. Ela também traz as correções de um pente fino em todo o app e na API. Esta seção é o roteiro para revisar o PR.

### Resumo

| Commit | Conteúdo |
|---|---|
| `feat: Adiciona recuperação de senha, login com Google e cor do item na API` | backend |
| `feat: Integra recuperação de senha, Google, busca de endereço, câmera e laudo em PDF no app` | app |
| `docs: Atualiza os READMEs, o CLAUDE.md e o padrão da API` | documentação |

### O que foi implementado

**API (`backend/`)**

- **Recuperação de senha.** O fluxo usa um código de 6 dígitos enviado por e-mail, em três passos: `POST /auth/password/forgot`, `/verify` e `/reset`.
  - O código fica guardado como HMAC, vale por 15 minutos e aceita 5 tentativas.
  - A resposta é sempre igual, exista a conta ou não. Isso vale também quando o envio do e-mail falha, para não revelar quais contas existem.
  - As tabelas novas estão na migration `V14__recuperacao_senha_cor_item.sql`.
- **Login com Google** (`POST /auth/google`). A API confere a assinatura, o emissor e o público do ID token. Quem já tem conta com o mesmo e-mail fica com o Google vinculado no primeiro acesso. Quem não tem conta recebe um erro que leva o app ao cadastro.
- **Cor do item.** A cor ganhou uma coluna própria e é copiada para a vistoria de saída junto com os outros dados do item.
- **Responsável e andamento na listagem de vistorias.** A listagem já traz quem é o responsável e o progresso, calculados numa única consulta (`InspectionProgress`). Antes, o app fazia uma chamada para cada vistoria.
- **`GET /users/me`** passa a devolver o CPF da conta.
- **`PATCH` de item:** os campos `description`, `material`, `color` e `notes` enviados como texto vazio agora são apagados. Um campo omitido ou enviado como `null` continua sem mudança. Antes, não havia como apagar um campo pelo app.
- **Ambiente local.** O `backend/compose.yaml` sobe o PostgreSQL 17 com `pg_cron` e o Mailpit, que recebe os e-mails de teste.

**App (`app/`)**

- Telas de recuperação de senha (e-mail → código → nova senha) e login com Google (Credential Manager), ligadas à API.
- **Busca de endereço pelo ViaCEP.** O endereço é preenchido pelo CEP ou buscado por "rua, cidade, UF".
- **Fotos.** O item aceita fotos da câmera (pelo FileProvider) ou da galeria, inclusive antes de o item ser salvo. Toda foto é convertida para JPEG, reduzida e girada conforme o EXIF, para que a foto da câmera não fique deitada.
- **Laudo em PDF.** O PDF é gerado no aparelho, com os dados, as fotos e as linhas de assinatura, e abre o compartilhamento. Para gerar de novo o laudo de uma vistoria já concluída, basta tocar nela no histórico do imóvel.
- No cadastro de pessoa jurídica, a pessoa escolhe o próprio papel na empresa: sócio, representante legal ou titular MEI.
- A Home mostra o responsável e o andamento de cada vistoria.
- Para remover um ambiente, basta um toque longo nele; o app pede confirmação antes.
- A busca de pessoas cadastradas aceita o CPF, com ou sem pontuação.

### O que foi analisado e corrigido no pente fino

| Problema encontrado | Correção |
|---|---|
| Erros fora da API fechavam o app, por exemplo uma foto ilegível, uma falha no PDF ou um aparelho sem câmera. | O `executar` do NavGraph, o `Carregar`, as fotos, o laudo e a busca de endereço transformam a falha em aviso (`ApiErro` ou Toast). |
| As listas paravam em 50 registros, o limite de página da API. | O helper `todasAsPaginas` percorre as páginas de vistorias, imóveis e pessoas e a busca da vistoria de entrada. |
| Apagar a cor ou as observações de um item não tinha efeito, porque o valor antigo voltava. | O app envia o texto vazio e a API apaga o campo (veja o `PATCH` acima). |
| O CPF do perfil sumia ao entrar em outro aparelho, porque vinha só da sessão local. | O CPF agora vem de `GET /users/me`. |
| A data da Home podia pular para o dia seguinte à noite, porque o instante UTC era formatado sem fuso. | A data é formatada no fuso do aparelho. Há um teste para isso em `FormatacaoTest`. |
| O formulário do item e a recuperação de senha se perdiam quando o Android recriava a tela, por exemplo ao abrir a câmera ou girar o aparelho. | Esse estado agora usa `rememberSaveable`. |
| Se o login automático falhava depois de trocar a senha, parecia que a troca tinha falhado. | O app vai para o login com o aviso "Senha alterada. Entre com a nova senha." |
| A tela travava quando um ambiente ou item tinha sumido, ou quando faltavam dados do cadastro. | O app avisa e volta para a tela anterior. |
| Os tokens de sessão entravam no backup do Android. | `visura_sessao.xml` foi excluído de `backup_rules.xml` e de `data_extraction_rules.xml`. |
| A tela de nova senha não mostrava as regras da senha. | Ela usa a mesma validação do cadastro e avisa quando a confirmação não confere. |
| O título de um item podia ficar numa página do PDF e as fotos na seguinte, e os bitmaps não eram liberados. | O título fica junto das fotos, o laudo é gerado em `Dispatchers.IO` e os bitmaps são reciclados. |
| Uma falha no envio do e-mail de recuperação devolvia erro 500 e revelava que a conta existia. | A falha é registrada no log e a resposta continua 204. |

Também foram revistos, sem bugs encontrados: o armazenamento de fotos na API, a vinculação de pessoas, as telas de nova vistoria, conclusão, perfil e tipo do imóvel, e a correspondência campo a campo entre os modelos do app e os DTOs da API.

### Como foi verificado

- **API:** `./mvnw test` passou com 557 testes, 0 falhas e 1 ignorado. Há testes novos de fluxo para a recuperação de senha (`RecoveryFlowTest`), o login com Google (`GoogleLoginServiceTest`) e a limpeza de campos do item (`InspectionFlowTest`).
- **App:** `./gradlew assembleDebug testDebugUnitTest lintDebug` passou sem avisos de lint nos arquivos alterados. Há testes novos para a busca de endereço e a formatação de datas.
- **Emulador (Pixel 7):** testei cadastro, login, recuperação de senha, imóvel com busca de endereço, vistoria completa com fotos da câmera e da galeria, conclusão com laudo em PDF, remoção de ambiente e CPF no perfil. Uma foto com EXIF de rotação 90° chegou em pé na API, com 1440×1920.

### O que ainda não foi testado de ponta a ponta

- Falha do login automático logo depois de redefinir a senha.
- Aparelho sem app de câmera.
- Recriação da tela após a morte do processo; só a rotação e a abertura da câmera foram testadas.
- Apagar campos do item pela interface do app; só a API foi testada.
- Gerar de novo o laudo pelo histórico do imóvel.
- Login com Google com um client ID real; nos testes da API, a verificação do token é simulada.

---

## O que o app faz

- **Conta:** cadastro com e-mail e senha, como pessoa física ou jurídica, login, login com Google e recuperação de senha por código.
- **Imóveis:** cadastro com endereço, preenchido pelo CEP ou buscado no ViaCEP.
- **Vistorias:** de entrada ou de saída. A de saída copia os ambientes e os itens da entrada. Cada vistoria tem proprietário e inquilino, ambientes do catálogo ou personalizados, e itens com estado, material, cor, observações e fotos.
- **Laudo:** PDF com os dados, as fotos e as linhas de assinatura, compartilhado direto do app.
- **Home:** as vistorias, com o andamento e o responsável calculados pela API.

## Arquitetura do app

O código fica em `app/src/main/java/com/visura/`. Não há ViewModels nem injeção de dependência: a arquitetura é propositalmente enxuta.

- `data/`
  - `VisuraRepositorio` chama a API pela interface Retrofit `VisuraApi` e converte as respostas nos modelos das telas. Toda falha vira `ApiErro`, já com a mensagem para o usuário.
  - `Sessao` guarda os tokens, e o `ApiClient` renova o access token sozinho.
  - Também ficam aqui a busca de endereço (`BuscaDeEndereco.kt`), o laudo (`Laudo.kt`), o preparo das fotos (`Fotos.kt`) e o login com Google (`LoginGoogle.kt`).
- `ui/navigation/`: `VisuraNavGraph` é o único `NavHost`. Ele chama o repositório e navega; as telas só recebem dados e callbacks.
- `ui/screens/`: uma pasta por funcionalidade, cada uma com o estado do formulário guardado no próprio Compose.

Os detalhes estão em [CLAUDE.md](CLAUDE.md).

## Como rodar

### 1. API

Pré-requisitos: JDK 17+ e Docker.

```bash
cd backend
docker compose up -d                 # PostgreSQL 17 com pg_cron e Mailpit (e-mails em http://localhost:8025)
./mvnw -DskipTests package
java -jar target/visura-api-0.0.1-SNAPSHOT.jar
```

A API lê a configuração de variáveis de ambiente, listadas em [backend/README.md](backend/README.md#configuração-e-execução). Para o ambiente do `compose.yaml`:

- **Banco:** `DB_URL=jdbc:postgresql://localhost:5432/visura`, `DB_USERNAME=visura` e `DB_PASSWORD=visura`.
- **E-mail:** `MAIL_HOST=localhost` e `MAIL_PORT=1025`. O Mailpit não usa autenticação nem TLS, então a API precisa subir com estes argumentos: `--spring.mail.properties.mail.smtp.auth=false --spring.mail.properties.mail.smtp.starttls.enable=false --spring.mail.properties.mail.smtp.starttls.required=false`.

No Git Bash do Windows, defina `MSYS2_ENV_CONV_EXCL='*'` antes de subir a API. Sem isso, uma chave Base64 que comece com `/` é convertida em caminho do Windows e a API não inicia.

### 2. App

Pré-requisitos: um Android Studio recente (o build usa o AGP `9.3.0-alpha09`) e um emulador ou aparelho.

```bash
./gradlew installDebug
```

Configurações opcionais no `local.properties`:

| Chave | Para quê | Padrão |
|---|---|---|
| `visura.api.url` | Endereço da API, terminado em `/api/1/` | `http://10.0.2.2:8080/api/1/` (o computador, visto pelo emulador) |
| `visura.google.client.id` | Client ID do tipo "Web application" do Google Cloud, o mesmo de `GOOGLE_CLIENT_ID` na API | vazio: o botão do Google avisa que não está configurado |

### Login com Google

1. No Google Cloud Console, crie um client OAuth do tipo **Web application**. O ID dele vai em `visura.google.client.id` (app) e em `GOOGLE_CLIENT_ID` (API).
2. Crie também um client do tipo **Android**, com o pacote `com.visura` e o SHA-1 do certificado de debug, que aparece em `./gradlew signingReport`. O Google exige esse client, mas o app não usa o ID dele.
3. Só entra quem já tem conta no Visura com o mesmo e-mail. Sem conta, o app abre o cadastro com o nome e o e-mail preenchidos, porque o cadastro exige CPF.

## Testes

```bash
./gradlew testDebugUnitTest lint     # app: testes na JVM e lint
cd backend && ./mvnw test            # API: testes, inclusive de fluxo contra um PostgreSQL embutido
```

## Contribuição

Leia o [CONTRIBUTING.md](CONTRIBUTING.md). Em resumo: branches `feature/`, `fix/` ou `docs/`, e commits atômicos com prefixo convencional e mensagem em português (`feat: Adiciona ...`).
