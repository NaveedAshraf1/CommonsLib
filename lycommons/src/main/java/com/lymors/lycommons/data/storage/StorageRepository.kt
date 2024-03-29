package com.lymors.lycommons.data.storage

import android.graphics.Bitmap
import com.lymors.lycommons.utils.MyResult

interface StorageRepository {
    suspend fun uploadImageToFirebaseStorage(uri: String): MyResult<String>
    suspend fun uploadImageToFirebaseStorage(bitmap: Bitmap): MyResult<String>
    suspend fun deleteImageToFirebaseStorage(url: String): MyResult<String>

}