package com.visura.data.remote

import com.google.gson.Gson
import com.visura.BuildConfig
import com.visura.data.Sessao
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Monta o Retrofit da API com:
//  - o access token em toda chamada (menos as de auth/);
//  - renovação automática: num 401, troca o refresh token por um novo par e repete a chamada uma vez.
//    Se a renovação for recusada, limpa a sessão e avisa o app (Sessao.expirou).
object ApiClient {

    val gson = Gson()

    fun criar(sessao: Sessao): VisuraApi {
        val log = HttpLoggingInterceptor().apply {
            // BASIC registra só método, rota e status: nunca senha ou token no logcat.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        val cliente = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(TokenInterceptor(sessao))
            .authenticator(RenovaToken(sessao))
            .addInterceptor(log)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(VisuraApi::class.java)
    }

    private fun ehRotaDeAutenticacao(request: Request): Boolean {
        val caminho = request.url.encodedPath
        return caminho.endsWith("/auth/login") || caminho.endsWith("/auth/register") || caminho.endsWith("/auth/refresh") ||
            caminho.endsWith("/auth/google") || caminho.contains("/auth/password/")
    }

    private class TokenInterceptor(private val sessao: Sessao) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val token = sessao.accessToken
            if (token == null || ehRotaDeAutenticacao(original)) return chain.proceed(original)
            return chain.proceed(original.newBuilder().header("Authorization", "Bearer $token").build())
        }
    }

    private class RenovaToken(private val sessao: Sessao) : Authenticator {

        // Cliente separado, sem este Authenticator, para a chamada de renovação não entrar em laço.
        private val cliente = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        override fun authenticate(route: Route?, response: Response): Request? {
            val request = response.request
            // Só tenta uma vez por chamada, e nunca para as rotas de login/cadastro (lá 401 é senha errada).
            if (response.priorResponse != null || ehRotaDeAutenticacao(request)) return null

            synchronized(this) {
                val tokenUsado = request.header("Authorization")?.removePrefix("Bearer ")
                val atual = sessao.accessToken
                // Outra chamada já renovou enquanto esta esperava: só repete com o token novo.
                if (atual != null && atual != tokenUsado) {
                    return request.newBuilder().header("Authorization", "Bearer $atual").build()
                }

                val refresh = sessao.refreshToken ?: return null
                return when (val resultado = renovar(refresh)) {
                    is Renovacao.Ok -> {
                        sessao.salvarTokens(resultado.access, resultado.refresh)
                        request.newBuilder().header("Authorization", "Bearer ${resultado.access}").build()
                    }
                    // A API recusou o refresh token (expirado, revogado ou reusado): a sessão acabou.
                    Renovacao.Recusada -> {
                        sessao.limpar(expirou = true)
                        null
                    }
                    // Falha de rede ou do servidor: mantém a sessão e deixa a chamada falhar com o 401.
                    Renovacao.Falhou -> null
                }
            }
        }

        private sealed interface Renovacao {
            data class Ok(val access: String, val refresh: String) : Renovacao
            data object Recusada : Renovacao
            data object Falhou : Renovacao
        }

        private fun renovar(refreshToken: String): Renovacao = try {
            val corpo = gson.toJson(RefreshRequest(refreshToken)).toRequestBody("application/json".toMediaType())
            val chamada = Request.Builder()
                .url(BuildConfig.API_BASE_URL + "auth/refresh")
                .post(corpo)
                .build()
            cliente.newCall(chamada).execute().use { resposta ->
                val token = if (resposta.isSuccessful) {
                    gson.fromJson(resposta.body?.string(), TokenResponse::class.java)
                } else null
                when {
                    token?.accessToken != null && token.refreshToken != null -> Renovacao.Ok(token.accessToken, token.refreshToken)
                    resposta.code == 400 || resposta.code == 401 -> Renovacao.Recusada
                    else -> Renovacao.Falhou
                }
            }
        } catch (_: Exception) {
            Renovacao.Falhou
        }
    }
}
