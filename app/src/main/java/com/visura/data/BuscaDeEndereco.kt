package com.visura.data

import com.visura.data.remote.ViaCepApi
import com.visura.data.remote.ViaCepEndereco
import com.visura.ui.screens.imoveis.EnderecoSugerido
import java.io.IOException

// Busca de endereço pelo ViaCEP. Aceita um CEP (8 dígitos, com ou sem máscara) ou "rua, cidade, UF".
class BuscaDeEndereco(private val viaCep: ViaCepApi = ViaCepApi.criar()) {

    suspend fun buscar(texto: String): List<EnderecoSugerido> {
        val digitos = texto.filter { it.isDigit() }
        if (digitos.length == 8 && texto.none { it.isLetter() }) return porCep(digitos)?.let(::listOf).orEmpty()

        val partes = texto.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (partes.size < 3) throw ApiErro("Digite o CEP ou: rua, cidade, UF (ex.: Avenida Paulista, São Paulo, SP).")
        val uf = partes.last().uppercase()
        if (uf.length != 2 || uf.any { !it.isLetter() }) throw ApiErro("Termine a busca com a sigla do estado (ex.: SP).")
        val rua = partes.first()
        if (rua.length < 3) throw ApiErro("Digite ao menos 3 letras do nome da rua.")
        val cidade = partes[partes.size - 2]

        return consultar { viaCep.porLogradouro(uf, cidade, rua) }.mapNotNull { it.paraSugestao() }
    }

    // Só o CEP, para completar o formulário quando o usuário digita os 8 dígitos.
    suspend fun porCep(cep: String): EnderecoSugerido? =
        consultar { viaCep.porCep(cep.filter { it.isDigit() }) }.paraSugestao()

    private suspend fun <T> consultar(chamada: suspend () -> T): T = try {
        chamada()
    } catch (_: IOException) {
        throw ApiErro("Sem conexão com a busca de endereços. Verifique a internet.")
    } catch (_: retrofit2.HttpException) {
        throw ApiErro("A busca de endereços não respondeu. Tente de novo ou preencha à mão.")
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (_: RuntimeException) {
        // Resposta fora do formato esperado (ex.: JSON inválido)
        throw ApiErro("A busca de endereços respondeu algo inesperado. Tente de novo ou preencha à mão.")
    }

    private fun ViaCepEndereco.paraSugestao(): EnderecoSugerido? {
        if (erro != null || cep.isNullOrBlank()) return null
        return EnderecoSugerido(
            rua = logradouro.orEmpty(),
            numero = "",
            bairro = bairro.orEmpty(),
            cidade = localidade.orEmpty(),
            uf = uf.orEmpty(),
            cep = cep
        )
    }
}
