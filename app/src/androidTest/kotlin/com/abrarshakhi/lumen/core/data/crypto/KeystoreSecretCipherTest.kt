package com.abrarshakhi.lumen.core.data.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class KeystoreSecretCipherTest {

    private val cipher = KeystoreSecretCipher()

    @After
    fun tearDown() = cipher.resetKey()

    @Test
    fun roundTrip_returnsTheOriginal() {
        val secret = "sk-test-0123456789-ABCDEF"

        assertEquals(secret, cipher.decrypt(cipher.encrypt(secret)))
    }

    @Test
    fun ciphertext_doesNotContainThePlaintext() {
        val secret = "super-secret-api-key"

        val envelope = cipher.encrypt(secret)

        assertTrue(!envelope.contains(secret), "the stored value must not leak the plaintext")
    }

    @Test
    fun encryptingTwice_producesDifferentCiphertext() {
        val secret = "same-input"

        assertNotEquals(cipher.encrypt(secret), cipher.encrypt(secret))
    }

    @Test
    fun tamperedCiphertext_isRejected() {
        val envelope = cipher.encrypt("original")
        val (iv, body) = envelope.split(":")
        val flipped = body.replaceRange(0, 1, if (body.first() == 'A') "B" else "A")

        assertFailsWith<KeystoreSecretCipher.DecryptionFailed> {
            cipher.decrypt("$iv:$flipped")
        }
    }

    @Test
    fun malformedEnvelope_isRejected() {
        assertFailsWith<KeystoreSecretCipher.DecryptionFailed> { cipher.decrypt("not-an-envelope") }
        assertFailsWith<KeystoreSecretCipher.DecryptionFailed> { cipher.decrypt("") }
    }

    @Test
    fun afterKeyReset_oldCiphertextIsUnreadable() {
        val envelope = cipher.encrypt("will-be-lost")
        cipher.resetKey()

        assertFailsWith<KeystoreSecretCipher.DecryptionFailed> { cipher.decrypt(envelope) }
    }

    @Test
    fun unicodeAndLongSecrets_surviveIntact() {
        val secret = "ключ-🔑-" + "x".repeat(2000)

        assertEquals(secret, cipher.decrypt(cipher.encrypt(secret)))
    }
}
