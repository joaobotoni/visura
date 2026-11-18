package com.visura.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

// Grava a narração do vistoriador em M4A (AAC mono, 64 kbps): uns 0,5 MB por minuto, bom para a transcrição e
// bem abaixo do limite de 25 MB da API (uns 50 minutos). Os arquivos ficam em files/narracoes até o envio
// terminar; o cache não serve, porque o Android pode limpá-lo antes de a narração chegar à API.
class Gravador(private val context: Context) {

    private var gravador: MediaRecorder? = null
    private var arquivo: File? = null

    val gravando: Boolean get() = gravador != null

    // Começa uma gravação nova. Falha (microfone ocupado, sem permissão) vira ApiErro com a mensagem pronta.
    fun iniciar() {
        check(gravador == null) { "Já existe uma gravação em andamento." }
        val destino = File(pasta(context), "${UUID.randomUUID()}.m4a")
        val novo = criar()
        try {
            novo.setAudioSource(MediaRecorder.AudioSource.MIC)
            novo.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            novo.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            novo.setAudioChannels(1)
            novo.setAudioSamplingRate(44_100)
            novo.setAudioEncodingBitRate(64_000)
            novo.setOutputFile(destino.absolutePath)
            novo.prepare()
            novo.start()
        } catch (e: Exception) {
            novo.release()
            destino.delete()
            throw ApiErro("Não foi possível usar o microfone: ${e.message ?: "ele está ocupado por outro app"}.")
        }
        gravador = novo
        arquivo = destino
    }

    fun pausar() {
        gravador?.pause()
    }

    fun continuar() {
        gravador?.resume()
    }

    // Encerra e devolve o arquivo gravado; null se a gravação não chegou a ter áudio (parada logo depois de começar).
    fun concluir(): File? {
        val atual = gravador ?: return null
        val destino = arquivo
        gravador = null
        arquivo = null
        val gravou = try {
            atual.stop()
            true
        } catch (_: RuntimeException) {
            // O MediaRecorder lança quando é parado sem nenhum quadro de áudio gravado
            false
        } finally {
            atual.release()
        }
        if (!gravou) destino?.delete()
        return destino?.takeIf { gravou && it.length() > 0 }
    }

    fun descartar() {
        concluir()?.delete()
    }

    @Suppress("DEPRECATION")
    private fun criar(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()

    companion object {
        fun pasta(context: Context): File = File(context.filesDir, "narracoes").apply { mkdirs() }
    }
}
