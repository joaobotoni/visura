package com.visura.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Cifra os valores da sessão antes de irem para o SharedPreferences.
interface Cifra {
    fun cifrar(texto: String): String

    // Lança exceção se o valor foi adulterado ou se a chave não existe mais.
    fun decifrar(cifrado: String): String
}

// AES-256-GCM com a chave guardada no Android Keystore: a chave não sai do aparelho
// (nem do hardware seguro, quando existe) e não vai para o backup.
// Formato gravado: Base64(IV de 12 bytes + texto cifrado com a tag de 16 bytes).
class CifraKeystore(private val alias: String = ALIAS) : Cifra {

    override fun cifrar(texto: String): String {
        val cipher = Cipher.getInstance(TRANSFORMACAO)
        cipher.init(Cipher.ENCRYPT_MODE, chave())
        val cifrado = cipher.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + cifrado, Base64.NO_WRAP)
    }

    override fun decifrar(cifrado: String): String {
        val bytes = Base64.decode(cifrado, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMACAO)
        cipher.init(Cipher.DECRYPT_MODE, chaveExistente(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
        return String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(PROVEDOR).apply { load(null) }

    private fun chaveExistente(): SecretKey =
        keyStore().getKey(alias, null) as? SecretKey ?: error("Chave da sessão não encontrada")

    @Synchronized
    private fun chave(): SecretKey = keyStore().getKey(alias, null) as? SecretKey ?: gerarChave()

    private fun gerarChave(): SecretKey {
        val gerador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVEDOR)
        gerador.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gerador.generateKey()
    }

    // Apaga a chave (usado nos testes para simular a perda da chave).
    fun apagarChave() = keyStore().deleteEntry(alias)

    private companion object {
        const val PROVEDOR = "AndroidKeyStore"
        const val ALIAS = "visura_sessao"
        const val TRANSFORMACAO = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
