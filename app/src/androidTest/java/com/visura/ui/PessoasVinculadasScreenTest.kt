package com.visura.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visura.model.PapelPessoa
import com.visura.model.PessoaVinculada
import com.visura.ui.screens.pessoas.PessoasVinculadasScreen
import com.visura.ui.theme.VisuraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PessoasVinculadasScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val carla = PessoaVinculada("Carla Souza", PapelPessoa.PROPRIETARIO, "CPF 529.982.247-25", id = "p1")
    private val joao = PessoaVinculada("João Lima", PapelPessoa.INQUILINO, "CPF 111.444.777-35", id = "p2")

    @Test
    fun soContinuaComProprietarioEInquilino() {
        compose.setContent { VisuraTheme { PessoasVinculadasScreen(pessoasIniciais = listOf(carla)) } }

        compose.onNodeWithText("Continuar").assertIsNotEnabled()
        compose.onNodeWithText("Adicionar inquilino").assertExists()
    }

    @Test
    fun continuaComAsDuasPessoas() {
        var escolhidas: List<PessoaVinculada>? = null
        compose.setContent {
            VisuraTheme {
                PessoasVinculadasScreen(pessoasIniciais = listOf(carla, joao), onContinueClick = { escolhidas = it })
            }
        }

        compose.onNodeWithText("Continuar").assertIsEnabled().performClick()

        assertEquals(listOf(carla, joao), escolhidas)
    }

    @Test
    fun pessoasEscolhidasSobrevivemARecriacaoDaTela() {
        // Simula a recriação da Activity (girar a tela): a lista volta do estado salvo, não dos parâmetros
        val restauracao = StateRestorationTester(compose)
        var escolhidas: List<PessoaVinculada>? = null
        restauracao.setContent {
            VisuraTheme {
                PessoasVinculadasScreen(
                    pessoasIniciais = listOf(carla),
                    pessoasCadastradas = listOf(joao),
                    onContinueClick = { escolhidas = it }
                )
            }
        }
        compose.onNodeWithText("Pessoas cadastradas", useUnmergedTree = true).performClick()
        compose.onNodeWithText("João Lima", useUnmergedTree = true).performClick()

        restauracao.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("Continuar").assertIsEnabled().performClick()
        assertEquals(listOf("Carla Souza", "João Lima"), escolhidas?.map { it.nome })
    }
}
