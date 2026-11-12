package com.visura.ui.screens.imoveis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import com.visura.ui.screens.home.StatusVistoria
import com.visura.ui.screens.vistoria.TipoVistoria

enum class PapelPessoa(val rotulo: String) {
    PROPRIETARIO("Proprietário"),
    INQUILINO("Inquilino")
}

data class PessoaVinculada(
    val nome: String,
    val papel: PapelPessoa,
    val documento: String = "", // já formatado, ex.: "CPF 022.884.610-91"
    val telefone: String = "",
    val email: String = "",
    val id: String = "" // id da pessoa na API (vazio enquanto não foi cadastrada)
) {
    // "Carla Souza Lima" -> "CS"
    val iniciais: String
        get() = nome.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
}

data class VistoriaHistorico(
    val tipo: TipoVistoria,
    val status: StatusVistoria,
    val data: String,
    val id: String = ""
)

data class Imovel(
    val id: String,
    val nome: String,
    val endereco: String,
    val tipo: String,
    val finalidade: String,
    val pessoas: List<PessoaVinculada>,
    val historico: List<VistoriaHistorico>
) {
    val quantidadeVistorias: Int get() = historico.size
}

internal fun iconeDoImovel(nome: String): ImageVector = when {
    nome.contains("Edifício", ignoreCase = true) ||
            nome.contains("apto", ignoreCase = true) -> Icons.Filled.Apartment
    nome.contains("Sala", ignoreCase = true) ||
            nome.contains("Comercial", ignoreCase = true) -> Icons.Filled.Storefront
    else -> Icons.Filled.House
}
