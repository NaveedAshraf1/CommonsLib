package com.lymors.lycommons.data.storage

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storage
import com.google.zxing.common.BitArray
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(private val storageReference: StorageReference,private val storage:FirebaseStorage):
    StorageRepository {

    override suspend fun uploadImageToFirebaseStorageWithUri(
        uri: Uri,
    ): MyResult<String> {
        return try {
            val filename =  (uri.lastPathSegment) ?: System.currentTimeMillis()
            val imageRef = storageReference.child("images/${filename}")
            val uploadTask = imageRef.putFile(uri)
            val result: UploadTask.TaskSnapshot = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun uploadDocumentToFirebaseStorage(uri: Uri): MyResult<String> {
       return uploadImageToFirebaseStorageWithUri(uri)
    }



    override suspend fun uploadImageToFirebaseStorageWithBitmap(bitmap: Bitmap ): MyResult<String> {
        return try {
            var n = "images/${System.currentTimeMillis()}"
            val imageRef = storageReference.child("images/${n}")
            val byteArray = bitmapToByteArray(bitmap)
            val uploadTask = imageRef.putBytes(byteArray)
            val result= uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun uploadImageToFirebaseStorageWithBitmap(bitArray: ByteArray): MyResult<String> {
        return try {
            var n = "images/${System.currentTimeMillis()}"
            val imageRef = storageReference.child("images/${n}")
            val uploadTask = imageRef.putBytes(bitArray)
            val result= uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteImageToFirebaseStorage(url: String): MyResult<String> {
        return try {
            val storageRef = storage.getReferenceFromUrl(url)
            val deleteTask: Task<Void> = storageRef.delete()
            Tasks.await(deleteTask)
            MyResult.Success("Image deleted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            MyResult.Error("Failed to delete image: ${e.message}")
        }
    }



    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }



    override suspend fun uploadVideoToFirebaseStorage(
        videoUri: Uri,
        progressCallBack: (Int) -> Unit
    ): MyResult<String> {
        return try {
            val storageReference = Firebase.storage.reference
            val uploadTask = storageReference.child("${System.currentTimeMillis()}.mp4").putFile(videoUri)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressCallBack(progress)
            }
            val result = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

}