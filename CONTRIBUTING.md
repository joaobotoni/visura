Guia de Contribuição
Bem-vindo ao projeto! Agradecemos seu interesse em contribuir. Este guia explica como você pode ajudar a melhorar o projeto usando Git. Siga os passos abaixo para garantir um processo de contribuição tranquilo e organizado.
Como Contribuir
1. Configurando o Ambiente

Fork o repositório: Clique no botão "Fork" no GitHub para criar uma cópia do repositório na sua conta.
Clone o repositório:git clone https://github.com/joaobotoni/visura.git


Acesse o diretório:cd NOME_DO_REPOSITORIO


Adicione o repositório original como upstream:git remote add upstream https://github.com/joaobotoni/visura.git



2. Criando uma Branch

Crie uma branch para sua feature ou correção:git switch -c nome-da-sua-branch


Use nomes descritivos, como feature/nova-funcionalidade ou fix/correcao-bug.

3. Fazendo Alterações

Faça suas alterações no código.
Certifique-se de seguir as diretrizes de estilo do projeto (se houver).
Teste suas alterações localmente antes de commitar.

4. Commitando Mudanças

Adicione os arquivos modificados:git add .


Escreva mensagens de commit claras e concisas:git commit -m "Descrição clara do que foi alterado"


Exemplo: git commit -m "Adiciona funcionalidade de login com OAuth"

5. Sincronizando com o Repositório Original

Antes de enviar suas alterações, atualize sua branch com o repositório: git fetch 

Resolva conflitos, se houver, e confirme as alterações:git commit

6. Enviando Suas Alterações

Envie sua branch para o seu fork:git push origin nome-da-sua-branch

Crie um Pull Request (PR) no GitHub:
Acesse seu fork no GitHub.
Clique em "Compare & pull request".
Descreva suas alterações no PR com detalhes, incluindo o propósito e o impacto.



7. Revisão e Feedback

Aguarde a revisão da equipe do projeto.
Responda a quaisquer comentários ou sugestões no PR.
Faça ajustes, se necessário, e commit novamente:git commit -m "Ajustes conforme feedback"
git push origin nome-da-sua-branch



Diretrizes Gerais

Siga o código de conduta: Respeite todos os colaboradores.
Documentação: Atualize a documentação, se necessário.
Commits atômicos: Faça commits pequenos e focados em uma única mudança.

