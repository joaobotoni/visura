package com.visura.ui.screens.ambientes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Ícone de cada ambiente e item, escolhido pelo nome
private val iconesDeAmbiente = listOf(
    "quarto" to Icons.Filled.Bed,
    "suíte" to Icons.Filled.Bed,
    "sala" to Icons.Filled.Weekend,
    "cozinha" to Icons.Filled.Kitchen,
    "banheiro" to Icons.Filled.Bathtub,
    "lavabo" to Icons.Filled.Bathtub,
    "garagem" to Icons.Filled.Garage,
    "lavanderia" to Icons.Filled.LocalLaundryService,
    "área externa" to Icons.Filled.Yard,
    "escritório" to Icons.Filled.Work,
    "varanda" to Icons.Filled.DoorSliding,
    "closet" to Icons.Filled.Checkroom,
    "escada" to Icons.Filled.Stairs,
    "churrasqueira" to Icons.Filled.OutdoorGrill,
    "adega" to Icons.Filled.WineBar,
    "home theater" to Icons.Filled.Theaters,
    "piscina" to Icons.Filled.Pool,
    "quintal" to Icons.Filled.Deck,
    "depósito" to Icons.Filled.HomeRepairService,
    "hall" to Icons.Filled.MeetingRoom,
    "jardim" to Icons.Filled.Grass
)

private val iconesDeItem = listOf(
    "piso" to Icons.Filled.Layers,
    "parede" to Icons.Filled.Wallpaper,
    "pintura" to Icons.Filled.FormatPaint,
    "porta" to Icons.Filled.DoorFront,
    "janela" to Icons.Filled.Window,
    "tomada" to Icons.Filled.ElectricalServices,
    "iluminação" to Icons.Filled.Lightbulb,
    "lâmpada" to Icons.Filled.Lightbulb,
    "teto" to Icons.Filled.Roofing,
    "pia" to Icons.Filled.Countertops,
    "bancada" to Icons.Filled.Countertops,
    "chuveiro" to Icons.Filled.Shower,
    "armário" to Icons.Filled.Kitchen
)

// null quando o nome não bate com nenhum ícone conhecido (a tela usa um ícone genérico)
fun iconeDoAmbiente(nome: String): ImageVector? =
    iconesDeAmbiente.firstOrNull { nome.contains(it.first, ignoreCase = true) }?.second

fun iconeDoItem(nome: String): ImageVector? =
    iconesDeItem.firstOrNull { nome.contains(it.first, ignoreCase = true) }?.second
