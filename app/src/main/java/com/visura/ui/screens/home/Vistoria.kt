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
    val responsavel: String,
    val id: String = ""
)
