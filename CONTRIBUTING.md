# Guia de contribuição do Visura

Este guia vale para o repositório inteiro. As orientações de cada módulo estão em [app/CONTRIBUTING.md](app/CONTRIBUTING.md) e [backend/CONTRIBUTING.md](backend/CONTRIBUTING.md).

## Sumário

- [Organização do repositório](#organização-do-repositório)
- [Configuração do ambiente](#configuração-do-ambiente)
- [Trabalhando nos módulos](#trabalhando-nos-módulos)
- [Fluxo de trabalho](#fluxo-de-trabalho)
- [Padrão de branches](#padrão-de-branches)
- [Conventional Commits](#conventional-commits)
- [Pull Requests](#pull-requests)
- [Checklist de revisão](#checklist-de-revisão)
- [Estratégia de testes](#estratégia-de-testes)

## Organização do repositório

| Pasta | Módulo | Build | Abre no |
|---|---|---|---|
| `app/` | App Android | Gradle (`app/gradlew`) | Android Studio, abrindo a pasta `app/` |
| `backend/` | API REST | Maven (`backend/mvnw`) | IntelliJ IDEA ou outra IDE Java, abrindo a pasta `backend/` |

Os dois módulos não compartilham código nem build. O ponto de contato entre eles é o contrato HTTP da API, descrito em [backend/README.md](backend/README.md#api) e consumido em `app/src/main/java/com/visura/data/remote/`.

## Configuração do ambiente

1. Clone o repositório:

   ```bash
   git clone https://github.com/joaobotoni/visura.git
   cd visura
   ```

2. Configure a sua identidade Git, com o e-mail vinculado à sua conta do GitHub:

   ```bash
   git config user.name "Seu Nome"
   git config user.email "seu-email@exemplo.com"
   ```

3. Prepare o backend: [backend/CONTRIBUTING.md](backend/CONTRIBUTING.md#ambiente).
4. Prepare o app: [app/CONTRIBUTING.md](app/CONTRIBUTING.md#ambiente).

## Trabalhando nos módulos

- Rode os comandos de cada módulo dentro da pasta dele: `cd app` ou `cd backend`.
- **Mudanças no contrato da API:** uma mudança que altera o contrato (rota, payload, código HTTP) toca os dois módulos. Faça as duas partes no mesmo PR, em commits separados: primeiro o backend, depois o app.
- **Documentação:** cada módulo documenta só o que é dele. O README da raiz traz apenas a visão geral e a integração.
- **Arquivos que não vão para o Git:**
  - Configuração local: `app/local.properties` e as variáveis de ambiente da API.
  - Saídas de build: `app/build/` e `backend/target/`.
  - Configurações de IDE: `.idea/`.

## Fluxo de trabalho

1. Atualize a `main`: `git switch main && git pull`.
2. Crie uma branch seguindo o [padrão](#padrão-de-branches): `git switch -c feature/nome-curto`.
3. Faça commits pequenos e atômicos, seguindo os [Conventional Commits](#conventional-commits).
4. Rode os testes do módulo alterado. Veja a [estratégia de testes](#estratégia-de-testes).
5. Traga a `main` para a sua branch antes de abrir o PR (`git fetch origin && git merge origin/main`) e resolva os conflitos.
6. Envie a branch (`git push -u origin feature/nome-curto`) e abra o Pull Request para a `main`.

Não faça commits direto na `main`. Não reescreva o histórico de uma branch que outra pessoa já usa. Se precisar reescrever, avise a equipe e use `git push --force-with-lease`, nunca `--force`.

## Padrão de branches

| Prefixo | Uso | Exemplo |
|---|---|---|
| `feature/` | Funcionalidade nova ou conjunto de melhorias | `feature/recuperacao-senha` |
| `fix/` | Correção de defeito | `fix/data-home-fuso` |
| `docs/` | Só documentação | `docs/readme-backend` |

As branches `backup/*` guardam versões antigas do projeto e não são referência para trabalho novo.

## Conventional Commits

O formato é `tipo: descrição`, com a descrição em português, no imperativo da terceira pessoa (`adiciona`, `corrige`), em minúscula e sem ponto final. Se precisar, acrescente um corpo depois de uma linha em branco, explicando o que mudou e por quê.

| Tipo | Quando usar |
|---|---|
| `feat` | Funcionalidade nova |
| `fix` | Correção de problema |
| `refactor` | Reorganização sem mudança de comportamento |
| `docs` | Só documentação |
| `test` | Testes novos ou ajustados |
| `build` | Build, dependências, Gradle ou Maven |
| `chore` | Manutenção e tarefas auxiliares |
| `perf` | Melhoria de desempenho |
| `style` | Formatação, sem mudança de lógica |

Exemplos reais do histórico:

```text
feat: adiciona recuperação de senha, login com Google e cor do item na API
refactor: centraliza o estado do fluxo no VisuraViewModel e move os modelos para model/
build: move o build do Android para dentro de app/
test: substitui o teste de exemplo por testes do modelo Ambiente
```

Cada commit deve compilar e conter uma única mudança lógica. Não junte uma dependência nova, uma refatoração e uma funcionalidade no mesmo commit.

## Pull Requests

- **Destino:** a `main`.
- **Título:** no mesmo formato dos commits.
- **Descrição:**
  - o que mudou e por quê;
  - como foi testado, com os comandos rodados e o resultado;
  - o que ficou pendente.
- **Contrato da API:** se a mudança alterar o contrato, avise na descrição e confirme que o app continua compatível.
- **Merge:** aguarde a revisão de outra pessoa da equipe antes do merge e responda os comentários com commits novos, sem reescrever o que já foi revisado.

## Checklist de revisão

- [ ] A branch e os commits seguem os padrões deste guia.
- [ ] O módulo alterado compila e os testes passam:
  - backend: `./mvnw test`;
  - app: `./gradlew assembleDebug testDebugUnitTest lintDebug connectedDebugAndroidTest`.
- [ ] Há testes para a regra nova ou para o defeito corrigido.
- [ ] Os erros viram respostas ou avisos claros, sem `catch` genérico que esconda a falha.
- [ ] Não há segredos, tokens, `local.properties` nem arquivos de build no diff.
- [ ] Se o contrato da API mudou, o app e o [README do backend](backend/README.md#api) foram atualizados.
- [ ] Mudança de banco entrou numa migration nova, sem alterar uma que já rodou.
- [ ] A documentação do módulo foi atualizada, quando o comportamento mudou.

## Estratégia de testes

| Nível | Backend | App |
|---|---|---|
| Unitário | Serviços com Mockito; value objects; filtros e segurança com MockMvc | Modelos (`model/`) e utilitários de `data/` na JVM |
| Banco | Procedures, triggers e permissões contra um PostgreSQL embutido | Não se aplica |
| Integração / fluxo | `*FlowTest`: a aplicação inteira contra um PostgreSQL embutido, com as migrations | Sem testes automatizados contra a API; os fluxos são testados à mão no emulador, com a API local |
| Instrumentado (aparelho) | Não se aplica | `src/androidTest/`: cifra e sessão no Android Keystore real e telas Compose |
| Estático | `ServicePatternTest` (forma dos serviços) | Lint do Android (`lintDebug`) |

Os testes do backend não precisam de Docker nem de banco instalado. Os do app na JVM não precisam de emulador.
