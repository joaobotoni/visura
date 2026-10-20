package com.example.firstproject.ui.screens.ambientes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.ui.theme.AlertaErroBg
import com.example.firstproject.ui.theme.AlertaErroIcone
import com.example.firstproject.ui.theme.BorderGray
import com.example.firstproject.ui.theme.LightBlueBg
import com.example.firstproject.ui.theme.NavyBlue
import com.example.firstproject.ui.theme.TextGray

@Composable
fun AmbienteDetalheScreen(
    ambiente: Ambiente,
    onBackClick: () -> Unit = {},
    onItemClick: (ItemAmbiente) -> Unit = {},
    onAdicionarItemClick: () -> Unit = {},
    onEditarItemClick: (ItemAmbiente) -> Unit = {},
    onRemoverItemClick: (ItemAmbiente) -> Unit = {}
) {
    // Item que está com o menu "Editar / Remover" aberto (aberto ao segurar o item)
    var itemComMenu by remember { mutableStateOf<String?>(null) }

    EtapaLayout(titulo = ambiente.nome, onBackClick = onBackClick) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (ambiente.itens.isEmpty()) {
                item {
                    Text(
                        "Nenhum item neste ambiente. Adicione um item para avaliar.",
                        fontSize = 13.sp,
                        color = TextGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(ambiente.itens, key = { it.id }) { item ->
                ItemRow(
                    item = item,
                    menuAberto = itemComMenu == item.id,
                    onClick = { onItemClick(item) },
                    onLongClick = { itemComMenu = item.id },
                    onFecharMenu = { itemComMenu = null },
                    onEditar = {
                        itemComMenu = null
                        onEditarItemClick(item)
                    },
                    onRemover = {
                        itemComMenu = null
                        onRemoverItemClick(item)
                    }
                )
            }

            item {
                OutlinedButton(
                    onClick = onAdicionarItemClick,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, NavyBlue),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar item personalizado", color = NavyBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            item {
                Text(
                    "Dica: segure um item para editar ou remover.",
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemRow(
    item: ItemAmbiente,
    menuAberto: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFecharMenu: () -> Unit,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val forma = RoundedCornerShape(14.dp)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(forma)
                .background(if (menuAberto) LightBlueBg else Color.White)
                .border(if (menuAberto) 2.dp else 1.dp, if (menuAberto) NavyBlue else BorderGray, forma)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.nome, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            EstadoChip(item.estado)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (menuAberto) NavyBlue else TextGray,
                modifier = Modifier.size(20.dp)
            )
        }

        // Menu ancorado embaixo do item, alinhado à direita
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            DropdownMenu(
                expanded = menuAberto,
                onDismissRequest = onFecharMenu,
                offset = DpOffset(x = 0.dp, y = 4.dp),
                containerColor = Color.White
            ) {
                DropdownMenuItem(
                    text = { Text("Editar item") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = TextGray) },
                    onClick = onEditar
                )
                DropdownMenuItem(
                    text = { Text("Remover item", color = AlertaErroIcone) },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = AlertaErroIcone) },
                    onClick = onRemover,
                    modifier = Modifier.background(AlertaErroBg)
                )
            }
        }
    }
}
