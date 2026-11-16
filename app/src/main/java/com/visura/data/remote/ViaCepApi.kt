package com.visura.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Busca de endereços no ViaCEP (https://viacep.com.br), serviço público e gratuito dos dados dos Correios.
// Não precisa de chave; a API do Visura não participa.
interface ViaCepApi {

    @GET("{cep}/json/")
    suspend fun porCep(@Path("cep") cep: String): ViaCepEndereco

    // O ViaCEP exige UF, cidade e ao menos 3 letras do logradouro; devolve até 50 endereços.
    @GET("{uf}/{cidade}/{logradouro}/json/")
    suspend fun porLogradouro(
        @Path("uf") uf: String,
        @Path("cidade") cidade: String,
        @Path("logradouro") logradouro: String
    ): List<ViaCepEndereco>

    companion object {
        fun criar(): ViaCepApi = Retrofit.Builder()
            .baseUrl("https://viacep.com.br/ws/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ViaCepApi::class.java)
    }
}

// CEP inexistente volta como {"erro": "true"} (ou true, nas versões antigas), por isso "erro" é Any?.
data class ViaCepEndereco(
    val cep: String?,
    val logradouro: String?,
    val bairro: String?,
    val localidade: String?,
    val uf: String?,
    val erro: Any?
)
