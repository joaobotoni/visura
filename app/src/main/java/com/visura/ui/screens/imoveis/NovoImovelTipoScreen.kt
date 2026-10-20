package com.visura.ui.screens.imoveis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

enum class TipoImovel(val rotulo: String) {
    APARTAMENTO("Apartamento"),
    CASA("Casa"),
    SALA_COMERCIAL("Sala comercial"),
    LOJA("Loja"),
    GALPAO("Galpão"),
    TERRENO("Terreno"),
    OUTRO("Outro")
}

enum class CategoriaImovel(val rotulo: String) {
    RESIDENCIAL("Residencial"),
    COMERCIAL("Comercial"),
    INDUSTRIAL("Industrial"),
    ALTO_PADRAO("Alto padrão"),
    POPULAR("Popular")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovoImovelTipoScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: (TipoImovel, CategoriaImovel) -> Unit = { _, _ -> }
) {
    var tipo by rememberSaveable { mutableStateOf<TipoImovel?>(null) }
    var categoria by rememberSaveable { mutableStateOf<CategoriaImovel?>(null) }

    // Os dois campos são obrigatórios
    val podeContinuar = tipo != null && categoria != null

    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .safeDrawingPadding()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dados do imóvel", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Conte um pouco mais sobre o tipo e a categoria do imóvel.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Tipo do imóvel", fontSize = 12.sp, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoImovel.entries.forEach { opcao ->
                        OpcaoChip(
                            texto = opcao.rotulo,
                            selecionado = tipo == opcao,
                            fundoSelecionado = NavyBlue,
                            textoSelecionado = Color.White,
                            bordaSelecionada = NavyBlue,
                            onClick = { tipo = opcao }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Categoria", fontSize = 12.sp, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoriaImovel.entries.forEach { opcao ->
                        OpcaoChip(
                            texto = opcao.rotulo,
                            selecionado = categoria == opcao,
                            fundoSelecionado = LightBlueBg,
                            textoSelecionado = NavyBlue,
                            bordaSelecionada = LightBlueBg,
                            onClick = { categoria = opcao }
                        )
                    }
                }
            }

            HorizontalDivider(color = BorderGray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        val t = tipo
                        val c = categoria
                        if (t != null && c != null) onContinueClick(t, c)
                    },
                    enabled = podeContinuar,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun OpcaoChip(
    texto: String,
    selecionado: Boolean,
    fundoSelecionado: Color,
    textoSelecionado: Color,
    bordaSelecionada: Color,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .clip(forma)
            .background(if (selecionado) fundoSelecionado else Color.White)
            .border(1.dp, if (selecionado) bordaSelecionada else BorderGray, forma)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            texto,
            fontSize = 13.sp,
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selecionado) textoSelecionado else Color.Black
        )
    }
}