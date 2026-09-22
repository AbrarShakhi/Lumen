package com.abrarshakhi.lumen.core.data.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Instrumented because `AndroidKeyStore` has no JVM equivalent — this is one of the few
 * things that genuinely cannot be unit-tested.
 */
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
        // A fresh IV per encryption. Reusing one under GCM is catastrophic, so identical
        // output for identical input would be a red flag.
        val secret = "same-input"

        assertNotEquals(cipher.encrypt(secret), cipher.encrypt(secret))
    }

    @Test
    fun tamperedCiphertext_isRejected() {
        // GCM is authenticated: a modified envelope must fail loudly rather than decrypt
        // to plausible garbage.
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
        // Models a rotated or invalidated key: the store must treat this as "the secret is
        // gone" and prompt for re-entry, not retry forever.
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
