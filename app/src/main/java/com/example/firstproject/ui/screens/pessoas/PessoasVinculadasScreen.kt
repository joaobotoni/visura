package com.example.firstproject.ui.screens.pessoas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.ui.screens.imoveis.PapelPessoa
import com.example.firstproject.ui.screens.imoveis.PessoaVinculada
import com.example.firstproject.ui.theme.AlertaErroBg
import com.example.firstproject.ui.theme.AlertaErroIcone
import com.example.firstproject.ui.theme.AlertaErroText
import com.example.firstproject.ui.theme.BorderGray
import com.example.firstproject.ui.theme.NavyBlue

@Composable
fun PessoasVinculadasScreen(
    pessoasIniciais: List<PessoaVinculada> = emptyList(),
    onBackClick: () -> Unit = {},
    onContinueClick: (pessoas: List<PessoaVinculada>) -> Unit = {}
) {
    var pessoas by remember { mutableStateOf(pessoasIniciais) }
    var mostrarAdicionar by remember { mutableStateOf(false) }
    var mostrarCadastradas by remember { mutableStateOf(false) }

    val papelFaltando = papelQueFalta(pessoas)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            floatingActionButton = {
                // Só faz sentido escolher alguém enquanto falta proprietário ou inquilino
                if (papelFaltando != null) {
                    ExtendedFloatingActionButton(
                        onClick = { mostrarCadastradas = true },
                        containerColor = NavyBlue,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        icon = { Icon(Icons.Filled.Group, contentDescription = null) },
                        text = { Text("Pessoas cadastradas", fontWeight = FontWeight.Bold) }
                    )
                }
            },
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = BorderGray)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = { onContinueClick(pessoas) },
                            enabled = papelFaltando == null,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
                    Text("Pessoas vinculadas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    // espaço no fim para o botão flutuante não cobrir o último item
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (papelFaltando != null) {
                        item { AvisoPendencia(pessoas) }
                    }

                    items(pessoas) { pessoa ->
                        PessoaCard(pessoa)
                    }

                    if (papelFaltando != null) {
                        item {
                            OutlinedButton(
                                onClick = { mostrarAdicionar = true },
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.dp, NavyBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = NavyBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Adicionar ${papelFaltando.rotulo.lowercase()}",
                                    color = NavyBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mostrarCadastradas && papelFaltando != null) {
            PessoasCadastradasSheet(
                jaVinculadas = pessoas,
                onClose = { mostrarCadastradas = false },
                // A pessoa escolhida entra no papel que está faltando, como no cadastro manual
                onPessoaClick = { pessoa ->
                    pessoas = pessoas + pessoa.copy(papel = papelFaltando)
                    mostrarCadastradas = false
                },
                onCadastrarNovaClick = {
                    mostrarCadastradas = false
                    mostrarAdicionar = true
                }
            )
        }

        if (mostrarAdicionar && papelFaltando != null) {
            AdicionarPessoaSheet(
                papel = papelFaltando,
                onClose = { mostrarAdicionar = false },
                onSave = { pessoa ->
                    pessoas = pessoas + pessoa
                    mostrarAdicionar = false
                }
            )
        }
    }
}

@Composable
private fun AvisoPendencia(pessoas: List<PessoaVinculada>) {
    val faltando = PapelPessoa.entries.filter { papel -> pessoas.none { it.papel == papel } }

    val texto = buildAnnotatedString {
        append("Falta vincular ")
        faltando.forEachIndexed { i, papel ->
            if (i > 0) append(" e ")
            append("um ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(papel.rotulo.lowercase()) }
        }
        append(" para continuar. A vistoria exige proprietário e inquilino.")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AlertaErroBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.Error,
            contentDescription = null,
            tint = AlertaErroIcone,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(texto, fontSize = 13.sp, color = AlertaErroText)
    }
}
