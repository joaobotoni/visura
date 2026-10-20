package com.visura.ui.screens.ambientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.AlertaErroBg
import com.visura.ui.theme.AlertaErroIcone
import com.visura.ui.theme.BackgroundLavender
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.StatusGreenBg
import com.visura.ui.theme.StatusGreenText
import com.visura.ui.theme.StatusOrangeBg
import com.visura.ui.theme.StatusOrangeText
import com.visura.ui.theme.TextGray

// Estrutura comum das etapas da vistoria: voltar + título, conteúdo e (opcional) botão fixo no rodapé
@Composable
internal fun EtapaLayout(
    titulo: String,
    onBackClick: () -> Unit,
    rodape: (@Composable () -> Unit)? = null,
    botaoFlutuante: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        floatingActionButton = botaoFlutuante,
        bottomBar = {
            if (rodape != null) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = BorderGray)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        rodape()
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
                Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            content()
        }
    }
}

@Composable
internal fun BotaoPrincipal(
    texto: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icone: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (icone != null) {
            Spacer(modifier = Modifier.width(8.dp))
            icone()
        }
    }
}

@Composable
internal fun Rotulo(texto: String, opcional: Boolean = false) {
    Row {
        Text(texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (opcional) {
            Spacer(modifier = Modifier.width(4.dp))
            Text("(opcional)", fontSize = 13.sp, color = TextGray)
        }
    }
}

@Composable
internal fun CampoMultilinha(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = { Text(placeholder, color = TextGray) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = NavyBlue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
    )
}

@Composable
internal fun Chip(texto: String, fundo: Color, cor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = cor, fontWeight = FontWeight.Bold)
    }
}

private fun EstadoItem.ehProblema() = this == EstadoItem.RUIM || this == EstadoItem.DANIFICADO

@Composable
internal fun EstadoChip(estado: EstadoItem?) {
    when {
        estado == null -> Chip("Pendente", BackgroundLavender, TextGray)
        estado.ehProblema() -> Chip(estado.rotulo, AlertaErroBg, AlertaErroIcone)
        estado == EstadoItem.REGULAR -> Chip(estado.rotulo, StatusOrangeBg, StatusOrangeText)
        else -> Chip(estado.rotulo, StatusGreenBg, StatusGreenText)
    }
}

@Composable
internal fun StatusAmbienteChip(status: StatusAmbiente) {
    when (status) {
        StatusAmbiente.CONCLUIDO -> Chip("Concluído", StatusGreenBg, StatusGreenText)
        StatusAmbiente.EM_ANDAMENTO -> Chip("Em andamento", StatusOrangeBg, StatusOrangeText)
        StatusAmbiente.PENDENTE -> Chip("Pendente", BackgroundLavender, TextGray)
    }
}

// Chip de seleção com contorno; selecionado fica preenchido
@Composable
internal fun ChipSelecionavel(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    corSelecionado: Color = NavyBlue,
    icone: (@Composable () -> Unit)? = null
) {
    val forma = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .clip(forma)
            .background(if (selecionado) corSelecionado else Color.White)
            .border(1.dp, if (selecionado) corSelecionado else BorderGray, forma)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icone != null) {
            CompositionLocalProvider(LocalContentColor provides if (selecionado) Color.White else TextGray) {
                icone()
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            texto,
            fontSize = 12.sp,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selecionado) Color.White else TextGray
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EstadoSelector(
    estado: EstadoItem?,
    onEstadoChange: (EstadoItem) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EstadoItem.entries.forEach { opcao ->
            ChipSelecionavel(
                texto = opcao.rotulo,
                selecionado = opcao == estado,
                onClick = { onEstadoChange(opcao) },
                corSelecionado = if (opcao.ehProblema()) AlertaErroIcone else NavyBlue
            )
        }
    }
}

// SIMULAÇÃO — sem câmera ainda: cada toque em "adicionar foto" só soma uma miniatura
@Composable
internal fun EvidenciasGrid(
    fotos: Int,
    onAdicionarFoto: () -> Unit
) {
    val celulas = fotos + 1
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (0 until celulas).chunked(3).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                linha.forEach { i ->
                    val ehBotao = i == fotos
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundLavender)
                            .then(
                                if (ehBotao) Modifier
                                    .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                                    .clickable { onAdicionarFoto() }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (ehBotao) Icons.Filled.AddAPhoto else Icons.Filled.Image,
                            contentDescription = if (ehBotao) "Adicionar foto" else null,
                            tint = if (ehBotao) TextGray else NavyBlue
                        )
                    }
                }
                // completa a linha para as células manterem o mesmo tamanho
                repeat(3 - linha.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
