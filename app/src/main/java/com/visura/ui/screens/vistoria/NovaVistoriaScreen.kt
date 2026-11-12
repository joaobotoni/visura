package com.visura.ui.screens.vistoria

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

enum class TipoVistoria { ENTRADA, SAIDA }

@Composable
fun NovaVistoriaScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: (tipo: TipoVistoria, observacoes: String) -> Unit = { _, _ -> }
) {
    var tipoSelecionado by remember { mutableStateOf<TipoVistoria?>(null) }
    var observacoes by remember { mutableStateOf("") }

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
                Text("Nova vistoria", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                TipoVistoriaCard(
                    icone = Icons.AutoMirrored.Filled.Login,
                    titulo = "Vistoria de entrada",
                    descricao = "Registra as condições iniciais do imóvel antes da entrada de um novo inquilino.",
                    selecionado = tipoSelecionado == TipoVistoria.ENTRADA,
                    onClick = { tipoSelecionado = TipoVistoria.ENTRADA }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TipoVistoriaCard(
                    icone = Icons.AutoMirrored.Filled.Logout,
                    titulo = "Vistoria de saída",
                    descricao = "Registra o estado do imóvel no momento da devolução pelo inquilino.",
                    selecionado = tipoSelecionado == TipoVistoria.SAIDA,
                    onClick = { tipoSelecionado = TipoVistoria.SAIDA }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row {
                    Text("Observações iniciais", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("(opcional)", fontSize = 13.sp, color = TextGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    placeholder = {
                        Text(
                            "Ex.: imóvel recém-reformado, chaves entregues pela imobiliária...",
                            color = TextGray
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderGray,
                        focusedBorderColor = NavyBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            HorizontalDivider(color = BorderGray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { tipoSelecionado?.let { onContinueClick(it, observacoes) } },
                    enabled = tipoSelecionado != null,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TipoVistoriaCard(
    icone: ImageVector,
    titulo: String,
    descricao: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selecionado) LightBlueBg else Color.White)
            .border(
                width = if (selecionado) 2.dp else 1.dp,
                color = if (selecionado) NavyBlue else BorderGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icone,
            contentDescription = null,
            tint = if (selecionado) NavyBlue else TextGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (selecionado) NavyBlue else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(descricao, fontSize = 13.sp, color = TextGray)
        }
    }
}