package com.example.firstproject.ui.screens.vistoria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.ui.screens.ambientes.Ambiente
import com.example.firstproject.ui.screens.ambientes.StatusAmbiente
import com.example.firstproject.ui.screens.home.cardBorder
import com.example.firstproject.ui.screens.imoveis.Imovel
import com.example.firstproject.ui.screens.imoveis.PessoaVinculada
import com.example.firstproject.ui.screens.pessoas.PapelChip
import com.example.firstproject.ui.theme.BorderGray
import com.example.firstproject.ui.theme.NavyBlue
import com.example.firstproject.ui.theme.StatusGreenBg
import com.example.firstproject.ui.theme.StatusGreenText
import com.example.firstproject.ui.theme.TextGray

@Composable
fun RevisaoScreen(
    imovel: Imovel?,
    pessoas: List<PessoaVinculada>,
    ambientes: List<Ambiente>,
    onBackClick: () -> Unit = {},
    onFinalizarClick: () -> Unit = {}
) {
    val ambientesAvaliados = ambientes.count { it.status == StatusAmbiente.CONCLUIDO }
    val itensAnalisados = ambientes.sumOf { it.itensAvaliados }
    val evidencias = ambientes.sumOf { amb -> amb.itens.sumOf { it.fotos } }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = BorderGray)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = onFinalizarClick,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Finalizar vistoria", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
                Text("Revisão geral", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(StatusGreenBg)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusGreenText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Tudo pronto! Nenhuma pendência encontrada.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusGreenText
                    )
                }

                if (imovel != null) {
                    Secao("IMÓVEL") {
                        Text(imovel.nome, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(imovel.endereco, fontSize = 13.sp, color = TextGray)
                        Text("${imovel.tipo} · ${imovel.finalidade}", fontSize = 12.sp, color = TextGray)
                    }
                }

                Secao("PESSOAS VINCULADAS") {
                    pessoas.forEach { pessoa ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pessoa.nome, fontSize = 14.sp)
                            PapelChip(pessoa.papel)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardBorder()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Numero("$ambientesAvaliados/${ambientes.size}", "Ambientes avaliados", Modifier.weight(1f))
                    Numero("$itensAnalisados", "Itens analisados", Modifier.weight(1f))
                    Numero("$evidencias", "Evidências", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Secao(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardBorder()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        content()
    }
}

@Composable
private fun Numero(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(rotulo, fontSize = 11.sp, color = TextGray)
    }
}
