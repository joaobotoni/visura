# Guia de Contribuição ao Visura

Bem-vindo(a) ao projeto **Visura**! Sua contribuição é extremamente valiosa para nós. Este guia detalha o processo de colaboração utilizando **Git** e **GitHub**, garantindo um fluxo de trabalho organizado e eficiente.

---

## Como Contribuir

### 1. Configuração Inicial do Ambiente

Para começar, você precisará configurar seu ambiente local:

| Passo | Ação | Comando |
| :--- | :--- | :--- |
| **1.1 Fork** | Crie uma cópia do repositório em sua conta no GitHub. | *(Ação no site do GitHub)* |
| **1.2 Clone** | Baixe o repositório para sua máquina local. | `git clone https://github.com/joaobotoni/visura.git` |
| **1.3 Acesso** | Navegue até o diretório do projeto. | `cd visura` |

### 2. Criação de uma Branch de Trabalho

Toda alteração deve ser feita em uma nova *branch* para manter a `main` limpa:

```bash
# Crie e mude para sua nova branch
git switch -c nome-da-sua-branch
```

> **Convenção de Nomenclatura:** Utilize nomes descritivos e prefixos claros, como:
> - `feature/nova-funcionalidade`
> - `fix/correcao-bug`
> - `docs/atualizacao-readme`

### 3. Desenvolvimento e Alterações

1.  Desenvolva suas alterações no código.
2.  **Siga o Estilo:** Mantenha-se fiel às diretrizes de estilo do projeto, se existirem.
3.  **Teste:** Certifique-se de que suas alterações foram testadas localmente e não introduziram novos problemas.

### 4. Commitando as Mudanças

Crie *commits* atômicos e com mensagens claras:

```bash
# Adicione todos os arquivos modificados
git add .

# Escreva uma mensagem de commit clara e concisa
git commit -m "Descrição clara e focada do que foi alterado"
```

**Exemplo de Commit Ideal:**
```bash
git commit -m "feat: Adiciona funcionalidade de login com OAuth"
```

### 5. Sincronização e Resolução de Conflitos

Antes de submeter, mantenha sua *branch* atualizada com o repositório original:

```bash
# Baixa as últimas alterações do repositório original
git fetch 

# Mescla as alterações na sua branch
git merge *****
```

Se houver conflitos, resolva-os e finalize a mesclagem:
```bash
# Após resolver os conflitos manualmente...
git commit
```

### 6. Revisão e Feedback

Aguarde a revisão da equipe. Esteja pronto(a) para responder a sugestões e fazer ajustes:

```bash
# Após fazer os ajustes solicitados...
git commit -m "Ajustes conforme feedback da revisão"
git push origin nome-da-sua-branch
```

---

## Diretrizes de Colaboração

*   **Código de Conduta:** Respeite todos os colaboradores.
*   **Documentação:** Se suas alterações afetarem a funcionalidade, atualize a documentação relevante.
*   **Commits Atômicos:** Cada *commit* deve ser focado em uma única mudança lógica.

Agradecemos novamente por dedicar seu tempo e esforço ao projeto Visura!



