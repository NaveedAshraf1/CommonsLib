package com.lymors.lycommons.utils

import android.content.Context
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.lymors.lycommons.utils.MyExtensions.logT

class FirebaseUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

            val imageUri = inputData.getString("uri") ?: return Result.failure()
            val databasePath = inputData.getString("path") ?: return Result.failure()
            databasePath.logT("worker initialized")
       return try {
           "try".logT()

            val storageRef = FirebaseStorage.getInstance().reference.child("images/${Uri.parse(imageUri).lastPathSegment}")
            val uploadTask = storageRef.putFile(Uri.parse(imageUri))

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    if (databasePath.isNotEmpty()){
                    updateDatabase(databasePath, downloadUri.toString())
                    }
                } else {

                    task.exception?.message?.logT()
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.message?.logT("error::::")
           Result.retry()
        }
    }

    private fun updateDatabase(databasePath: String, imageUrl: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference(databasePath)
        databaseRef.setValue(imageUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Image URL updated successfully
            } else {
                // Handle failures
            }
        }
    }
}
