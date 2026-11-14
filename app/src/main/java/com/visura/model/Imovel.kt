package com.visura.model

// Só os valores que a API aceita; valorApi é o que vai no JSON
enum class TipoImovel(val rotulo: String, val valorApi: String) {
    APARTAMENTO("Apartamento", "apartamento"),
    CASA("Casa", "casa"),
    SALA_COMERCIAL("Sala comercial", "sala_comercial"),
    GALPAO("Galpão", "galpao"),
    OUTRO("Outro", "outro")
}

enum class CategoriaImovel(val rotulo: String, val valorApi: String) {
    RESIDENCIAL("Residencial", "residencial"),
    COMERCIAL("Comercial", "comercial"),
    ALTO_PADRAO("Alto padrão", "alto_padrao")
}

data class EnderecoImovel(
    val cep: String,
    val rua: String,
    val numero: String,
    val complemento: String,
    val bairro: String,
    val cidade: String,
    val uf: String
)

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
