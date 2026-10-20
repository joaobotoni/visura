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
    val titulo: String get() = "$rua, $numero"
    val subtitulo: String get() = "$bairro, $cidade/$uf — $cep"
}

// SIMULAÇÃO — será substituída pela busca real (mapa / serviço de endereços).
private val enderecosDeExemplo = listOf(
    EnderecoSugerido("Rua das Palmeiras", "210", "Água Verde", "Curitiba", "PR", "82010-330"),
    EnderecoSugerido("Rua das Palmeiras", "340", "Água Verde", "Curitiba", "PR", "82010-330"),
    EnderecoSugerido("Alameda das Palmeiras", "12", "Batel", "Curitiba", "PR", "80420-000")
)

private val CorMapaProvisoria = Color(0xFF8E9196)

@Composable
fun BuscarEnderecoOverlay(
    onClose: () -> Unit,
    onConfirm: (EnderecoSugerido) -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var selecionado by remember { mutableStateOf<EnderecoSugerido?>(null) }

    BackHandler(onBack = onClose)

    // Simulação: procura qualquer palavra (4+ letras) da busca na rua ou no bairro
    val resultados = remember(busca) {
        val palavras = busca.trim().split(" ").filter { it.length >= 4 }
        if (palavras.isEmpty()) {
            emptyList()
        } else {
            enderecosDeExemplo.filter { e ->
                palavras.any { p ->
                    e.rua.contains(p, ignoreCase = true) || e.bairro.contains(p, ignoreCase = true)
                }
            }
        }
    }

    val escolhido = selecionado?.takeIf { it in resultados }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickableNoRipple { } // impede que toques atravessem para a tela de baixo
    ) {
        // Área do mapa (provisória)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CorMapaProvisoria)
        ) {
            Text(
                "Mapa",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center)
            )

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
                placeholder = { Text("Buscar rua, bairro ou cidade") },
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
                busca.isBlank() -> Text(
                    "Digite o nome da rua para buscar.",
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