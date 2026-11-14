package com.visura.ui.screens.imoveis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

internal fun iconeDoImovel(nome: String): ImageVector = when {
    nome.contains("Edifício", ignoreCase = true) ||
            nome.contains("apto", ignoreCase = true) -> Icons.Filled.Apartment
    nome.contains("Sala", ignoreCase = true) ||
            nome.contains("Comercial", ignoreCase = true) -> Icons.Filled.Storefront
    else -> Icons.Filled.House
}
