package com.lymors.lycommons.managers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment

class MyDownloadManager {


    // Download a file from a URL
    class DownloadManagerHelper(private val context: Context) {

        private val downloadManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        fun downloadFile(url: String, fileName: String, downloadCallback: (String) -> Unit) {
            val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle("Downloading...")
                .setDescription("Downloading $fileName")

            val downloadId = downloadManager.enqueue(request)

            // Register broadcast receiver for download completion
            val downloadCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        val downloadedFilePath = getDownloadedFilePath(downloadId)
                        if (downloadedFilePath != null) {
                            downloadCallback(downloadedFilePath) // Call the callback with URI
                        }
                        // Unregister receiver after completion (optional)
                        context?.unregisterReceiver(this)
                    }
                }
            }

            context.registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        private fun getDownloadedFilePath(downloadId: Long): String? {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val localUriString = cursor.getString(columnIndex)
                cursor.close()
                return localUriString
            }
            cursor.close()
            return null
        }
    }




}