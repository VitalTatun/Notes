package com.example.notes.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasscodeSecurityManager @Inject constructor() {

    fun createHash(raw: String): Pair<String, String> {
        val saltBytes = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val hash = hash(raw, saltBytes)
        return hash to Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    fun verify(raw: String, expectedHash: String, salt: String): Boolean {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        return hash(raw, saltBytes) == expectedHash
    }

    private fun hash(raw: String, salt: ByteArray): String {
        val spec = PBEKeySpec(raw.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = keyFactory.generateSecret(spec).encoded
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_BYTES = 16
        val secureRandom = SecureRandom()
    }
}
