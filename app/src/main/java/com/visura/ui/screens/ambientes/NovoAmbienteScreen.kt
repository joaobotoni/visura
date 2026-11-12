package com.visura.ui.screens.ambientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.screens.createaccount.LabeledField
import com.visura.ui.theme.TextGray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovoAmbienteScreen(
    ambientesExistentes: List<Ambiente>,
    onBackClick: () -> Unit = {},
    onAdicionarClick: (Ambiente) -> Unit = {}
) {
    var nome by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var duplicarDe by remember { mutableStateOf<Ambiente?>(null) }

    // Só faz sentido copiar de um ambiente que tem itens
    val ambientesParaDuplicar = ambientesExistentes.filter { it.itens.isNotEmpty() }

    EtapaLayout(
        titulo = "Novo ambiente",
        onBackClick = onBackClick,
        rodape = {
            BotaoPrincipal(
                texto = "Adicionar ambiente",
                enabled = nome.isNotBlank(),
                onClick = {
                    // Ao duplicar, os itens e estados vêm do ambiente escolhido (com ids novos)
                    val itens = duplicarDe?.itens?.map { it.copy(id = java.util.UUID.randomUUID().toString()) }
                        ?: itensPadrao()
                    onAdicionarClick(Ambiente(nome = nome.trim(), itens = itens, observacoes = observacoes.trim()))
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Escolha um tipo de ambiente ou crie um personalizado.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            LabeledField("Nome do ambiente *", nome, { nome = it }, placeholder = "Ex.: Sala de jogos")
            if (nome.isNotBlank() && iconeDoAmbiente(nome) == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Não encontramos um ícone específico — usaremos um ícone genérico para este ambiente.",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Rotulo("Tipos comuns")
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiposDeAmbienteComuns.forEach { tipo ->
                    ChipSelecionavel(
                        texto = tipo,
                        selecionado = nome.trim().equals(tipo, ignoreCase = true),
                        onClick = { nome = tipo },
                        icone = { IconeAmbiente(tipo) }
                    )
                }
            }

            if (ambientesParaDuplicar.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))

                Rotulo("Duplicar avaliação de", opcional = true)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ambientesParaDuplicar.forEach { amb ->
                        ChipSelecionavel(
                            texto = amb.nome,
                            selecionado = amb == duplicarDe,
                            // tocar de novo no escolhido desfaz a seleção
                            onClick = { duplicarDe = if (amb == duplicarDe) null else amb },
                            icone = { IconeAmbiente(amb.nome) }
                        )
                    }
                }
                duplicarDe?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Itens e estados de \"${it.nome}\" serão copiados.", fontSize = 12.sp, color = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Rotulo("Observações", opcional = true)
            Spacer(modifier = Modifier.height(8.dp))
            CampoMultilinha(observacoes, { observacoes = it }, "Ex.: ambiente anexo, sem uso frequente...")

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IconeAmbiente(nome: String) {
    Icon(
        iconeDoAmbiente(nome) ?: Icons.Filled.Category,
        contentDescription = null,
        modifier = Modifier.size(16.dp)
    )
}
