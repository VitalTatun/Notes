package com.example.notes.util

import java.security.MessageDigest

object SecurityUtils {
    fun hashString(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}
