package com.visura.ui.screens.imoveis

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.data.ApiErro
import kotlinx.coroutines.delay
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray
import com.visura.ui.theme.clickableNoRipple

data class EnderecoSugerido(
    val rua: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val uf: String,
    val cep: String
) {
    val titulo: String get() = if (numero.isBlank()) rua.ifBlank { "CEP $cep" } else "$rua, $numero"
    val subtitulo: String get() = "$bairro, $cidade/$uf — $cep"
}

// A busca procura pelo CEP (8 dígitos) ou por "rua, cidade, UF". Os resultados não trazem o número:
// o usuário completa no formulário.
private fun buscaCompleta(texto: String): Boolean {
    val digitos = texto.filter { it.isDigit() }
    return (digitos.length == 8 && texto.none { it.isLetter() }) || texto.count { it == ',' } >= 2
}

@Composable
fun BuscarEnderecoOverlay(
    onClose: () -> Unit,
    onConfirm: (EnderecoSugerido) -> Unit,
    buscar: suspend (String) -> List<EnderecoSugerido>
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var selecionado by remember { mutableStateOf<EnderecoSugerido?>(null) }
    var resultados by remember { mutableStateOf(emptyList<EnderecoSugerido>()) }
    var buscando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onClose)

    // Espera o usuário parar de digitar antes de consultar o serviço de endereços
    LaunchedEffect(busca) {
        erro = null
        if (!buscaCompleta(busca)) {
            resultados = emptyList()
            return@LaunchedEffect
        }
        delay(600)
        buscando = true
        try {
            resultados = buscar(busca)
        } catch (e: ApiErro) {
            resultados = emptyList()
            erro = e.message
        } finally {
            buscando = false
        }
    }

    val escolhido = selecionado?.takeIf { it in resultados }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickableNoRipple { } // impede que toques atravessem para a tela de baixo
    ) {
        // Área superior escurecida, com a explicação da busca
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.45f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Endereços dos Correios, consultados no ViaCEP",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Fechar busca")
            }
        }

        // Painel inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Buscar endereço", fontSize = 17.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("CEP ou rua, cidade, UF") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextGray)
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LightBlueBg,
                    unfocusedContainerColor = LightBlueBg,
                    focusedBorderColor = NavyBlue,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                buscando -> LinearProgressIndicator(
                    color = NavyBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )

                erro != null -> Text(
                    erro.orEmpty(),
                    fontSize = 13.sp,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                !buscaCompleta(busca) -> Text(
                    "Digite o CEP ou o endereço no formato: rua, cidade, UF.\nEx.: Avenida Paulista, São Paulo, SP",
                    fontSize = 13.sp,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                resultados.isEmpty() -> Text(
                    "Nenhum endereço encontrado.",
                    fontSize = 13.sp,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(resultados) { endereco ->
                        ResultadoItem(
                            endereco = endereco,
                            selecionado = endereco == escolhido,
                            onClick = { selecionado = endereco }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { escolhido?.let(onConfirm) },
                enabled = escolhido != null,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Usar este endereço", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResultadoItem(
    endereco: EnderecoSugerido,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (selecionado) LightBlueBg else Color.White)
            .border(
                width = if (selecionado) 1.5.dp else 1.dp,
                color = if (selecionado) NavyBlue else BorderGray,
                shape = forma
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.LocationOn,
            contentDescription = null,
            tint = if (selecionado) NavyBlue else TextGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(endereco.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(endereco.subtitulo, fontSize = 12.sp, color = TextGray)
        }
    }
}