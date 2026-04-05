package com.example.notes.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {
    fun shareJsonFile(context: Context, jsonString: String, fileName: String) {
        try {
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(jsonString)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Экспорт данных"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
