package com.visura.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val LADO_MAXIMO = 2048
private const val LIMITE_BYTES = 10 * 1024 * 1024 // a API aceita até 10 MB

// Lê a foto escolhida pelo usuário e devolve um JPEG reduzido, no formato que a API aceita (JPEG ou PNG).
// Converter sempre para JPEG resolve fotos em HEIC/WebP da galeria e mantém o arquivo bem abaixo do limite.
suspend fun prepararFoto(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
    if (limites.outWidth <= 0 || limites.outHeight <= 0) throw ApiErro("Não foi possível ler a foto escolhida.")

    var amostra = 1
    while (maxOf(limites.outWidth, limites.outHeight) / amostra > LADO_MAXIMO) amostra *= 2

    val bitmap = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
    } ?: throw ApiErro("Não foi possível ler a foto escolhida.")

    val saida = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, saida)
    bitmap.recycle()
    saida.toByteArray().also {
        if (it.size > LIMITE_BYTES) throw ApiErro("A foto ficou maior que 10 MB.")
    }
}
