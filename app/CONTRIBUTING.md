# Contribuindo com o app Android

Este guia vale só para o aplicativo, que fica em `app/`. O fluxo de Git e de Pull Request comum ao repositório está no [CONTRIBUTING da raiz](../CONTRIBUTING.md), e a arquitetura do app está no [README do app](README.md).

## Sumário

- [Ambiente](#ambiente)
- [Executar localmente](#executar-localmente)
- [Arquitetura e padrões de código](#arquitetura-e-padrões-de-código)
- [Convenções de nomenclatura](#convenções-de-nomenclatura)
- [Tratamento de erros](#tratamento-de-erros)
- [Dependências](#dependências)
- [Lint](#lint)
- [Testes](#testes)
- [Git e Pull Requests](#git-e-pull-requests)

## Ambiente

1. Instale um Android Studio recente, compatível com o AGP 9.3, e o Android SDK com a plataforma 37.
2. Abra a pasta **`app/`** no Android Studio: ela é o projeto Gradle. A raiz do repositório não é um projeto Gradle.
3. Na primeira sincronização, o Android Studio cria o `app/local.properties` com o `sdk.dir`. Esse arquivo não vai para o Git.
4. Opcional: defina `visura.api.url` e `visura.google.client.id` no `local.properties`. Veja [Configuração](README.md#configuração).

O Gradle Wrapper vem no repositório (`gradlew`, `gradlew.bat` e `gradle/wrapper/`), então não é preciso instalar o Gradle. O daemon pede a JVM 21 e a baixa sozinho, se ela não estiver instalada.

## Executar localmente

1. Suba a API seguindo o [CONTRIBUTING do backend](../backend/CONTRIBUTING.md#executar-localmente).
2. Inicie um emulador. No emulador, o endereço padrão `http://10.0.2.2:8080/api/1/` aponta para o computador.
3. Instale e abra o app:

```bash
cd app
./gradlew installDebug
```

Num aparelho físico, defina `visura.api.url=http://<IP-do-computador>:8080/api/1/` no `local.properties`.

## Arquitetura e padrões de código

- **Camadas:** a tela chama um callback, o `NavGraph` repassa ao `VisuraViewModel`, que chama o `VisuraRepositorio`, que chama a `VisuraApi`. Não pule camadas.
- **Telas:**
  - A tela não recebe `NavController`, repositório nem ViewModel. Ela recebe dados e callbacks com o que o usuário digitou.
  - As telas novas ficam em `ui/screens/<funcionalidade>/`.
- **Estado:**
  - O estado que precisa passar de uma tela para outra, ou sobreviver à rotação, fica no `VisuraViewModel`: num `StateFlow`, ou no `SavedStateHandle` se precisar sobreviver à morte do processo.
  - O estado de formulário usa `rememberSaveable`. As senhas são a exceção: usam `remember`, para não irem para o estado salvo.
- **Ações:**
  - Uma ação que chama a API roda em `executar { }` no ViewModel, nunca em `rememberCoroutineScope`.
  - Navegação, avisos e compartilhamento saem como `Evento`. O ViewModel não guarda Activity, Context de tela nem NavController.
- **Camadas sem ciclo:**
  - `data/` e `model/` não importam de `ui/`.
  - `model/` não depende de Compose.
- **Contrato com a API:**
  - Os DTOs ficam em `data/remote/ApiModels.kt`, com os nomes de campo da API e os campos anuláveis.
  - Se uma rota mudar no backend, atualize `VisuraApi` e o README do backend no mesmo PR.
- **Tema:** use as cores nomeadas de `ui/theme/Color.kt` (`NavyBlue`, `BorderGray`...) e os componentes que já existem. Não mude a identidade visual sem combinar com a equipe.
- **Estilo:** o projeto segue o estilo oficial do Kotlin (`kotlin.code.style=official` no `gradle.properties`). Use a formatação do Android Studio (Ctrl+Alt+L).

## Convenções de nomenclatura

- **Idioma:** os conceitos do domínio e os textos da interface ficam em português: `Imovel`, `Vistoria`, `abaSelecionada`, `rotulo`. Os nomes de campo dos DTOs ficam em inglês, como na API.
- **Arquivos e componentes:**
  - Telas: `<Nome>Screen.kt`, com o composable de mesmo nome.
  - Bottom sheets: `<Nome>Sheet.kt`.
  - Componentes compartilhados de uma funcionalidade: `<Funcionalidade>Shared.kt`.
- **Rotas:** constantes em `Routes`, em `snake_case`, com os parâmetros entre chaves (`ambiente/{ambienteId}`). Os ids são os UUIDs da API.
- **Enums que vão para a API:** têm `valorApi` com o valor exato aceito pela API, e `rotulo` para a interface.

## Tratamento de erros

- O repositório envolve cada chamada em `chamar { }`, que converte qualquer falha em `ApiErro` com uma mensagem para o usuário. A mensagem vem de `errors[].message` ou do `detail` do erro RFC 9457 da API.
- O `executar { }` do ViewModel transforma o `ApiErro` e as falhas fora da API (foto ilegível, PDF, câmera ausente) num aviso. O app não pode fechar por erro esperado.
- Não engula exceções em silêncio. A `CancellationException` sempre volta a ser lançada.
- Uma falha de rede não derruba a sessão. Só a recusa do refresh pela API leva ao login.

## Dependências

- Declare toda dependência nova em `gradle/libs.versions.toml` e use-a pelo alias (`libs.*`) no `build.gradle.kts`.
- Não atualize dependências em massa. As atualizações grandes (Kotlin 2.4, Compose BOM, Navigation 2.10, Retrofit 3 e OkHttp 5), que o lint aponta, mudam APIs e devem ir num PR próprio.
- Se uma dependência nova for usada por reflexão (como o Gson), inclua a regra no `src/main/keepRules/rules.keep`.

## Lint

O projeto usa só o lint do Android (`./gradlew lintDebug`). Não há ktlint, detekt nem outro formatador configurado.

- O PR não pode introduzir erros de lint.
- Hoje restam só avisos de versões mais novas disponíveis (`NewerVersionAvailable`, `GradleDependency`, `AndroidGradlePluginVersion`).
- O relatório fica em `build/reports/lint-results-debug.html`.

## Testes

```bash
./gradlew testDebugUnitTest            # testes unitários na JVM
./gradlew connectedDebugAndroidTest    # testes instrumentados (emulador ou aparelho conectado)
```

- **Testes unitários:** ficam em `src/test/java/com/visura/`, espelhando o pacote testado:
  - `model/AmbienteTest`: status do ambiente, `comItem`, `semItem` e `valorApi`;
  - `data/FormatacaoTest`: formatação de datas no fuso do aparelho;
  - `data/BuscaDeEnderecoTest`: interpretação da busca de endereço.
  
  Coloque aqui a lógica pura dos modelos e dos utilitários de `data/`.
- **Testes instrumentados:** ficam em `src/androidTest/`. Por enquanto, só existe o teste de exemplo do template. As dependências do Compose UI Test e do Espresso já estão declaradas.
- **Testes de integração com a API:** o app não tem. Os fluxos ponta a ponta são testados no backend (`*FlowTest`) e à mão, num emulador com a API local.
- **Antes do PR:** rode `./gradlew assembleDebug testDebugUnitTest lintDebug` e teste no emulador o fluxo que você mudou. Inclua a rotação de tela no teste quando a mudança tiver estado.

## Git e Pull Requests

Siga o fluxo do [CONTRIBUTING da raiz](../CONTRIBUTING.md#fluxo-de-trabalho). No app:

- Faça commits atômicos: a dependência nova em `build:`, a mudança de tela em `feat:` ou `fix:`, e a reorganização sem mudança de comportamento em `refactor:`.
- Na descrição do PR, informe o resultado dos comandos acima e o que foi testado no emulador.
