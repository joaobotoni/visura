package com.visura.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val LADO_MAXIMO = 2048
private const val LIMITE_BYTES = 10 * 1024 * 1024 // a API aceita até 10 MB

// Lê a foto escolhida pelo usuário e devolve um JPEG reduzido, no formato que a API aceita (JPEG ou PNG).
// Converter sempre para JPEG resolve fotos em HEIC/WebP da galeria e mantém o arquivo bem abaixo do limite.
// A rotação do EXIF é aplicada nos pixels: o JPEG novo não leva o EXIF, e sem isso a foto da câmera sairia deitada.
suspend fun prepararFoto(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
    try {
        reduzirFoto(context, uri)
    } catch (e: ApiErro) {
        throw e
    } catch (_: IOException) {
        throw ApiErro("Não foi possível ler a foto escolhida.")
    } catch (_: SecurityException) {
        throw ApiErro("Sem permissão para ler a foto escolhida.")
    } catch (_: OutOfMemoryError) {
        throw ApiErro("A foto é grande demais para ser processada.")
    }
}

private fun reduzirFoto(context: Context, uri: Uri): ByteArray {
    val resolver = context.contentResolver

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
    if (limites.outWidth <= 0 || limites.outHeight <= 0) throw ApiErro("Não foi possível ler a foto escolhida.")

    var amostra = 1
    while (maxOf(limites.outWidth, limites.outHeight) / amostra > LADO_MAXIMO) amostra *= 2

    val lida = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
    } ?: throw ApiErro("Não foi possível ler a foto escolhida.")
    val bitmap = endireitar(lida, rotacao(context, uri))

    val saida = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, saida)
    bitmap.recycle()
    return saida.toByteArray().also {
        if (it.size > LIMITE_BYTES) throw ApiErro("A foto ficou maior que 10 MB.")
    }
}

// Graus de rotação gravados no EXIF (0 quando não há EXIF, como em PNG)
private fun rotacao(context: Context, uri: Uri): Int = try {
    context.contentResolver.openInputStream(uri)?.use { entrada ->
        when (ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } ?: 0
} catch (_: IOException) {
    0
}

private fun endireitar(bitmap: Bitmap, graus: Int): Bitmap {
    if (graus == 0) return bitmap
    val girada = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(graus.toFloat()) }, true)
    if (girada != bitmap) bitmap.recycle()
    return girada
}

// Arquivo temporário para a câmera gravar a foto, exposto pelo FileProvider (res/xml/arquivos.xml).
// A câmera do sistema grava no Uri; depois a foto passa por prepararFoto como as da galeria.
fun novaFotoDaCamera(context: Context): Uri {
    val pasta = java.io.File(context.cacheDir, "fotos").apply { mkdirs() }
    val arquivo = java.io.File.createTempFile("foto_", ".jpg", pasta)
    return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.arquivos", arquivo)
}
