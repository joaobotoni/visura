package com.visura.ui

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visura.ui.screens.login.LoginScreen
import com.visura.ui.theme.VisuraTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var login: Pair<String, String>? = null
    private var criarConta = 0
    private var esqueciSenha = 0

    private fun abrir() = compose.setContent {
        VisuraTheme {
            LoginScreen(
                onLoginClick = { email, senha -> login = email to senha },
                onForgotPasswordClick = { esqueciSenha++ },
                onCreateAccountClick = { criarConta++ }
            )
        }
    }

    @Test
    fun entrarEnviaEmailSemEspacosESenha() {
        abrir()
        val campos = compose.onAllNodes(hasSetTextAction())
        campos[0].performTextInput("  maria@exemplo.com ")
        campos[1].performTextInput("Senha@123")

        compose.onNodeWithText("Entrar").performClick()

        assertEquals("maria@exemplo.com" to "Senha@123", login)
    }

    @Test
    fun camposVaziosAvisamENaoChamamOLogin() {
        abrir()

        compose.onNodeWithText("Entrar").performClick()

        compose.onNodeWithText("Informe o e-mail e a senha").assertExists()
        assertNull(login)
    }

    @Test
    fun linksDeCadastroERecuperacao() {
        abrir()

        compose.onNodeWithText("Criar conta").performClick()
        compose.onNodeWithText("Esqueci minha senha").performClick()

        assertEquals(1, criarConta)
        assertEquals(1, esqueciSenha)
    }
}
