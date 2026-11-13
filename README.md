# Visura

Sistema de vistoria imobiliária: o app Android registra vistorias de entrada e saída, ambiente por ambiente e item por item, com fotos, e gera o laudo em PDF. Os dados ficam numa API REST própria.

O repositório é um monorepo com duas partes de build independente:

| Pasta | O que é | Tecnologias |
|---|---|---|
| raiz (`app/`) | App Android | Kotlin, Jetpack Compose, Navigation Compose, Retrofit, OkHttp, Credential Manager |
| `backend/` | API REST | Java 17, Spring Boot 4, PostgreSQL 17 com `pg_cron`, Flyway, JWT |

A documentação completa da API (regras de negócio, endpoints, segurança e variáveis de ambiente) está em [backend/README.md](backend/README.md).

## O que o app faz

- **Conta:** cadastro com e-mail e senha, como pessoa física ou jurídica (a empresa é criada com o papel escolhido: sócio, representante legal ou titular MEI), login, login com Google e recuperação de senha por código enviado ao e-mail.
- **Imóveis:** cadastro com endereço, preenchido pelo CEP ou buscado por "rua, cidade, UF" no ViaCEP.
- **Vistorias:** entrada ou saída (a saída copia os ambientes da entrada), proprietário e inquilino, ambientes do catálogo ou personalizados e itens com estado, material, cor, observações e fotos da câmera ou da galeria.
- **Laudo:** ao concluir, o app monta um PDF com os dados, as fotos e as linhas de assinatura e abre o compartilhamento.
- **Home:** as vistorias com o andamento e o responsável, calculados pela API.

## Arquitetura do app

O código fica em `app/src/main/java/com/visura/`. Não há ViewModels nem injeção de dependência: a arquitetura é propositalmente enxuta.

- `data/`: `VisuraRepositorio` chama a API pela interface Retrofit `VisuraApi` e converte as respostas nos modelos das telas. Toda falha vira `ApiErro`, já com a mensagem para o usuário. `Sessao` guarda os tokens e o `ApiClient` renova o access token sozinho. Também ficam aqui a busca de endereço (`BuscaDeEndereco`, ViaCEP), o laudo em PDF (`Laudo.kt`), o preparo das fotos (`Fotos.kt`) e o login com Google (`LoginGoogle.kt`).
- `ui/navigation/`: `VisuraNavGraph` é o único `NavHost`. Ele chama o repositório e navega. As telas só recebem dados e callbacks.
- `ui/screens/`: uma pasta por funcionalidade, com o estado de formulário local do Compose.

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
- **E-mail:** `MAIL_HOST=localhost` e `MAIL_PORT=1025`, com estes argumentos, porque o Mailpit não usa autenticação nem TLS: `--spring.mail.properties.mail.smtp.auth=false --spring.mail.properties.mail.smtp.starttls.enable=false --spring.mail.properties.mail.smtp.starttls.required=false`.

No Git Bash do Windows, defina `MSYS2_ENV_CONV_EXCL='*'` antes de subir a API. Sem isso, uma chave Base64 que comece com `/` é convertida em caminho do Windows e a API não inicia.

### 2. App

Pré-requisitos: um Android Studio recente (o build usa o AGP `9.3.0-alpha09`) e um emulador ou aparelho.

```bash
./gradlew installDebug
```

Configurações opcionais no `local.properties`:

| Chave | Para quê | Padrão |
|---|---|---|
| `visura.api.url` | Endereço da API, terminado em `/api/1/` | `http://10.0.2.2:8080/api/1/` (o computador visto pelo emulador) |
| `visura.google.client.id` | Client ID "Web application" do Google Cloud, o mesmo de `GOOGLE_CLIENT_ID` na API | vazio: o botão do Google avisa que não está configurado |

### Login com Google

1. No Google Cloud Console, crie um client OAuth do tipo **Web application**. O ID dele vai em `visura.google.client.id` (app) e em `GOOGLE_CLIENT_ID` (API).
2. Crie também um client do tipo **Android**, com o pacote `com.visura` e o SHA-1 do certificado de debug (`./gradlew signingReport`). O Google exige esse client, mas o app não usa o ID dele.
3. Entra quem já tem conta no Visura com o mesmo e-mail. No primeiro acesso, a conta do Google fica vinculada. Sem conta, o app abre o cadastro com nome e e-mail já preenchidos, porque o cadastro exige CPF.

## Testes

```bash
./gradlew testDebugUnitTest lint     # app: testes na JVM e lint
cd backend && ./mvnw test            # API: testes, inclusive de fluxo contra um PostgreSQL embutido
```

## Contribuição

Leia o [CONTRIBUTING.md](CONTRIBUTING.md). Em resumo: branches `feature/`, `fix/` ou `docs/`, e commits atômicos com prefixo convencional e mensagem em português (`feat: Adiciona ...`).
