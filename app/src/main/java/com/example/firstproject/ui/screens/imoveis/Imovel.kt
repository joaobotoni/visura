package com.example.firstproject.ui.screens.imoveis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.firstproject.ui.screens.home.StatusVistoria
import com.example.firstproject.ui.screens.vistoria.TipoVistoria

enum class PapelPessoa(val rotulo: String) {
    PROPRIETARIO("Proprietário"),
    INQUILINO("Inquilino")
}

data class PessoaVinculada(
    val nome: String,
    val papel: PapelPessoa,
    val documento: String = "", // já formatado, ex.: "CPF 022.884.610-91"
    val telefone: String = "",
    val email: String = ""
) {
    // "Carla Souza Lima" -> "CS"
    val iniciais: String
        get() = nome.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
}

data class VistoriaHistorico(
    val tipo: TipoVistoria,
    val status: StatusVistoria,
    val data: String
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

// Lista de exemplo — será substituída pelos imóveis realmente cadastrados.
val imoveisDeExemplo = listOf(
    Imovel(
        id = "1",
        nome = "Edifício Aurora, apto 62",
        endereco = "Rua das Palmeiras, 210 — Água Verde, Curitiba/PR",
        tipo = "Apartamento",
        finalidade = "Residencial",
        pessoas = listOf(PessoaVinculada("Carla Souza Lima", PapelPessoa.PROPRIETARIO, "CPF 022.884.610-91")),
        historico = listOf(
            VistoriaHistorico(TipoVistoria.ENTRADA, StatusVistoria.EM_ANDAMENTO, "18 jul 2026"),
            VistoriaHistorico(TipoVistoria.SAIDA, StatusVistoria.CONCLUIDA, "02 fev 2026"),
            VistoriaHistorico(TipoVistoria.ENTRADA, StatusVistoria.CONCLUIDA, "14 jan 2025")
        )
    ),
    Imovel(
        id = "2",
        nome = "Casa Jardim das Acácias",
        endereco = "Av. Brasil, 1450 — Curitiba/PR",
        tipo = "Casa",
        finalidade = "Residencial",
        pessoas = listOf(PessoaVinculada("Beatriz Lima", PapelPessoa.PROPRIETARIO, "CPF 344.902.150-22")),
        historico = listOf(
            VistoriaHistorico(TipoVistoria.SAIDA, StatusVistoria.CONCLUIDA, "02 jul 2026"),
            VistoriaHistorico(TipoVistoria.ENTRADA, StatusVistoria.CONCLUIDA, "10 mar 2025")
        )
    ),
    Imovel(
        id = "3",
        nome = "Sala Comercial 4B",
        endereco = "Centro Empresarial Norte — Curitiba/PR",
        tipo = "Sala",
        finalidade = "Comercial",
        pessoas = listOf(PessoaVinculada("Empresa Delta", PapelPessoa.PROPRIETARIO, "CNPJ 19.204.885/0001-30")),
        historico = listOf(
            VistoriaHistorico(TipoVistoria.ENTRADA, StatusVistoria.CONCLUIDA, "24 jun 2026")
        )
    )
)

// Mude para true para simular "nenhum imóvel cadastrado"
private const val SIMULAR_SEM_IMOVEIS = false

// Lista que o app realmente usa (precisa ficar por último no arquivo)
val imoveisExemplo: List<Imovel> =
    if (SIMULAR_SEM_IMOVEIS) emptyList() else imoveisDeExemplo