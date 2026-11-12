# CLAUDE.md

Este arquivo orienta o Claude Code (claude.ai/code) ao trabalhar com o código deste repositório.

## Projeto

Visura é um app Android de módulo único (`:app`) para vistoria imobiliária, escrito em Kotlin com Jetpack Compose. Os textos exibidos ao usuário e os identificadores de conceitos do domínio estão em português (pt-BR): `Imovel`, `Vistoria`, `NovoImovelScreen`, `abaSelecionada`.

**O app é, por enquanto, um protótipo só de interface.** Não tem Hilt, Firebase, Supabase, ViewModels nem camadas de dados e domínio. O pacote, o namespace e o `applicationId` são todos `com.visura`, e o projeto raiz do Gradle se chama `Visura`. A versão anterior da `main`, com Hilt e Firebase, foi substituída por estas telas e continua só no histórico do git. A `feature/banco-supa` tem outra arquitetura, então não suponha que ela vale aqui.

## Comandos

Use `./gradlew` (Git Bash) ou `.\gradlew.bat` (PowerShell).

```bash
./gradlew assembleDebug            # gera o APK de debug
./gradlew installDebug             # instala no dispositivo/emulador conectado
./gradlew build                    # build completo, com lint e testes unitários
./gradlew lint                     # lint do Android
./gradlew testDebugUnitTest        # testes unitários na JVM (app/src/test)
./gradlew testDebugUnitTest --tests "com.visura.ExampleUnitTest"   # uma classe de teste
./gradlew connectedDebugAndroidTest # testes instrumentados (precisa de dispositivo)
```

Não é preciso configurar chaves no `local.properties` nem ter `google-services.json`. O build usa o AGP `9.3.0-alpha09`, então exige um Android Studio recente e compatível. Também usa compileSdk/targetSdk 37 e minSdk 24, com JVM target 11. As versões ficam em `gradle/libs.versions.toml`. Os únicos testes são os exemplos gerados pelo template.

## Arquitetura

Todo o código fica em `app/src/main/java/com/visura/ui/`:

- **Navegação** (`navigation/`): as rotas são constantes string no objeto `Routes`. Rotas com parâmetro têm uma função que monta o caminho, por exemplo `IMOVEL_DETALHE = "imovel_detalhe/{imovelId}"` e `Routes.imovelDetalhe(id)`. `VisuraNavGraph` é o único `NavHost`, e `LOGIN` é o destino inicial. As telas nunca recebem o `NavController`: elas expõem lambdas de callback (`onBackClick`, `onContinueClick`, …), e é o `NavGraph.kt` que liga esses callbacks à navegação. Os fluxos que terminam a autenticação usam `popUpTo(Routes.LOGIN) { inclusive = true }`, e o logout limpa a pilha com `popUpTo(0)`. Já existe a constante `Routes.IMOVEIS`, mas nenhum destino a registra no grafo.
- **Telas** (`screens/<funcionalidade>/`): não falam com nenhum backend. O estado é local do Compose (`remember { mutableStateOf(...) }`). A `HomeScreen` alterna entre as abas Home e Perfil com o índice `abaSelecionada` e uma `NavigationBar` inferior, sem navegação aninhada.
- **Dados fixos de exemplo fazem o papel de backend.** Os modelos do domínio são `data class`es simples, definidos junto às telas que os usam. `Imovel` e `PessoaVinculada` estão em `screens/imoveis/Imovel.kt`, `Vistoria` e `StatusVistoria` estão em `screens/home/Vistoria.kt`, e `TipoVistoria` está em `screens/vistoria/`. As listas `imoveisDeExemplo` e `vistoriasExemplo` são `val`s de nível superior, e `enderecosDeExemplo` (busca de endereço) é `private` em `BuscarEnderecoOverlay.kt`. O `NavGraph` busca o imóvel em `imoveisDeExemplo` pelo id para montar a tela de detalhe. Callbacks de formulário como `onContinueClick = { _, _ -> }` ainda descartam os dados digitados, e `NOVO_IMOVEL_TIPO` sempre leva ao primeiro imóvel de exemplo. A flag `SIMULAR_SEM_IMOVEIS` em `Imovel.kt` liga a tela de estado vazio. Ela afeta só `imoveisExemplo`, a lista que as telas exibem. Já `imoveisDeExemplo` é a lista completa que o `NavGraph` usa nas buscas. `imoveisExemplo` precisa continuar no fim do arquivo por causa da ordem de inicialização das `val`s de nível superior.
- **Tema** (`theme/`): `VisuraTheme` envolve o app no `MainActivity`, que também ativa o edge-to-edge com barras de sistema transparentes e claras. As telas usam principalmente as cores nomeadas de `Color.kt` (`NavyBlue`, `BorderGray`, `StatusOrangeBg`, …) direto, e não `MaterialTheme.colorScheme`. Os helpers compartilhados são `Modifier.clickableNoRipple` (`theme/Modifiers.kt`), além de `Modifier.cardBorder()` e `StatCard` (`screens/home/HomeShared.kt`, `internal`).

## Fluxo de Git

- Crie branches com os prefixos `feature/`, `fix/` ou `docs/`.
- Faça commits atômicos com prefixos convencionais (`feat: ...`). O histórico desta branch escreve o resto da mensagem em inglês, enquanto as outras branches usam português (`feat: Adiciona ...`).
