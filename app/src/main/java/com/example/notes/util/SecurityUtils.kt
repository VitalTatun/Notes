package com.example.notes.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val HASH_FORMAT = "pbkdf2_sha256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    fun createHash(input: String): String {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(input, salt, ITERATIONS, KEY_LENGTH)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        return "$HASH_FORMAT$$ITERATIONS$$saltBase64$$hashBase64"
    }

    fun verifyHash(input: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false

        val parts = storedHash.split("$")
        if (parts.size == 4 && parts[0] == HASH_FORMAT) {
            val iterations = parts[1].toIntOrNull() ?: return false
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[3], Base64.NO_WRAP)
            val actualHash = pbkdf2(input, salt, iterations, expectedHash.size * 8)
            return MessageDigest.isEqual(actualHash, expectedHash)
        }

        return legacySha256(input) == storedHash
    }

    private fun pbkdf2(input: String, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        val spec = PBEKeySpec(input.toCharArray(), salt, iterations, keyLength)
        return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
    }

    private fun legacySha256(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}
