package com.visura.ui.screens.home

enum class StatusVistoria { EM_ANDAMENTO, CONCLUIDA }

data class Vistoria(
    val imovelId: String,
    val titulo: String,
    val endereco: String,
    val status: StatusVistoria,
    val data: String,
    val ambientesFeitos: Int,
    val ambientesTotais: Int,
    val responsavel: String
)

// Lista de exemplo só para visualização do layout — será substituída
// pelos dados reais vindos do banco/backend futuramente.
// O imovelId aponta para o imóvel correspondente em imoveisDeExemplo.
val vistoriasExemplo = listOf(
    Vistoria(
        imovelId = "1",
        titulo = "Edifício Aurora, apto 62",
        endereco = "Rua das Palmeiras, 210 — Curitiba/PR",
        status = StatusVistoria.EM_ANDAMENTO,
        data = "18 jul 2026",
        ambientesFeitos = 4,
        ambientesTotais = 7,
        responsavel = "Carla Souza"
    ),
    Vistoria(
        imovelId = "2",
        titulo = "Casa Jardim das Acácias",
        endereco = "Av. Brasil, 1450 — Curitiba/PR",
        status = StatusVistoria.CONCLUIDA,
        data = "02 jul 2026",
        ambientesFeitos = 7,
        ambientesTotais = 7,
        responsavel = "Beatriz Lima"
    ),
    Vistoria(
        imovelId = "3",
        titulo = "Sala Comercial 4B",
        endereco = "Centro Empresarial Norte — Curitiba/PR",
        status = StatusVistoria.CONCLUIDA,
        data = "24 jun 2026",
        ambientesFeitos = 5,
        ambientesTotais = 5,
        responsavel = "Empresa Delta"
    )
)