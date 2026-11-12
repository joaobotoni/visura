package com.visura.data

import com.visura.data.remote.ApiClient
import com.visura.data.remote.ProblemResponse
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

// Erro já com a mensagem pronta para mostrar ao usuário.
class ApiErro(mensagem: String, val status: Int? = null) : Exception(mensagem)

// Executa uma chamada da API e converte qualquer falha em ApiErro com uma mensagem em português.
// A API responde erros no formato RFC 9457; usamos a mensagem do primeiro campo inválido ou o "detail".
internal suspend fun <T> chamar(bloco: suspend () -> T): T = try {
    bloco()
} catch (e: CancellationException) {
    throw e
} catch (e: ApiErro) {
    throw e
} catch (e: HttpException) {
    throw ApiErro(mensagemDoErro(e), e.code())
} catch (_: IOException) {
    throw ApiErro("Não foi possível falar com o servidor. Verifique a conexão e se a API está no ar.")
} catch (e: Exception) {
    throw ApiErro("Erro inesperado: ${e.message ?: e.javaClass.simpleName}")
}

private fun mensagemDoErro(e: HttpException): String {
    val problema = try {
        e.response()?.errorBody()?.string()?.let { ApiClient.gson.fromJson(it, ProblemResponse::class.java) }
    } catch (_: Exception) {
        null
    }
    problema?.errors?.firstOrNull()?.message?.takeIf { it.isNotBlank() }?.let { return it }
    problema?.detail?.takeIf { it.isNotBlank() }?.let { return it }
    return when (e.code()) {
        401 -> "Sua sessão expirou. Entre novamente."
        403 -> "Você não tem permissão para fazer isso."
        404 -> "Registro não encontrado."
        409 -> "Conflito com um registro existente."
        429 -> "Muitas tentativas. Aguarde um pouco e tente de novo."
        else -> "O servidor respondeu com erro ${e.code()}."
    }
}
