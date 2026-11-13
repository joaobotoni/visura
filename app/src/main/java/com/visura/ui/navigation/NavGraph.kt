package com.visura.ui.navigation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.visura.VisuraApplication
import androidx.core.content.FileProvider
import com.visura.data.ApiErro
import com.visura.data.ContaGoogle
import com.visura.data.gerarLaudo
import com.visura.data.loginComGoogleConfigurado
import com.visura.data.pedirContaGoogle
import com.visura.data.Perfil
import com.visura.data.VistoriaCompleta
import com.visura.data.prepararFoto
import com.visura.ui.components.Carregar
import com.visura.ui.screens.ambientes.Ambiente
import com.visura.ui.screens.ambientes.AmbienteDetalheScreen
import com.visura.ui.screens.ambientes.AmbientesScreen
import com.visura.ui.screens.ambientes.ItemScreen
import com.visura.ui.screens.ambientes.ModoItem
import com.visura.ui.screens.ambientes.NovoAmbienteScreen
import com.visura.ui.screens.ambientes.comItem
import com.visura.ui.screens.ambientes.semItem
import com.visura.ui.screens.createaccount.CreateAccountDocsScreen
import com.visura.ui.screens.createaccount.CreateAccountInfoScreen
import com.visura.ui.screens.createaccount.TipoPessoa
import com.visura.ui.screens.forgotpassword.ForgotPasswordCodeScreen
import com.visura.ui.screens.forgotpassword.ForgotPasswordEmailScreen
import com.visura.ui.screens.forgotpassword.ForgotPasswordNewScreen
import com.visura.ui.screens.home.HomeScreen
import com.visura.ui.screens.home.StatusVistoria
import com.visura.ui.screens.home.Vistoria
import com.visura.ui.screens.imoveis.EnderecoImovel
import com.visura.ui.screens.imoveis.ImovelDetalheScreen
import com.visura.ui.screens.imoveis.NovoImovelScreen
import com.visura.ui.screens.imoveis.NovoImovelTipoScreen
import com.visura.ui.screens.login.LoginScreen
import com.visura.ui.screens.pessoas.PessoasVinculadasScreen
import com.visura.ui.screens.vistoria.ConclusaoScreen
import com.visura.ui.screens.vistoria.NovaVistoriaScreen
import com.visura.ui.screens.vistoria.RevisaoScreen
import com.visura.ui.theme.NavyBlue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// Dados da primeira etapa do cadastro, guardados até a segunda etapa chamar a API
private data class ContaEmCadastro(val nome: String, val email: String, val senha: String)

private data class DadosDaHome(val perfil: Perfil, val vistorias: List<Vistoria>)

@Composable
fun VisuraNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val aplicacao = remember { context.applicationContext as VisuraApplication }
    val repositorio = aplicacao.repositorio
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Enquanto uma ação da API roda, a tela fica bloqueada e aparece uma barra de progresso no topo.
    var ocupado by remember { mutableStateOf(false) }

    fun avisar(mensagem: String) {
        scope.launch { snackbar.showSnackbar(mensagem) }
    }

    // Roda uma ação da API (salvar, enviar, finalizar...) e mostra o erro, se houver, num snackbar.
    fun executar(acao: suspend () -> Unit) {
        if (ocupado) return
        scope.launch {
            ocupado = true
            try {
                acao()
            } catch (e: ApiErro) {
                avisar(e.message ?: "Algo deu errado.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Erro fora da API (arquivo, câmera, PDF...): avisa em vez de fechar o app
                avisar("Erro inesperado: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                ocupado = false
            }
        }
    }

    // Gera o laudo em PDF no aparelho, com as fotos baixadas da API, e abre o compartilhamento
    suspend fun compartilharLaudo(vistoria: VistoriaCompleta) {
        val pdf = gerarLaudo(context, vistoria) { repositorio.baixarFoto(it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.arquivos", pdf)
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laudo de vistoria — ${vistoria.imovel?.nome.orEmpty()}")
            clipData = android.content.ClipData.newRawUri("laudo", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(envio, "Compartilhar laudo"))
    }

    // Já logado (há refresh token salvo): começa direto na Home.
    val inicio = remember { if (repositorio.sessao.logado) Routes.HOME else Routes.LOGIN }

    // Se o refresh token for recusado, a sessão acabou: volta para o login.
    LaunchedEffect(Unit) {
        repositorio.sessao.expirou.collect { expirou ->
            if (expirou) {
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                avisar("Sua sessão expirou. Entre novamente.")
            }
        }
    }

    // Estado compartilhado entre etapas
    var contaEmCadastro by remember { mutableStateOf<ContaEmCadastro?>(null) }
    // Recuperação de senha: e-mail e código conferido, guardados entre as três etapas
    var emailRecuperacao by rememberSaveable { mutableStateOf("") }
    var codigoRecuperacao by rememberSaveable { mutableStateOf("") }
    // Conta Google sem cadastro no Visura: nome e e-mail preenchem o cadastro
    var contaGoogle by remember { mutableStateOf<ContaGoogle?>(null) }
    var enderecoNovoImovel by remember { mutableStateOf<EnderecoImovel?>(null) }
    // Ambientes da vistoria aberta. Fica acima do NavHost para que ambientes e itens enxerguem os mesmos dados.
    var ambientes by remember { mutableStateOf(emptyList<Ambiente>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = inicio
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginClick = { email, senha ->
                        executar {
                            repositorio.entrar(email, senha)
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    },
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD_EMAIL) },
                    onCreateAccountClick = {
                        contaGoogle = null
                        navController.navigate(Routes.CREATE_ACCOUNT_INFO)
                    },
                    onGoogleClick = {
                        if (!loginComGoogleConfigurado) {
                            avisar("Login com Google não configurado: defina visura.google.client.id no local.properties.")
                        } else {
                            executar {
                                val conta = pedirContaGoogle(context) ?: return@executar
                                try {
                                    repositorio.entrarComGoogle(conta.idToken)
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                } catch (e: ApiErro) {
                                    if (e.status != 404) throw e
                                    // Sem conta no Visura: segue para o cadastro com nome e e-mail do Google
                                    contaGoogle = conta
                                    avisar(e.message ?: "Crie a sua conta para continuar.")
                                    navController.navigate(Routes.CREATE_ACCOUNT_INFO)
                                }
                            }
                        }
                    }
                )
            }

            // Recuperação de senha: a API manda um código de 6 dígitos para o e-mail, confere o código
            // e grava a nova senha. No fim, o app já entra com a senha nova.
            composable(Routes.FORGOT_PASSWORD_EMAIL) {
                ForgotPasswordEmailScreen(
                    emailInicial = emailRecuperacao,
                    onBackClick = { navController.popBackStack() },
                    onCodeSent = { email ->
                        executar {
                            repositorio.pedirCodigo(email)
                            emailRecuperacao = email
                            navController.navigate(Routes.FORGOT_PASSWORD_CODE)
                        }
                    }
                )
            }

            composable(Routes.FORGOT_PASSWORD_CODE) {
                ForgotPasswordCodeScreen(
                    email = emailRecuperacao,
                    onBackClick = { navController.popBackStack() },
                    onResendClick = {
                        executar {
                            repositorio.pedirCodigo(emailRecuperacao)
                            avisar("Enviamos um código novo. O anterior deixou de valer.")
                        }
                    },
                    onCodeConfirmed = { codigo ->
                        executar {
                            repositorio.conferirCodigo(emailRecuperacao, codigo)
                            codigoRecuperacao = codigo
                            navController.navigate(Routes.FORGOT_PASSWORD_NEW)
                        }
                    }
                )
            }

            composable(Routes.FORGOT_PASSWORD_NEW) {
                ForgotPasswordNewScreen(
                    onBackClick = { navController.popBackStack() },
                    onPasswordChanged = { senha ->
                        executar {
                            val email = emailRecuperacao
                            repositorio.redefinirSenha(email, codigoRecuperacao, senha)
                            emailRecuperacao = ""
                            codigoRecuperacao = ""
                            // A senha já mudou e o código não vale mais: se o login falhar, segue para a tela de login
                            val destino = try {
                                repositorio.entrar(email, senha)
                                avisar("Senha alterada. Você já está conectado.")
                                Routes.HOME
                            } catch (e: ApiErro) {
                                avisar("Senha alterada. Entre com a nova senha.")
                                Routes.LOGIN
                            }
                            navController.navigate(destino) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.CREATE_ACCOUNT_INFO) {
                CreateAccountInfoScreen(
                    nomeInicial = contaGoogle?.nome.orEmpty(),
                    emailInicial = contaGoogle?.email.orEmpty(),
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { nome, email, senha ->
                        contaEmCadastro = ContaEmCadastro(nome, email, senha)
                        navController.navigate(Routes.CREATE_ACCOUNT_DOCS)
                    }
                )
            }

            composable(Routes.CREATE_ACCOUNT_DOCS) {
                CreateAccountDocsScreen(
                    nomeInicial = contaEmCadastro?.nome.orEmpty(),
                    onBackClick = { navController.popBackStack() },
                    onFinishClick = { docs ->
                        val conta = contaEmCadastro
                        if (conta == null) {
                            avisar("Os dados da primeira etapa se perderam. Preencha de novo, por favor.")
                            navController.popBackStack()
                            return@CreateAccountDocsScreen
                        }
                        executar {
                            repositorio.registrar(docs.nome, docs.cpf, conta.email, conta.senha)
                            contaEmCadastro = null
                            contaGoogle = null
                            // A conta já existe; se a empresa falhar, segue para a Home e avisa.
                            val papel = docs.papel
                            if (docs.tipoPessoa == TipoPessoa.JURIDICA && papel != null) {
                                try {
                                    repositorio.cadastrarEmpresa(docs.razaoSocial, docs.nomeFantasia, docs.cnpj, papel.valorApi)
                                } catch (e: ApiErro) {
                                    avisar("Conta criada, mas a empresa não foi cadastrada: ${e.message}")
                                }
                            }
                            navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                Carregar(
                    carregar = {
                        coroutineScope {
                            val perfil = async { repositorio.perfil() }
                            val vistorias = async { repositorio.vistoriasDaHome() }
                            DadosDaHome(perfil.await(), vistorias.await())
                        }
                    }
                ) { dados, _ ->
                    HomeScreen(
                        nomeUsuario = dados.perfil.nome,
                        email = dados.perfil.email,
                        cpf = dados.perfil.cpf,
                        cnpj = dados.perfil.cnpj,
                        vistorias = dados.vistorias,
                        onNovoImovelClick = { navController.navigate(Routes.NOVO_IMOVEL) },
                        // Tocar numa vistoria abre as informações do imóvel dela
                        onVistoriaClick = { vistoria ->
                            navController.navigate(Routes.imovelDetalhe(vistoria.imovelId))
                        },
                        onLogoutClick = {
                            executar {
                                repositorio.sair()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }

            composable(
                route = Routes.IMOVEL_DETALHE,
                arguments = listOf(navArgument("imovelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val imovelId = backStackEntry.arguments?.getString("imovelId").orEmpty()

                Carregar(imovelId, carregar = { repositorio.imovel(imovelId) }) { imovel, _ ->
                    ImovelDetalheScreen(
                        imovel = imovel,
                        onBackClick = { navController.popBackStack() },
                        onNovaVistoriaClick = { navController.navigate(Routes.novaVistoria(imovel.id)) },
                        // Vistoria em andamento: retoma a partir das pessoas vinculadas.
                        // Concluída: gera o laudo de novo (as fotos e os dados vêm da API).
                        onVistoriaClick = { vistoria ->
                            if (vistoria.status == StatusVistoria.EM_ANDAMENTO) {
                                navController.navigate(Routes.pessoasVinculadas(imovel.id, vistoria.id))
                            } else {
                                executar { compartilharLaudo(repositorio.vistoriaCompleta(vistoria.id)) }
                            }
                        }
                    )
                }
            }

            // Cadastro do imóvel, etapa 1: endereço
            composable(Routes.NOVO_IMOVEL) {
                NovoImovelScreen(
                    buscarEnderecos = { aplicacao.buscaDeEndereco.buscar(it) },
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { endereco ->
                        enderecoNovoImovel = endereco
                        navController.navigate(Routes.NOVO_IMOVEL_TIPO)
                    }
                )
            }

            // Cadastro do imóvel, etapa 2: tipo e categoria. Aqui o imóvel é criado na API.
            composable(Routes.NOVO_IMOVEL_TIPO) {
                NovoImovelTipoScreen(
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { tipo, categoria ->
                        val endereco = enderecoNovoImovel
                        if (endereco == null) {
                            avisar("O endereço se perdeu. Preencha de novo, por favor.")
                            navController.popBackStack()
                            return@NovoImovelTipoScreen
                        }
                        executar {
                            val id = repositorio.cadastrarImovel(endereco, tipo, categoria)
                            enderecoNovoImovel = null
                            // O popUpTo remove as telas do cadastro, então a seta de voltar leva para a Home.
                            navController.navigate(Routes.imovelDetalhe(id)) {
                                popUpTo(Routes.HOME)
                            }
                        }
                    }
                )
            }

            // Vistoria, etapa 1: tipo e observações. Aqui a vistoria é iniciada na API.
            composable(
                route = Routes.NOVA_VISTORIA,
                arguments = listOf(navArgument("imovelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val imovelId = backStackEntry.arguments?.getString("imovelId").orEmpty()

                NovaVistoriaScreen(
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { tipo, observacoes ->
                        executar {
                            val vistoriaId = repositorio.iniciarVistoria(imovelId, tipo, observacoes)
                            // Tira esta etapa da pilha para o "voltar" não criar outra vistoria
                            navController.navigate(Routes.pessoasVinculadas(imovelId, vistoriaId)) {
                                popUpTo(Routes.NOVA_VISTORIA) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Vistoria, etapa 2: proprietário e inquilino.
            // Começa com as pessoas que a vistoria já tem ou, numa vistoria nova, com os proprietários do imóvel.
            composable(
                route = Routes.PESSOAS_VINCULADAS,
                arguments = listOf(
                    navArgument("imovelId") { type = NavType.StringType },
                    navArgument("vistoriaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val imovelId = backStackEntry.arguments?.getString("imovelId").orEmpty()
                val vistoriaId = backStackEntry.arguments?.getString("vistoriaId").orEmpty()

                Carregar(
                    vistoriaId,
                    carregar = {
                        coroutineScope {
                            val daVistoria = async { repositorio.pessoasDaVistoria(vistoriaId) }
                            val donos = async { repositorio.proprietariosDoImovel(imovelId) }
                            val cadastradas = async { repositorio.pessoasCadastradas() }
                            val iniciais = daVistoria.await().ifEmpty { donos.await() }
                            iniciais to cadastradas.await()
                        }
                    }
                ) { (iniciais, cadastradas), _ ->
                    PessoasVinculadasScreen(
                        pessoasIniciais = iniciais,
                        pessoasCadastradas = cadastradas,
                        onCadastrarPessoa = { pessoa, cpf, cnpj, onCriada ->
                            executar { onCriada(repositorio.cadastrarPessoa(pessoa, cpf, cnpj)) }
                        },
                        onBackClick = { navController.popBackStack() },
                        onContinueClick = { pessoas ->
                            executar {
                                repositorio.vincularPessoas(vistoriaId, imovelId, pessoas)
                                navController.navigate(Routes.ambientes(vistoriaId))
                            }
                        }
                    )
                }
            }

            // Vistoria, etapa 3: lista de ambientes (sempre recarregada da API ao abrir)
            composable(
                route = Routes.AMBIENTES,
                arguments = listOf(navArgument("vistoriaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vistoriaId = backStackEntry.arguments?.getString("vistoriaId").orEmpty()

                Carregar(
                    vistoriaId,
                    carregar = { repositorio.ambientes(vistoriaId).also { ambientes = it } }
                ) { _, _ ->
                    AmbientesScreen(
                        ambientes = ambientes,
                        onBackClick = { navController.popBackStack() },
                        onAmbienteClick = { navController.navigate(Routes.ambienteDetalhe(it.id)) },
                        onRemoverAmbienteClick = { ambiente ->
                            executar {
                                repositorio.removerAmbiente(ambiente.id)
                                ambientes = ambientes.filterNot { it.id == ambiente.id }
                            }
                        },
                        onAdicionarAmbienteClick = { navController.navigate(Routes.novoAmbiente(vistoriaId)) },
                        onContinueClick = { navController.navigate(Routes.revisao(vistoriaId)) }
                    )
                }
            }

            composable(
                route = Routes.NOVO_AMBIENTE,
                arguments = listOf(navArgument("vistoriaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vistoriaId = backStackEntry.arguments?.getString("vistoriaId").orEmpty()

                Carregar(carregar = { repositorio.tiposDeAmbiente() }) { tipos, _ ->
                    NovoAmbienteScreen(
                        ambientesExistentes = ambientes,
                        tiposDeAmbiente = tipos,
                        onBackClick = { navController.popBackStack() },
                        onAdicionarClick = { nome, tipo, observacoes, duplicarDe ->
                            executar {
                                val novo = repositorio.adicionarAmbiente(vistoriaId, nome, tipo?.id, observacoes, duplicarDe)
                                ambientes = ambientes + novo
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }

            composable(
                route = Routes.AMBIENTE_DETALHE,
                arguments = listOf(navArgument("ambienteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ambiente = ambientes.find { it.id == backStackEntry.arguments?.getString("ambienteId") }

                if (ambiente != null) {
                    AmbienteDetalheScreen(
                        ambiente = ambiente,
                        onBackClick = { navController.popBackStack() },
                        onItemClick = { navController.navigate(Routes.itemAvaliar(ambiente.id, it.id)) },
                        onAdicionarItemClick = { navController.navigate(Routes.novoItem(ambiente.id)) },
                        onEditarItemClick = { navController.navigate(Routes.itemEditar(ambiente.id, it.id)) },
                        onRemoverItemClick = { item ->
                            executar {
                                repositorio.removerItem(item.id)
                                ambientes = ambientes.semItem(ambiente.id, item.id)
                            }
                        }
                    )
                } else {
                    VoltarParaAmbientes(navController)
                }
            }

            composable(
                route = Routes.NOVO_ITEM,
                arguments = listOf(navArgument("ambienteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ambiente = ambientes.find { it.id == backStackEntry.arguments?.getString("ambienteId") }

                if (ambiente != null) {
                    ItemScreen(
                        modo = ModoItem.NOVO,
                        nomeDoAmbiente = ambiente.nome,
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { item, fotos ->
                            executar {
                                val criado = repositorio.salvarItem(ambiente.id, item.copy(fotos = 0), novo = true)
                                ambientes = ambientes.comItem(ambiente.id, criado)
                                // O item já existe; uma foto que falhar não desfaz o item, só avisa
                                val enviadas = mutableListOf<String>()
                                try {
                                    fotos.forEach { uri ->
                                        enviadas += repositorio.enviarFoto(ambiente.id, criado.id, prepararFoto(context, uri))
                                    }
                                } catch (e: ApiErro) {
                                    avisar("Item salvo, mas ${fotos.size - enviadas.size} foto(s) não foram enviadas: ${e.message}")
                                }
                                ambientes = ambientes.comItem(
                                    ambiente.id,
                                    criado.copy(fotos = enviadas.size, fotosIds = enviadas)
                                )
                                navController.popBackStack()
                            }
                        }
                    )
                } else {
                    VoltarParaAmbientes(navController)
                }
            }

            // Avaliar e editar usam a mesma tela; só muda o modo
            listOf(Routes.ITEM_AVALIAR to ModoItem.AVALIAR, Routes.ITEM_EDITAR to ModoItem.EDITAR).forEach { (rota, modo) ->
                composable(
                    route = rota,
                    arguments = listOf(
                        navArgument("ambienteId") { type = NavType.StringType },
                        navArgument("itemId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val ambiente = ambientes.find { it.id == backStackEntry.arguments?.getString("ambienteId") }
                    val item = ambiente?.itens?.find { it.id == backStackEntry.arguments?.getString("itemId") }

                    if (ambiente != null && item != null) {
                        ItemScreen(
                            modo = modo,
                            nomeDoAmbiente = ambiente.nome,
                            itemInicial = item,
                            onBackClick = { navController.popBackStack() },
                            onSaveClick = { atualizado, _ ->
                                executar {
                                    val salvo = repositorio.salvarItem(ambiente.id, atualizado, novo = false)
                                    ambientes = ambientes.comItem(ambiente.id, salvo)
                                    navController.popBackStack()
                                }
                            },
                            onFotoEscolhida = { uri, onEnviada ->
                                executar {
                                    val fotoId = repositorio.enviarFoto(ambiente.id, item.id, prepararFoto(context, uri))
                                    onEnviada()
                                    // Atualiza a contagem também na lista, caso o usuário volte sem salvar
                                    ambientes.find { it.id == ambiente.id }?.itens?.find { it.id == item.id }?.let { atual ->
                                        ambientes = ambientes.comItem(
                                            ambiente.id,
                                            atual.copy(fotos = atual.fotos + 1, fotosIds = atual.fotosIds + fotoId)
                                        )
                                    }
                                    avisar("Foto enviada.")
                                }
                            }
                        )
                    } else {
                        VoltarParaAmbientes(navController)
                    }
                }
            }

            // Vistoria, etapa 4: revisão antes de finalizar
            composable(
                route = Routes.REVISAO,
                arguments = listOf(navArgument("vistoriaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vistoriaId = backStackEntry.arguments?.getString("vistoriaId").orEmpty()

                Carregar(vistoriaId, carregar = { repositorio.vistoriaCompleta(vistoriaId) }) { vistoria, _ ->
                    RevisaoScreen(
                        imovel = vistoria.imovel,
                        pessoas = vistoria.pessoas,
                        ambientes = vistoria.ambientes,
                        onBackClick = { navController.popBackStack() },
                        onFinalizarClick = {
                            executar {
                                repositorio.finalizarVistoria(vistoriaId)
                                // Tira as etapas da pilha: depois da conclusão, voltar leva para a Home
                                navController.navigate(Routes.conclusao(vistoriaId)) {
                                    popUpTo(Routes.HOME)
                                }
                            }
                        }
                    )
                }
            }

            composable(
                route = Routes.CONCLUSAO,
                arguments = listOf(navArgument("vistoriaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vistoriaId = backStackEntry.arguments?.getString("vistoriaId").orEmpty()

                Carregar(vistoriaId, carregar = { repositorio.vistoriaCompleta(vistoriaId) }) { vistoria, _ ->
                    ConclusaoScreen(
                        onFinalizarClick = { navController.popBackStack(Routes.HOME, inclusive = false) },
                        onFinalizarECompartilharClick = {
                            // Gera o laudo em PDF no aparelho, com as fotos baixadas da API, e abre o compartilhamento
                            executar {
                                compartilharLaudo(vistoria)
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            }
                        }
                    )
                }
            }
        }

        if (ocupado) {
            // Bloqueia toques enquanto a ação roda, para não enviar duas vezes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.4f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            )
            LinearProgressIndicator(
                color = NavyBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        )
    }
}

// Ambiente ou item que não está mais na memória (o Android encerrou o app em segundo plano):
// volta uma tela por vez até a lista de ambientes, que recarrega da API.
@Composable
private fun VoltarParaAmbientes(navController: NavHostController) {
    LaunchedEffect(Unit) { navController.popBackStack() }
}
