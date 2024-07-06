package com.lymors.lycommons.data.viewmodels

import android.graphics.Bitmap
import android.net.Uri
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
    suspend fun uploadImageToFirebaseStorage(uri: Uri , result: (MyResult<String>) -> Unit = {}) {
        withContext(Dispatchers.IO){storageRepository.uploadImageToFirebaseStorageWithUri(uri ){
            result.invoke(it)
        } }
    }

    suspend fun uploadDocumentToFirebaseStorage(uri: Uri , result: (MyResult<String>) -> Unit = {}) {
        withContext(Dispatchers.IO){storageRepository.uploadDocumentToFirebaseStorage(uri ){
            result.invoke(it)
        } }
    }

    suspend fun uploadAudioToFirebaseStorage(uri: Uri , result: (MyResult<String>) -> Unit ={}) {
        withContext(Dispatchers.IO){storageRepository.uploadAudioToFirebaseStorage(uri ){
            result.invoke(it)
        } }
    }



    suspend fun uploadVideoToFirebaseStorage(uri: Uri , progressCallBack: (Int) -> Unit ={}, result: (MyResult<String>) -> Unit ) {
        withContext(Dispatchers.IO){storageRepository.uploadVideoToFirebaseStorage(uri ,{
            result.invoke(it)
        },{
            progressCallBack.invoke(it)
        })}
    }

    suspend fun uploadImageToFirebaseStorage(bitmap: Bitmap , result: (MyResult<String>) -> Unit = {}) {
        withContext(Dispatchers.IO){storageRepository.uploadImageToFirebaseStorageWithBitmap(bitmap ){
            result.invoke(it)
        } }
    }
    suspend fun deleteImageToFirebaseStorage(url: String, result: (MyResult<String>) -> Unit = {}){
        withContext(Dispatchers.IO){storageRepository.deleteImageToFirebaseStorage(url){
            result.invoke(it)
        } }
    }





}