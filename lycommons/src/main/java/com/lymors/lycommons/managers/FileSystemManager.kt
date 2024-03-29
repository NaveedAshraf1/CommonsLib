package com.lymors.lycommons.managers

import android.app.Activity
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FileSystemManager(val context: Activity) {

    val BUFFER_SIZE = 1024

    suspend fun saveFile(text: String, fileName: String, context: Activity): Uri? {
        val textDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!textDirectory.exists()) {
            textDirectory.mkdir()
        }
        val file = File(textDirectory, "$fileName.txt")

        withContext(Dispatchers.IO) {
            FileOutputStream(file).use {
                it.write(text.toByteArray())
            }
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Text File Created", Toast.LENGTH_SHORT).show()
        }
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            null
        }
    }

    suspend fun readFile(uri: Uri): File? {
        val resolver = context.contentResolver
        if (resolver.getType(uri)?.startsWith("text/") == true) {
            return null
        }
        return try {
            val inputStream = resolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("temp_file", null, context.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream, BUFFER_SIZE)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }




}