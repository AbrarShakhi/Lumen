package com.abrarshakhi.lumen.core.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

class KeystoreSecretCipher {

    class DecryptionFailed(message: String, cause: Throwable?) : Exception(message, cause)

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return "${iv.encode()}$SEPARATOR${ciphertext.encode()}"
    }

    fun decrypt(envelope: String): String {
        val parts = envelope.split(SEPARATOR)
        if (parts.size != 2) throw DecryptionFailed("Malformed secret envelope", null)

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(TAG_LENGTH_BITS, parts[0].decode()),
            )
            String(cipher.doFinal(parts[1].decode()), Charsets.UTF_8)
        } catch (cause: Exception) {
            throw DecryptionFailed("Stored secret could not be decrypted", cause)
        }
    }

    fun resetKey() {
        runCatching { keyStore().deleteEntry(KEY_ALIAS) }
    }

    private fun secretKey(): SecretKey {
        val existing = keyStore().getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        existing?.secretKey?.let { return it }
        return generateKey()
    }

    private fun generateKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)

        fun spec(strongBox: Boolean) = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .apply { if (strongBox) setIsStrongBoxBacked(true) }
            .build()

        return try {
            generator.init(spec(strongBox = true))
            generator.generateKey()
        } catch (_: StrongBoxUnavailableException) {
            generator.init(spec(strongBox = false))
            generator.generateKey()
        } catch (_: Exception) {
            generator.init(spec(strongBox = false))
            generator.generateKey()
        }
    }

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(PROVIDER).apply { load(null) }

    private fun ByteArray.encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "lumen.secrets.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val TAG_LENGTH_BITS = 128
        const val SEPARATOR = ":"
    }
}
