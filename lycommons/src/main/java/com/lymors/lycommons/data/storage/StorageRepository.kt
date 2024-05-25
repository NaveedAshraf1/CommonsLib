package com.lymors.lycommons.data.storage

import android.graphics.Bitmap
import android.net.Uri
import com.lymors.lycommons.utils.MyResult

interface StorageRepository {
    suspend fun uploadImageToFirebaseStorageWithUri(uri: Uri , name: String = ""): MyResult<String>
    suspend fun uploadDocumentToFirebaseStorage(uri: Uri , name: String = ""): MyResult<String>
    suspend fun uploadImageToFirebaseStorageWithBitmap(bitmap: Bitmap , name: String = ""): MyResult<String>
    suspend fun deleteImageToFirebaseStorage(url: String): MyResult<String>
    suspend fun uploadVideoToFirebaseStorage(videoUri: Uri, progressCallBack: (Int) -> Unit): MyResult<String>

}