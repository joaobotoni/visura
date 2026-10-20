package com.example.firstproject.ui.screens.imoveis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.ui.screens.home.StatusVistoria
import com.example.firstproject.ui.screens.vistoria.TipoVistoria
import com.example.firstproject.ui.theme.BorderGray
import com.example.firstproject.ui.theme.LightBlueBg
import com.example.firstproject.ui.theme.NavyBlue
import com.example.firstproject.ui.theme.StatusGreenBg
import com.example.firstproject.ui.theme.StatusGreenText
import com.example.firstproject.ui.theme.StatusOrangeBg
import com.example.firstproject.ui.theme.StatusOrangeText
import com.example.firstproject.ui.theme.TextGray
import com.example.firstproject.ui.theme.TipoSaidaBg
import com.example.firstproject.ui.theme.TipoSaidaText

@Composable
fun ImovelDetalheScreen(
    imovel: Imovel,
    onBackClick: () -> Unit = {},
    onNovaVistoriaClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNovaVistoriaClick,
                containerColor = NavyBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova vistoria", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        imovel.nome.substringBefore(","),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(iconeDoImovel(imovel.nome), contentDescription = null, tint = NavyBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(imovel.nome, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(imovel.endereco, fontSize = 13.sp, color = TextGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    Chip(imovel.tipo, LightBlueBg, NavyBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Chip(imovel.finalidade, LightBlueBg, NavyBlue)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderGray)
                Spacer(modifier = Modifier.height(16.dp))

                SecaoTitulo("PESSOAS VINCULADAS")
                Spacer(modifier = Modifier.height(10.dp))

                if (imovel.pessoas.isEmpty()) {
                    Text("Nenhuma pessoa vinculada.", fontSize = 13.sp, color = TextGray)
                }
            }

            items(imovel.pessoas) { pessoa ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(pessoa.nome, fontSize = 14.sp)
                    when (pessoa.papel) {
                        PapelPessoa.PROPRIETARIO -> Chip(pessoa.papel.rotulo, StatusGreenBg, StatusGreenText)
                        PapelPessoa.INQUILINO -> Chip(pessoa.papel.rotulo, LightBlueBg, NavyBlue)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderGray)
                Spacer(modifier = Modifier.height(16.dp))

                SecaoTitulo("HISTÓRICO DE VISTORIAS")
                Spacer(modifier = Modifier.height(10.dp))

                if (imovel.historico.isEmpty()) {
                    Text(
                        "Nenhuma vistoria realizada neste imóvel.",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            items(imovel.historico) { vistoria ->
                HistoricoCard(vistoria)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SecaoTitulo(texto: String) {
    Text(texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
}

@Composable
private fun Chip(texto: String, fundo: Color, cor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = cor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HistoricoCard(vistoria: VistoriaHistorico) {
    val (textoTipo, bgTipo, corTipo) = when (vistoria.tipo) {
        TipoVistoria.ENTRADA -> Triple("Vistoria de entrada", LightBlueBg, NavyBlue)
        TipoVistoria.SAIDA -> Triple("Vistoria de saída", TipoSaidaBg, TipoSaidaText)
    }
    val (textoStatus, bgStatus, corStatus) = when (vistoria.status) {
        StatusVistoria.EM_ANDAMENTO -> Triple("Em andamento", StatusOrangeBg, StatusOrangeText)
        StatusVistoria.CONCLUIDA -> Triple("Concluída", StatusGreenBg, StatusGreenText)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9FAFC))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip(textoTipo, bgTipo, corTipo)
            Chip(textoStatus, bgStatus, corStatus)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(vistoria.data, fontSize = 13.sp, color = TextGray)
    }
}