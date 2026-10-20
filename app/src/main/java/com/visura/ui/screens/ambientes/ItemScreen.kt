package com.visura.ui.screens.ambientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.screens.createaccount.LabeledField
import com.visura.ui.theme.BackgroundLavender
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

enum class ModoItem { NOVO, AVALIAR, EDITAR }

// Uma tela para três usos:
//  NOVO    — "Novo item — Sala" (item personalizado)
//  AVALIAR — "Portas — Sala" (estado, material, cor, observações e fotos)
//  EDITAR  — "Editar item — Sala" (como o NOVO, aberto pelo menu de segurar o item)
@Composable
fun ItemScreen(
    modo: ModoItem,
    nomeDoAmbiente: String,
    itemInicial: ItemAmbiente = ItemAmbiente(nome = ""),
    onBackClick: () -> Unit = {},
    onSaveClick: (ItemAmbiente) -> Unit = {}
) {
    var nome by remember { mutableStateOf(itemInicial.nome) }
    var descricao by remember { mutableStateOf(itemInicial.descricao) }
    var material by remember { mutableStateOf(itemInicial.material) }
    var cor by remember { mutableStateOf(itemInicial.cor) }
    var estado by remember { mutableStateOf(itemInicial.estado) }
    var observacoes by remember { mutableStateOf(itemInicial.observacoes) }
    var fotos by remember { mutableIntStateOf(itemInicial.fotos) }

    val editaNome = modo != ModoItem.AVALIAR

    val (titulo, textoBotao) = when (modo) {
        ModoItem.NOVO -> "Novo item — $nomeDoAmbiente" to "Adicionar item"
        ModoItem.AVALIAR -> "${itemInicial.nome} — $nomeDoAmbiente" to "Salvar item"
        ModoItem.EDITAR -> "Editar item — $nomeDoAmbiente" to "Salvar alterações"
    }

    EtapaLayout(
        titulo = titulo,
        onBackClick = onBackClick,
        rodape = {
            BotaoPrincipal(
                texto = textoBotao,
                enabled = nome.isNotBlank(),
                onClick = {
                    onSaveClick(
                        itemInicial.copy(
                            nome = nome.trim(),
                            descricao = descricao.trim(),
                            material = material.trim(),
                            cor = cor.trim(),
                            estado = estado,
                            observacoes = observacoes.trim(),
                            fotos = fotos
                        )
                    )
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
            if (editaNome) {
                Text(
                    "Descreva o item que deseja avaliar neste ambiente.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(18.dp))

                CampoNomeDoItem(nome) { nome = it }

                Spacer(modifier = Modifier.height(12.dp))
                LabeledField("Descrição (opcional)", descricao, { descricao = it }, placeholder = "Ex.: localização, uso, particularidade")
                Spacer(modifier = Modifier.height(18.dp))
            }

            Rotulo("Estado atual")
            Spacer(modifier = Modifier.height(8.dp))
            EstadoSelector(estado = estado, onEstadoChange = { estado = it })

            Spacer(modifier = Modifier.height(18.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField("Material", material, { material = it }, placeholder = "Ex.: MDF")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField("Cor", cor, { cor = it }, placeholder = "Ex.: Branco")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Rotulo("Observações")
            Spacer(modifier = Modifier.height(8.dp))
            CampoMultilinha(observacoes, { observacoes = it }, "Descreva problemas encontrados ou informações relevantes...")

            Spacer(modifier = Modifier.height(18.dp))

            Rotulo("Evidências fotográficas")
            Spacer(modifier = Modifier.height(8.dp))
            EvidenciasGrid(fotos = fotos, onAdicionarFoto = { fotos++ })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Campo do nome com o ícone do item à esquerda (genérico quando o nome não é reconhecido)
@Composable
private fun CampoNomeDoItem(nome: String, onNomeChange: (String) -> Unit) {
    Text("Nome do item *", fontSize = 13.sp, color = TextGray)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = nome,
        onValueChange = onNomeChange,
        placeholder = { Text("Ex.: Aquário embutido") },
        singleLine = true,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(BackgroundLavender),
                contentAlignment = Alignment.Center
            ) {
                val icone = iconeDoItem(nome)
                Icon(
                    icone ?: Icons.Filled.Category,
                    contentDescription = null,
                    tint = if (icone != null) NavyBlue else TextGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = NavyBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
    if (nome.isNotBlank() && iconeDoItem(nome) == null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Não encontramos um ícone específico — usaremos um ícone genérico para este item.",
            fontSize = 12.sp,
            color = TextGray
        )
    }
}
