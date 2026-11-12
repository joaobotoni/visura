# CLAUDE.md

Este arquivo orienta o Claude Code (claude.ai/code) ao trabalhar com o código deste repositório.

## Projeto

Visura é um sistema de vistoria imobiliária. Os textos exibidos ao usuário e os identificadores de conceitos do domínio estão em português (pt-BR): `Imovel`, `Vistoria`, `NovoImovelScreen`, `abaSelecionada`.

O repositório é um monorepo:

- **App Android**: fica na raiz, com módulo único `:app`, Kotlin e Jetpack Compose. O pacote, o namespace e o `applicationId` são todos `com.visura`, e o projeto raiz do Gradle se chama `Visura`.
- **API REST**: fica em `backend/`. É uma API em Spring Boot 4 (Java 17, Maven, PostgreSQL com `pg_cron`, Flyway e JWT), com pacote `com.visura` e artefato `visura-api`. Ela veio do repositório `vsr` com o histórico completo via `git subtree`, e o desenvolvimento dela agora acontece só aqui. As migrations, a role `vsr_app` do banco e o prefixo `vsr.` das propriedades mantêm o nome antigo de propósito. Não mexa neles: o Flyway confere o checksum das migrations que já rodaram.

Os dois têm builds independentes. A documentação da API, com regras de negócio, endpoints, variáveis de ambiente e testes, está em `backend/README.md`, e o padrão de código está em `backend/docs/PADRAO.md`. Antes de mexer em `backend/`, leia esses dois arquivos.

A versão antiga do app, com Hilt e Firebase, está guardada nas branches `backup/backend-firebase-hilt` e `backup/backend-supabase`. Não use essa arquitetura como referência.

## Comandos

Para o app, use `./gradlew` (Git Bash) ou `.\gradlew.bat` (PowerShell), na raiz:

```bash
./gradlew assembleDebug            # gera o APK de debug
./gradlew installDebug             # instala no dispositivo/emulador conectado
./gradlew build                    # build completo, com lint e testes unitários
./gradlew lint                     # lint do Android
./gradlew testDebugUnitTest        # testes unitários na JVM (app/src/test)
./gradlew connectedDebugAndroidTest # testes instrumentados (precisa de dispositivo)
```

O build usa o AGP `9.3.0-alpha09`, então exige um Android Studio recente e compatível. Também usa compileSdk/targetSdk 37 e minSdk 24, com JVM target 11. As versões ficam em `gradle/libs.versions.toml`. O app não tem testes além dos exemplos do template.

O endereço da API vem de `visura.api.url` no `local.properties` e vira `BuildConfig.API_BASE_URL`. Ele precisa terminar em `/api/1/`. O padrão é `http://10.0.2.2:8080/api/1/`, que é o localhost do computador visto pelo emulador. Num aparelho físico, use o IP do computador na rede. O HTTP sem TLS só é liberado no build de debug (`app/src/debug/AndroidManifest.xml`).

Para a API, rode os comandos dentro de `backend/`:

```bash
./mvnw -DskipTests package         # compila e gera o jar
./mvnw test                        # testes (os de fluxo sobem um PostgreSQL embutido; não precisam de banco instalado)
```

Para subir a API de verdade, é preciso um PostgreSQL 17 com `pg_cron` e as variáveis de ambiente listadas em `backend/README.md`.

## Arquitetura do app

O código fica em `app/src/main/java/com/visura/`. Não há Hilt nem ViewModels.

- **Dados** (`data/`): `VisuraApplication` cria uma única vez a `Sessao`, o cliente da API e o `VisuraRepositorio`. As telas chegam ao repositório pelo `NavGraph`.
  - `remote/`: `VisuraApi` é a interface Retrofit com os endpoints. `ApiModels.kt` traz os DTOs JSON, com campos em inglês iguais aos do Spring e anuláveis, porque o Gson ignora a nulabilidade do Kotlin. O `ApiClient` põe o access token em cada chamada e, num 401, troca o refresh token e repete a chamada uma vez. Se a API recusar o refresh, limpa a sessão e liga `Sessao.expirou`, e o `NavGraph` volta ao login. Falha de rede não derruba a sessão.
  - `Sessao` guarda tokens, nome, e-mail, CPF e o id do dispositivo no SharedPreferences. A API não devolve o CPF do usuário, então o app guarda o que foi digitado no cadastro.
  - `VisuraRepositorio` chama a API e converte as respostas nos modelos das telas. Toda falha vira `ApiErro`, já com a mensagem para o usuário. `chamar { }` (em `ApiErro.kt`) faz essa conversão e usa o `errors[].message` ou o `detail` do erro RFC 9457 da API.
  - A API não tem campo de cor no item. A cor é gravada como primeira linha das observações (`Cor: Bege`) e separada de novo na leitura (`juntarCor`/`separarCor`).
  - `prepararFoto` (`Fotos.kt`) converte a foto da galeria em JPEG de até 2048 px antes do upload.
- **Navegação** (`ui/navigation/`): as rotas são constantes string no objeto `Routes`, com funções que montam o caminho. Os ids nas rotas são os UUIDs da API. `VisuraNavGraph` é o único `NavHost`. O destino inicial é `HOME` quando há sessão salva e `LOGIN` quando não há. As telas nunca recebem o `NavController` nem o repositório: elas expõem callbacks com os dados digitados, e o `NavGraph.kt` chama a API e navega.
  - `executar { }` roda uma ação da API. Enquanto ela roda, a tela fica bloqueada e aparece uma barra de progresso. Um erro aparece num snackbar global.
  - `Carregar(chaves, carregar = { ... }) { dados, recarregar -> }` (`ui/components/Carregamento.kt`) busca os dados de uma tela e mostra o progresso ou o erro com "Tentar de novo".
  - Os ambientes da vistoria aberta ficam no estado `ambientes`, acima do `NavHost`, e são recarregados da API toda vez que a lista de ambientes abre. As telas de ambiente e item leem desse estado e o atualizam com a resposta de cada chamada.
  - Fluxo da vistoria: `NOVA_VISTORIA` cria a vistoria na API e é tirada da pilha, depois `PESSOAS_VINCULADAS` (`{imovelId}/{vistoriaId}`), `AMBIENTES`, `REVISAO` (que finaliza na API) e `CONCLUSAO`. Tocar numa vistoria em andamento no detalhe do imóvel retoma a partir das pessoas.
- **Telas** (`ui/screens/<funcionalidade>/`): o estado de formulário é local do Compose. Os modelos do domínio são `data class`es simples, definidos junto às telas: `Imovel` e `PessoaVinculada` em `imoveis/Imovel.kt`, `Vistoria` em `home/Vistoria.kt`, `Ambiente`, `ItemAmbiente` e `EstadoItem` em `ambientes/Ambiente.kt`, e `TipoVistoria` em `vistoria/`. Os enums que vão para a API têm `valorApi`: `TipoImovel`, `CategoriaImovel` e `EstadoItem`. Eles só podem ter os valores que a API aceita.
- **O que ainda é simulado**, porque a API não tem endpoint para isso: a recuperação de senha (as três telas só navegam), o login com Google (o botão mostra um aviso, e a `GoogleAccountPickerScreen` não está no grafo), a busca de endereço (`enderecosDeExemplo` em `BuscarEnderecoOverlay.kt`) e o laudo em PDF (a conclusão compartilha um resumo em texto).
- **Tema** (`ui/theme/`): `VisuraTheme` envolve o app no `MainActivity`, que também ativa o edge-to-edge com barras de sistema transparentes e claras. As telas usam principalmente as cores nomeadas de `Color.kt` (`NavyBlue`, `BorderGray`, `StatusOrangeBg`, …) direto, e não `MaterialTheme.colorScheme`. Os helpers compartilhados são `Modifier.clickableNoRipple` (`theme/Modifiers.kt`), além de `Modifier.cardBorder()` e `StatCard` (`screens/home/HomeShared.kt`, `internal`).

## Fluxo de Git

- Crie branches com os prefixos `feature/`, `fix/` ou `docs/`.
- Faça commits atômicos com prefixos convencionais e o resto da mensagem em português (`feat: Adiciona ...`). Os commits mais antigos das telas estão em inglês.
