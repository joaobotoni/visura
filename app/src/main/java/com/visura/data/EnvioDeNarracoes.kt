package com.visura.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.visura.VisuraApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.concurrent.TimeUnit

// Situação dos envios de narração de um ambiente, para a tela mostrar.
data class EnviosDeNarracao(
    val aguardando: Int = 0,
    val enviadas: Set<String> = emptySet(), // ids das evidências recebidas pela API nesta execução do app
    val recusadas: List<String> = emptyList() // mensagens da API para as narrações que ela não aceitou
)

// Envia as narrações gravadas em segundo plano, pelo WorkManager: continua com o app fechado, espera haver
// internet e tenta de novo com espera crescente se a conexão cair. O arquivo só é apagado do aparelho depois que a
// API confirma o recebimento, ou quando ela recusa de vez (vistoria finalizada, ambiente removido).
object EnvioDeNarracoes {

    private const val AMBIENTE = "ambienteId"
    private const val ARQUIVO = "arquivo"
    const val EVIDENCIA = "evidenciaId"
    const val ERRO = "erro"

    fun agendar(context: Context, ambienteId: String, arquivo: File) {
        val envio = OneTimeWorkRequestBuilder<EnvioDeNarracaoWorker>()
            .setInputData(workDataOf(AMBIENTE to ambienteId, ARQUIVO to arquivo.absolutePath))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(etiqueta(ambienteId))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(arquivo.name, ExistingWorkPolicy.KEEP, envio)
    }

    fun acompanhar(context: Context, ambienteId: String): Flow<EnviosDeNarracao> =
        WorkManager.getInstance(context).getWorkInfosByTagFlow(etiqueta(ambienteId)).map { envios ->
            EnviosDeNarracao(
                aguardando = envios.count { !it.state.isFinished },
                enviadas = envios.filter { it.state == WorkInfo.State.SUCCEEDED }
                    .mapNotNull { it.outputData.getString(EVIDENCIA) }.toSet(),
                recusadas = envios.filter { it.state == WorkInfo.State.FAILED }
                    .map { it.outputData.getString(ERRO) ?: "A API recusou a narração." }
            )
        }

    private fun etiqueta(ambienteId: String) = "narracao-$ambienteId"

    internal fun entrada(dados: Data): Pair<String, File>? {
        val ambiente = dados.getString(AMBIENTE) ?: return null
        val arquivo = dados.getString(ARQUIVO)?.let(::File) ?: return null
        return ambiente to arquivo
    }
}

class EnvioDeNarracaoWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val (ambienteId, arquivo) = EnvioDeNarracoes.entrada(inputData) ?: return Result.failure()
        if (!arquivo.exists()) return Result.failure(workDataOf(EnvioDeNarracoes.ERRO to "O arquivo da narração não existe mais."))
        val repositorio = (applicationContext as VisuraApplication).repositorio
        return try {
            val evidencia = repositorio.enviarNarracao(ambienteId, arquivo.readBytes())
            arquivo.delete()
            Result.success(workDataOf(EnvioDeNarracoes.EVIDENCIA to evidencia))
        } catch (e: ApiErro) {
            if (temporario(e)) {
                Result.retry()
            } else {
                arquivo.delete()
                Result.failure(workDataOf(EnvioDeNarracoes.ERRO to (e.message ?: "A API recusou a narração.")))
            }
        }
    }
}

// Sem resposta (rede), sessão expirada, limite de requisições ou erro do servidor: vale tentar de novo depois.
// As outras recusas (vistoria finalizada, ambiente removido, arquivo inválido) não mudam com o tempo.
internal fun temporario(erro: ApiErro): Boolean {
    val status = erro.status ?: return true
    return status == 401 || status == 408 || status == 429 || status >= 500
}
