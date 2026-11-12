package com.visura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.visura.data.ApiErro
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

private sealed interface Estado<out T> {
    data object Carregando : Estado<Nothing>
    data class Pronto<T>(val dados: T) : Estado<T>
    data class Erro(val mensagem: String) : Estado<Nothing>
}

// Carrega dados da API e só mostra o conteúdo quando eles chegam.
// Enquanto carrega, mostra um indicador; se falhar, mostra a mensagem e um botão para tentar de novo.
// O conteúdo recebe uma função para recarregar (ex.: depois de uma alteração).
@Composable
fun <T> Carregar(
    vararg chaves: Any?,
    carregar: suspend () -> T,
    conteudo: @Composable (dados: T, recarregar: () -> Unit) -> Unit
) {
    var tentativa by remember { mutableIntStateOf(0) }
    var estado by remember(*chaves) { mutableStateOf<Estado<T>>(Estado.Carregando) }

    LaunchedEffect(*chaves, tentativa) {
        // Na recarga, mantém o conteúdo atual na tela até os dados novos chegarem.
        if (estado !is Estado.Pronto) estado = Estado.Carregando
        estado = try {
            Estado.Pronto(carregar())
        } catch (e: ApiErro) {
            Estado.Erro(e.message ?: "Erro ao carregar os dados.")
        }
    }

    when (val atual = estado) {
        is Estado.Pronto -> conteudo(atual.dados) { tentativa++ }
        Estado.Carregando -> Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NavyBlue)
        }
        is Estado.Erro -> Column(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(atual.mensagem, color = TextGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { estado = Estado.Carregando; tentativa++ },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Tentar de novo")
            }
        }
    }
}
