package com.lymors.lycommons.data.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.lymors.lycommons.data.storage.StorageRepository
import com.lymors.lycommons.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class StorageViewModel @Inject constructor(private val storageRepository: StorageRepository) : ViewModel() {


    // fire base storage
    suspend fun uploadImageToFirebaseStorage(uri: String): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.uploadImageToFirebaseStorage(uri)}
    }
    suspend fun uploadImageToFirebaseStorage(bitmap: Bitmap): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.uploadImageToFirebaseStorage(bitmap)}
    }
    suspend fun deleteImageToFirebaseStorage(url: String): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.deleteImageToFirebaseStorage(url)}
    }





}