package com.lymors.lycommons.data.storage

import android.graphics.Bitmap
import android.net.Uri
import com.lymors.lycommons.utils.MyResult

interface StorageRepository {
    suspend fun uploadImageToFirebaseStorageWithUri(uri: Uri , result: (MyResult<String>) -> Unit)
    suspend fun uploadDocumentToFirebaseStorage(uri: Uri, result: (MyResult<String>) -> Unit)
    suspend fun uploadAudioToFirebaseStorage(uri: Uri, result: (MyResult<String>) -> Unit)
    suspend fun uploadImageToFirebaseStorageWithBitmap(bitmap: Bitmap , result: (MyResult<String>) -> Unit)
    suspend fun uploadImageToFirebaseStorageWithBitmap(bitArray: ByteArray , result: (MyResult<String>) -> Unit)
    suspend fun deleteImageToFirebaseStorage(url: String, result: (MyResult<String>) -> Unit)
    suspend fun uploadVideoToFirebaseStorage(videoUri: Uri, result: (MyResult<String>)-> Unit, progressCallBack: (Int) -> Unit)
}