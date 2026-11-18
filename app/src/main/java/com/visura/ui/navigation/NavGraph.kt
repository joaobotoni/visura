package com.visura.ui.navigation

import android.content.ActivityNotFoundException
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.visura.data.ApiErro
import com.visura.data.loginComGoogleConfigurado
import com.visura.data.pedirContaGoogle
import com.visura.model.Perfil
import com.visura.model.StatusVistoria
import com.visura.model.Vistoria
import com.visura.ui.components.Carregar
import com.visura.data.EnviosDeNarracao
import com.visura.ui.screens.ambientes.AmbienteDetalheScreen
import com.visura.ui.screens.ambientes.AmbientesScreen
import com.visura.ui.screens.ambientes.ItemScreen
import com.visura.ui.screens.ambientes.ModoItem
import com.visura.ui.screens.ambientes.NovoAmbienteScreen
import com.visura.ui.screens.createaccount.CreateAccountDocsScreen
import com.visura.ui.screens.createaccount.CreateAccountInfoScreen
import com.visura.ui.screens.forgotpassword.ForgotPasswordCodeScreen
import com.visura.ui.screens.forgotpassword.ForgotPasswordEmailScreen
import com.visura.ui.screens.forgotpassword.ForgotPasswordNewScreen
import com.visura.ui.screens.home.HomeScreen
import com.visura.ui.screens.imoveis.ImovelDetalheScreen
import com.visura.ui.screens.imoveis.NovoImovelScreen
import com.visura.ui.screens.imoveis.NovoImovelTipoScreen
import com.visura.ui.screens.login.LoginScreen
import com.visura.ui.screens.pessoas.PessoasVinculadasScreen
import com.visura.ui.screens.vistoria.ConclusaoScreen
import com.visura.ui.screens.vistoria.NovaVistoriaScreen
import com.visura.ui.screens.vistoria.RevisaoScreen
import com.visura.ui.theme.NavyBlue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private data class DadosDaHome(val perfil: Perfil, val vistorias: List<Vistoria>)

// Único NavHost do app. Liga cada tela ao VisuraViewModel, que guarda o estado do fluxo e chama a API,
// e executa aqui o que depende da tela: navegar, mostrar avisos e abrir o compartilhamento.
@Composable
fun VisuraNavGraph(
    navController: NavHostController = rememberNavController(),
    vm: VisuraViewModel = viewModel(factory = VisuraViewModel.Fabrica)
) {
    val context = LocalContext.current
    val repositorio = vm.repositorio
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val ocupado by vm.ocupado.collectAsStateWithLifecycle()
    val ambientes by vm.ambientes.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Eventos só são consumidos com a tela visível: um evento que chega com o app em segundo plano
    // espera no Channel do ViewModel em vez de navegar uma tela parada.
    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.eventos.collect { evento ->
                when (evento) {
                    // Em launch: o aviso não segura os eventos seguintes enquanto está na tela
                    is Evento.Aviso -> launch { snackbar.showSnackbar(evento.mensagem) }
                    is Evento.Navegar -> evento.comando(navController)
                    is Evento.Compartilhar -> try {
                        context.startActivity(Intent.createChooser(evento.envio, "Compartilhar laudo"))
                    } catch (_: ActivityNotFoundException) {
                        vm.avisar("Nenhum app para compartilhar o laudo.")
                    }
                }
            }
        }
    }

    // Já logado (há refresh token salvo): começa direto na Home.
    val inicio = remember { if (vm.logado) Routes.HOME else Routes.LOGIN }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = inicio
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginClick = vm::entrar,
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD_EMAIL) },
                    onCreateAccountClick = {
                        vm.novaConta()
                        navController.navigate(Routes.CREATE_ACCOUNT_INFO)
                    },
                    onGoogleClick = {
                        if (!loginComGoogleConfigurado) {
                            vm.avisar("Login com Google não configurado: defina visura.google.client.id no local.properties.")
                        } else {
                            // O seletor de contas precisa da Activity: roda no escopo da tela, e só o login vai ao ViewModel
                            scope.launch {
                                try {
                                    pedirContaGoogle(context)?.let(vm::entrarComGoogle)
                                } catch (e: ApiErro) {
                                    vm.avisar(e.message ?: "Não foi possível entrar com o Google.")
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
                    emailInicial = vm.emailRecuperacao,
                    onBackClick = { navController.popBackStack() },
                    onCodeSent = vm::pedirCodigo
                )
            }

            composable(Routes.FORGOT_PASSWORD_CODE) {
                ForgotPasswordCodeScreen(
                    email = vm.emailRecuperacao,
                    onBackClick = { navController.popBackStack() },
                    onResendClick = vm::reenviarCodigo,
                    onCodeConfirmed = vm::conferirCodigo
                )
            }

            composable(Routes.FORGOT_PASSWORD_NEW) {
                ForgotPasswordNewScreen(
                    onBackClick = { navController.popBackStack() },
                    onPasswordChanged = vm::redefinirSenha
                )
            }

            composable(Routes.CREATE_ACCOUNT_INFO) {
                CreateAccountInfoScreen(
                    nomeInicial = vm.contaGoogle?.nome.orEmpty(),
                    emailInicial = vm.contaGoogle?.email.orEmpty(),
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = vm::continuarCadastro
                )
            }

            composable(Routes.CREATE_ACCOUNT_DOCS) {
                CreateAccountDocsScreen(
                    nomeInicial = vm.nomeEmCadastro,
                    onBackClick = { navController.popBackStack() },
                    onFinishClick = vm::registrar
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
                        onLogoutClick = vm::sair
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
                                vm.compartilharLaudo(vistoria.id)
                            }
                        }
                    )
                }
            }

            // Cadastro do imóvel, etapa 1: endereço
            composable(Routes.NOVO_IMOVEL) {
                NovoImovelScreen(
                    buscarEnderecos = { vm.buscaDeEndereco.buscar(it) },
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = vm::continuarImovel
                )
            }

            // Cadastro do imóvel, etapa 2: tipo e categoria. Aqui o imóvel é criado na API.
            composable(Routes.NOVO_IMOVEL_TIPO) {
                NovoImovelTipoScreen(
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = vm::cadastrarImovel
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
                    onContinueClick = { tipo, observacoes -> vm.iniciarVistoria(imovelId, tipo, observacoes) }
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
                        onCadastrarPessoa = vm::cadastrarPessoa,
                        onBackClick = { navController.popBackStack() },
                        onContinueClick = { pessoas -> vm.vincularPessoas(vistoriaId, imovelId, pessoas) }
                    )
                }
            }

            // Vistoria, etapa 3: lista de ambientes (sempre recarregada da API ao abrir)
            composable(
                route = Routes.AMBIENTES,
                arguments = listOf(navArgument("vistoriaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vistoriaId = backStackEntry.arguments?.getString("vistoriaId").orEmpty()

                Carregar(vistoriaId, carregar = { vm.carregarAmbientes(vistoriaId) }) { _, _ ->
                    AmbientesScreen(
                        ambientes = ambientes,
                        onBackClick = { navController.popBackStack() },
                        onAmbienteClick = { navController.navigate(Routes.ambienteDetalhe(it.id)) },
                        onRemoverAmbienteClick = { vm.removerAmbiente(it.id) },
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
                            vm.adicionarAmbiente(vistoriaId, nome, tipo, observacoes, duplicarDe)
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
                    val gravacao by vm.gravacao.collectAsStateWithLifecycle()
                    val envios by remember(ambiente.id) { vm.enviosDeNarracao(ambiente.id) }
                        .collectAsStateWithLifecycle(EnviosDeNarracao())
                    AmbienteDetalheScreen(
                        ambiente = ambiente,
                        onBackClick = { navController.popBackStack() },
                        onItemClick = { navController.navigate(Routes.itemAvaliar(ambiente.id, it.id)) },
                        onAdicionarItemClick = { navController.navigate(Routes.novoItem(ambiente.id)) },
                        onEditarItemClick = { navController.navigate(Routes.itemEditar(ambiente.id, it.id)) },
                        onRemoverItemClick = { item -> vm.removerItem(ambiente.id, item.id) },
                        gravacao = gravacao,
                        envios = envios,
                        onGravar = { vm.gravarNarracao(ambiente) },
                        onPausar = vm::pausarNarracao,
                        onContinuar = vm::continuarNarracao,
                        onConcluir = vm::concluirNarracao,
                        onDescartar = vm::descartarNarracao
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
                        onSaveClick = { item, fotos -> vm.criarItem(ambiente.id, item, fotos) }
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
                            onSaveClick = { atualizado, _ -> vm.salvarItem(ambiente.id, atualizado) },
                            onFotoEscolhida = { uri, onEnviada -> vm.enviarFoto(ambiente.id, item.id, uri, onEnviada) },
                            carregarFoto = vm::miniatura
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
                        onFinalizarClick = { vm.finalizarVistoria(vistoriaId) }
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
                        onFinalizarECompartilharClick = { vm.compartilharLaudoEVoltar(vistoria) }
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
