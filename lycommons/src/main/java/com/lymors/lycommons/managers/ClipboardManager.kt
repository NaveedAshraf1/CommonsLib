package com.lymors.lycommons.managers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

class ClipboardManager(private val context: Context) {

    private var clipboardManager: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun copyText(text: String) {

        val clip = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clip)

        // Optionally, show a toast indicating that the text has been copied
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun getCopiedText(): String {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            return item.text.toString()
        }
        return ""
    }
}
