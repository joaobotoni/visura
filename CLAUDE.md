# CLAUDE.md

Este arquivo orienta o Claude Code (claude.ai/code) ao trabalhar com o código deste repositório.

## Projeto

Visura é um app Android de módulo único (`:app`) para vistoria imobiliária, escrito em Kotlin com Jetpack Compose. Os textos exibidos ao usuário e os identificadores de conceitos do domínio estão em português (pt-BR): `Imovel`, `Vistoria`, `NovoImovelScreen`, `abaSelecionada`.

**Esta branch (`feature/frontend-screens-navigation`) é um protótipo só de interface.** O código é separado do de `main`/`feature/banco-supa` e não tem Hilt, Firebase, Supabase, ViewModels nem camadas de dados e domínio. O pacote e o namespace são `com.example.firstproject`, e o projeto raiz do Gradle se chama `FirstProject`. Não suponha que a arquitetura das outras branches vale aqui.

## Comandos

Use `./gradlew` (Git Bash) ou `.\gradlew.bat` (PowerShell).

```bash
./gradlew assembleDebug            # gera o APK de debug
./gradlew installDebug             # instala no dispositivo/emulador conectado
./gradlew build                    # build completo, com lint e testes unitários
./gradlew lint                     # lint do Android
./gradlew testDebugUnitTest        # testes unitários na JVM (app/src/test)
./gradlew testDebugUnitTest --tests "com.example.firstproject.ExampleUnitTest"   # uma classe de teste
./gradlew connectedDebugAndroidTest # testes instrumentados (precisa de dispositivo)
```

Não é preciso configurar chaves no `local.properties` nem ter `google-services.json`. O build usa o AGP `9.3.0-alpha09`, então exige um Android Studio recente e compatível. Também usa compileSdk/targetSdk 37 e minSdk 24, com JVM target 11. As versões ficam em `gradle/libs.versions.toml`. Os únicos testes são os exemplos gerados pelo template.

## Arquitetura

Todo o código fica em `app/src/main/java/com/example/firstproject/ui/`:

- **Navegação** (`navigation/`): as rotas são constantes string no objeto `Routes`. Rotas com parâmetro têm uma função que monta o caminho, por exemplo `IMOVEL_DETALHE = "imovel_detalhe/{imovelId}"` e `Routes.imovelDetalhe(id)`. `FirstProjectNavGraph` é o único `NavHost`, e `LOGIN` é o destino inicial. As telas nunca recebem o `NavController`: elas expõem lambdas de callback (`onBackClick`, `onContinueClick`, …), e é o `NavGraph.kt` que liga esses callbacks à navegação. Os fluxos que terminam a autenticação usam `popUpTo(Routes.LOGIN) { inclusive = true }`, e o logout limpa a pilha com `popUpTo(0)`.
- **Telas** (`screens/<funcionalidade>/`): não falam com nenhum backend. O estado é local do Compose (`remember { mutableStateOf(...) }`). A `HomeScreen` alterna entre as abas Home e Perfil com o índice `abaSelecionada` e uma `NavigationBar` inferior, sem navegação aninhada.
- **Dados fixos de exemplo fazem o papel de backend.** Os modelos do domínio são `data class`es simples, definidos junto às telas que os usam. `Imovel` e `PessoaVinculada` estão em `screens/imoveis/Imovel.kt`, `Vistoria` e `StatusVistoria` estão em `screens/home/Vistoria.kt`, e `TipoVistoria` está em `screens/vistoria/`. As listas `imoveisDeExemplo`, `vistoriasExemplo` e `enderecosDeExemplo` (busca de endereço em `BuscarEnderecoOverlay`) são `val`s de nível superior. O `NavGraph` busca o imóvel em `imoveisDeExemplo` pelo id para montar a tela de detalhe. Callbacks de formulário como `onContinueClick = { _, _ -> }` ainda descartam os dados digitados, e `NOVO_IMOVEL_TIPO` sempre leva ao primeiro imóvel de exemplo. A flag `SIMULAR_SEM_IMOVEIS` em `Imovel.kt` liga a tela de estado vazio.
- **Tema** (`theme/`): `FirstProjectTheme` envolve o app no `MainActivity`, que também ativa o edge-to-edge com barras de sistema transparentes e claras. As telas usam principalmente as cores nomeadas de `Color.kt` (`NavyBlue`, `BorderGray`, `StatusOrangeBg`, …) direto, e não `MaterialTheme.colorScheme`. Os helpers compartilhados são `Modifier.clickableNoRipple` (`theme/Modifiers.kt`), além de `Modifier.cardBorder()` e `StatCard` (`screens/home/HomeShared.kt`, `internal`).

## Fluxo de Git

- Crie branches com os prefixos `feature/`, `fix/` ou `docs/`.
- Faça commits atômicos com prefixos convencionais (`feat: ...`). O histórico desta branch escreve o resto da mensagem em inglês, enquanto as outras branches usam português (`feat: Adiciona ...`).
