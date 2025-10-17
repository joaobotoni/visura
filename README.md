# Visura: Plataforma de Vistoria Imobiliária - Android

Bem-vindo ao repositório do aplicativo Android **Visura**, uma plataforma dedicada à vistoria imobiliária. Este documento apresenta as principais tecnologias e técnicas utilizadas no desenvolvimento deste projeto.

##  Visão Geral do Projeto

O Visura é um aplicativo Android desenvolvido em Kotlin, criado para simplificar o processo de vistoria de imóveis. Ele foi construído usando uma abordagem moderna e organizada para garantir que seja fácil de usar e manter.

## Como o Aplicativo é Construído (Arquitetura)

O projeto Visura segue um modelo de construção chamado **MVVM (Model-View-ViewModel)**. Pense nele como uma forma de organizar o código para que diferentes partes do aplicativo cuidem de tarefas específicas:

-   **View (Tela)**: É o que você vê e interage no aplicativo.
-   **ViewModel (Lógica da Tela)**: Prepara os dados para a tela e lida com as ações que você faz.
-   **Model (Dados e Regras)**: É onde os dados são guardados e as regras de negócio funcionam.

Além disso, usamos um sistema de **Fluxo de Dados Unidirecional**, o que significa que as informações se movem de forma clara e organizada, sempre em uma direção, tornando o aplicativo mais previsível e fácil de entender.

### Estrutura do Código

O código é dividido em partes para facilitar o trabalho:

-   **Interface do Usuário (UI)**: Tudo que aparece na tela e como você interage com o aplicativo.
-   **Lógica de Negócio (Domínio)**: As regras e operações principais do aplicativo.
-   **Acesso a Dados (Dados)**: Como o aplicativo busca e guarda informações.

```mermaid
graph TD
    UI[O que você vê na tela] -->|Pede informações, envia ações| ViewModel[Prepara informações para a tela]
    ViewModel -->|Pede para fazer algo| UseCase[Lógica principal do aplicativo]
    UseCase -->|Pede dados| Repository[Organiza o acesso aos dados]
    Repository -->|Busca ou guarda dados| DataSource[Onde os dados estão (internet, banco de dados)]
```

## Tecnologias e Técnicas Usadas

O Visura é construído com ferramentas e técnicas modernas do universo Android:

| Tecnologia/Técnica | O que é e para que usamos |
| :--- | :--- |
| **Kotlin** | A linguagem de programação moderna e oficial para criar aplicativos Android. Usamos para escrever todo o código do aplicativo. |
| **Jetpack Compose** | Uma forma nova e fácil de criar a interface do usuário do aplicativo, tudo de forma visual e interativa. |
| **MVVM** | Uma maneira de organizar o código que ajuda a manter a interface do usuário separada da lógica do aplicativo. |
| **Kotlin Coroutines** | Permite que o aplicativo faça várias coisas ao mesmo tempo sem travar, como carregar dados da internet enquanto você usa a tela. |
| **Kotlin Flow** | Ajuda o aplicativo a lidar com informações que mudam com o tempo, como atualizações de dados em tempo real. |
| **Hilt (Injeção de Dependência)** | Uma ferramenta que facilita a conexão entre as diferentes partes do aplicativo, tornando-o mais flexível e fácil de testar. |
| **Firebase Authentication** | Um sistema do Google que gerencia o login e cadastro de usuários, seja por e-mail e senha ou usando sua conta Google. |

## Comandos Úteis para Desenvolvedores

Se você for um desenvolvedor, aqui estão alguns comandos que podem ajudar a trabalhar com o projeto:

### Limpar o Projeto
Este comando remove todos os arquivos temporários de compilações anteriores, deixando o projeto pronto para uma nova construção limpa.
```bash
./gradlew clean
```

### Compilar o Projeto
Este comando constrói o aplicativo, gerando os arquivos de instalação (APK) que podem ser usados em celulares Android.
```bash
./gradlew build
```

### Relatório de Assinatura
Mostra informações sobre como o aplicativo está assinado digitalmente, o que é importante para publicar na loja de aplicativos.
```bash
./gradlew signingReport
```

## 🤝 Contribuição

Sua ajuda é muito bem-vinda! Se tiver ideias, encontrar problemas ou quiser adicionar algo novo, fique à vontade para contribuir.


## 🔗 Referências
[Guia de Arquitetura de Apps Android](https://developer.android.com/topic/architecture)