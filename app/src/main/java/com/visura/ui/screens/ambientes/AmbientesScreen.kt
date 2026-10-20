package com.visura.ui.screens.ambientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.visura.ui.theme.StatusGreenText
import com.visura.ui.theme.TextGray

@Composable
fun AmbientesScreen(
    ambientes: List<Ambiente>,
    onBackClick: () -> Unit = {},
    onAmbienteClick: (Ambiente) -> Unit = {},
    onAdicionarAmbienteClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val concluidos = ambientes.count { it.status == StatusAmbiente.CONCLUIDO }
    val todosConcluidos = ambientes.isNotEmpty() && concluidos == ambientes.size

    EtapaLayout(
        titulo = "Ambientes",
        onBackClick = onBackClick,
        botaoFlutuante = {
            ExtendedFloatingActionButton(
                onClick = onAdicionarAmbienteClick,
                containerColor = NavyBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Adicionar ambiente", fontWeight = FontWeight.Bold) }
            )
        },
        rodape = {
            BotaoPrincipal(
                texto = "Continuar",
                onClick = onContinueClick,
                enabled = todosConcluidos,
                icone = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
            )
        }
    ) {
        Text(
            "$concluidos de ${ambientes.size} ambientes avaliados",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            // espaço no fim para o botão flutuante não cobrir o último ambiente
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ambientes, key = { it.id }) { ambiente ->
                AmbienteCard(ambiente = ambiente, onClick = { onAmbienteClick(ambiente) })
            }
        }
    }
}

@Composable
private fun AmbienteCard(ambiente: Ambiente, onClick: () -> Unit) {
    val emAndamento = ambiente.status == StatusAmbiente.EM_ANDAMENTO
    val forma = RoundedCornerShape(14.dp)
    val progresso = if (ambiente.itens.isEmpty()) 0f else ambiente.itensAvaliados.toFloat() / ambiente.itens.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (emAndamento) LightBlueBg else Color.White)
            .border(if (emAndamento) 2.dp else 1.dp, if (emAndamento) NavyBlue else BorderGray, forma)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                ambiente.nome,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            StatusAmbienteChip(ambiente.status)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (emAndamento) NavyBlue else TextGray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("${ambiente.itensAvaliados} de ${ambiente.itens.size} itens", fontSize = 12.sp, color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progresso },
            color = if (ambiente.status == StatusAmbiente.CONCLUIDO) StatusGreenText else NavyBlue,
            trackColor = BorderGray,
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}
