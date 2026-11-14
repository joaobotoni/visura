package com.visura.model

// Modelos de domínio do app: o repositório converte as respostas da API neles e as telas os exibem.
// Ficam fora de ui/ para que a camada de dados não dependa da camada de interface.

enum class TipoVistoria { ENTRADA, SAIDA }

enum class StatusVistoria { EM_ANDAMENTO, CONCLUIDA }

// Vistoria como aparece na Home
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

// Vistoria como aparece no histórico do imóvel
data class VistoriaHistorico(
    val tipo: TipoVistoria,
    val status: StatusVistoria,
    val data: String,
    val id: String = ""
)

// Tudo que a revisão e o laudo precisam de uma vistoria
data class VistoriaCompleta(
    val id: String,
    val imovel: Imovel?,
    val pessoas: List<PessoaVinculada>,
    val ambientes: List<Ambiente>,
    val finalizada: Boolean,
    val tipo: TipoVistoria = TipoVistoria.ENTRADA,
    val responsavel: String = "",
    val iniciadaEm: String = "",
    val finalizadaEm: String = "",
    val observacoes: String = ""
)

data class Perfil(val nome: String, val email: String, val cpf: String, val cnpj: String)
