package com.example.firstproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.firstproject.ui.screens.ambientes.AmbientesScreen
import com.example.firstproject.ui.screens.ambientes.NovoAmbienteScreen
import com.example.firstproject.ui.screens.ambientes.ambientesDeExemplo
import com.example.firstproject.ui.screens.createaccount.CreateAccountDocsScreen
import com.example.firstproject.ui.screens.createaccount.CreateAccountInfoScreen
import com.example.firstproject.ui.screens.forgotpassword.ForgotPasswordCodeScreen
import com.example.firstproject.ui.screens.forgotpassword.ForgotPasswordEmailScreen
import com.example.firstproject.ui.screens.forgotpassword.ForgotPasswordNewScreen
import com.example.firstproject.ui.screens.googleauth.GoogleAccountPickerScreen
import com.example.firstproject.ui.screens.home.HomeScreen
import com.example.firstproject.ui.screens.imoveis.ImovelDetalheScreen
import com.example.firstproject.ui.screens.imoveis.NovoImovelScreen
import com.example.firstproject.ui.screens.imoveis.NovoImovelTipoScreen
import com.example.firstproject.ui.screens.imoveis.PessoaVinculada
import com.example.firstproject.ui.screens.imoveis.imoveisDeExemplo
import com.example.firstproject.ui.screens.login.LoginScreen
import com.example.firstproject.ui.screens.pessoas.PessoasVinculadasScreen
import com.example.firstproject.ui.screens.vistoria.NovaVistoriaScreen

@Composable
fun FirstProjectNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Vistoria em preenchimento. Fica acima do NavHost para que as etapas (pessoas, ambientes,
    // itens e revisão) enxerguem e alterem os mesmos dados.
    var pessoasDaVistoria by remember { mutableStateOf(emptyList<PessoaVinculada>()) }
    var ambientes by remember { mutableStateOf(ambientesDeExemplo()) }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD_EMAIL) },
                onCreateAccountClick = { navController.navigate(Routes.CREATE_ACCOUNT_INFO) },
                onGoogleClick = { navController.navigate(Routes.GOOGLE_ACCOUNT_PICKER) }
            )
        }

        composable(Routes.GOOGLE_ACCOUNT_PICKER) {
            GoogleAccountPickerScreen(
                onBackClick = { navController.popBackStack() },
                onAccountSelected = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD_EMAIL) {
            ForgotPasswordEmailScreen(
                onBackClick = { navController.popBackStack() },
                onCodeSent = { navController.navigate(Routes.FORGOT_PASSWORD_CODE) }
            )
        }

        composable(Routes.FORGOT_PASSWORD_CODE) {
            ForgotPasswordCodeScreen(
                onBackClick = { navController.popBackStack() },
                onCodeConfirmed = { navController.navigate(Routes.FORGOT_PASSWORD_NEW) }
            )
        }

        composable(Routes.FORGOT_PASSWORD_NEW) {
            ForgotPasswordNewScreen(
                onBackClick = { navController.popBackStack() },
                onPasswordChanged = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CREATE_ACCOUNT_INFO) {
            CreateAccountInfoScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Routes.CREATE_ACCOUNT_DOCS) }
            )
        }

        composable(Routes.CREATE_ACCOUNT_DOCS) {
            CreateAccountDocsScreen(
                onBackClick = { navController.popBackStack() },
                onFinishClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                // "+ Novo imóvel" sempre abre o cadastro
                onNovoImovelClick = { navController.navigate(Routes.NOVO_IMOVEL) },
                // Tocar numa vistoria abre as informações do imóvel dela
                onVistoriaClick = { vistoria ->
                    navController.navigate(Routes.imovelDetalhe(vistoria.imovelId))
                },
                onLogoutClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.IMOVEL_DETALHE,
            arguments = listOf(navArgument("imovelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val imovelId = backStackEntry.arguments?.getString("imovelId")
            val imovel = imoveisDeExemplo.find { it.id == imovelId }

            if (imovel != null) {
                ImovelDetalheScreen(
                    imovel = imovel,
                    onBackClick = { navController.popBackStack() },
                    onNovaVistoriaClick = { navController.navigate(Routes.novaVistoria(imovel.id)) }
                )
            }
        }

        // Cadastro do imóvel, etapa 1: endereço
        composable(Routes.NOVO_IMOVEL) {
            NovoImovelScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { _ ->
                    navController.navigate(Routes.NOVO_IMOVEL_TIPO)
                }
            )
        }

        // Cadastro do imóvel, etapa 2: tipo e categoria
        composable(Routes.NOVO_IMOVEL_TIPO) {
            NovoImovelTipoScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { _, _ ->
                    // Por enquanto abre os detalhes de um imóvel fixo de exemplo.
                    // O popUpTo remove as telas do cadastro, então a seta de voltar leva para a Home.
                    navController.navigate(Routes.imovelDetalhe(imoveisDeExemplo.first().id)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.NOVA_VISTORIA,
            arguments = listOf(navArgument("imovelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val imovelId = backStackEntry.arguments?.getString("imovelId").orEmpty()

            NovaVistoriaScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { _, _ ->
                    navController.navigate(Routes.pessoasVinculadas(imovelId))
                }
            )
        }

        // Vistoria, etapa 2: proprietário e inquilino (começa com as pessoas que o imóvel já tem)
        composable(
            route = Routes.PESSOAS_VINCULADAS,
            arguments = listOf(navArgument("imovelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val imovelId = backStackEntry.arguments?.getString("imovelId")
            val imovel = imoveisDeExemplo.find { it.id == imovelId }

            PessoasVinculadasScreen(
                pessoasIniciais = imovel?.pessoas.orEmpty(),
                onBackClick = { navController.popBackStack() },
                onContinueClick = { pessoas ->
                    // Começa a avaliação dos ambientes do zero para esta vistoria
                    pessoasDaVistoria = pessoas
                    ambientes = ambientesDeExemplo()
                    navController.navigate(Routes.ambientes(imovelId.orEmpty()))
                }
            )
        }

        // Vistoria, etapa 3: lista de ambientes
        composable(
            route = Routes.AMBIENTES,
            arguments = listOf(navArgument("imovelId") { type = NavType.StringType })
        ) {
            AmbientesScreen(
                ambientes = ambientes,
                onBackClick = { navController.popBackStack() },
                onAmbienteClick = {
                    // detalhe do ambiente — ainda não criado
                },
                onAdicionarAmbienteClick = { navController.navigate(Routes.NOVO_AMBIENTE) },
                onContinueClick = {
                    // próxima etapa: revisão — ainda não criada
                }
            )
        }

        composable(Routes.NOVO_AMBIENTE) {
            NovoAmbienteScreen(
                ambientesExistentes = ambientes,
                onBackClick = { navController.popBackStack() },
                onAdicionarClick = { novo ->
                    ambientes = ambientes + novo
                    navController.popBackStack()
                }
            )
        }
    }
}