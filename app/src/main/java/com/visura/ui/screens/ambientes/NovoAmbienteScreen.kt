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
import com.visura.data.TipoDeAmbiente
import com.visura.ui.screens.createaccount.LabeledField
import com.visura.ui.theme.TextGray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovoAmbienteScreen(
    ambientesExistentes: List<Ambiente>,
    tiposDeAmbiente: List<TipoDeAmbiente> = emptyList(),
    onBackClick: () -> Unit = {},
    // tipo é o tipo do catálogo escolhido (null para ambiente personalizado)
    onAdicionarClick: (nome: String, tipo: TipoDeAmbiente?, observacoes: String, duplicarDe: Ambiente?) -> Unit =
        { _, _, _, _ -> }
) {
    var nome by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var duplicarDe by remember { mutableStateOf<Ambiente?>(null) }

    // O tipo vale enquanto o nome for igual ao do chip escolhido
    val tipoEscolhido = tiposDeAmbiente.firstOrNull { it.nome.equals(nome.trim(), ignoreCase = true) }

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
                    // Ao duplicar, os itens e estados vêm do ambiente escolhido
                    onAdicionarClick(nome.trim(), tipoEscolhido, observacoes.trim(), duplicarDe)
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

            if (tiposDeAmbiente.isNotEmpty()) {
                Rotulo("Tipos comuns")
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tiposDeAmbiente.forEach { tipo ->
                        ChipSelecionavel(
                            texto = tipo.nome,
                            selecionado = tipo == tipoEscolhido,
                            onClick = { nome = tipo.nome },
                            icone = { IconeAmbiente(tipo.nome) }
                        )
                    }
                }
                if (tipoEscolhido != null && duplicarDe == null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Os itens típicos deste ambiente serão criados automaticamente.", fontSize = 12.sp, color = TextGray)
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
