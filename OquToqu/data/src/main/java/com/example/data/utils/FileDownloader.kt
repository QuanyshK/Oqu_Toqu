package com.example.data.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object FileDownloader {
    fun downloadPdfToDownloads(context: Context, url: String): File? {
        return try {
            val input = URL(url).openStream()
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloads, "article_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { output -> input.copyTo(output) }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

