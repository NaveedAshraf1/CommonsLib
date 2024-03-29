package com.lymors.lycommons.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lymors.lycommons.data.auth.email.AuthRepositoryWithEmail
import com.lymors.lycommons.data.auth.phone.AuthRepositoryWithPhone
import com.lymors.lycommons.data.database.MainRepository
import com.lymors.lycommons.data.storage.StorageRepository
import com.lymors.lycommons.models.ParentModel
import com.lymors.lycommons.models.StudentModel
import com.lymors.lycommons.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepo: MainRepository, private val storageRepository: StorageRepository, private val authRepositoryWithEmail: AuthRepositoryWithEmail, private val authRepositoryWithPhone: AuthRepositoryWithPhone) : ViewModel() {

    private val _studentFlow = MutableStateFlow<List<StudentModel>>(emptyList())
    val studentFlow = _studentFlow.asStateFlow()


    var visitMap = HashSet<String>()


    fun <T>collectAnyModels(path:String, clazz: Class<T>) {

        if (!visitMap.contains(path)) {
            viewModelScope.launch {
                mainRepo.collectAnyModel(path, clazz).collect {
                    when (clazz) {
                        StudentModel::class.java -> { _studentFlow.value = it as List<StudentModel>
                        }

                    }
                }
            }
            visitMap.add(path)
        }
    }








    suspend fun uploadAnyModel(child: String, model: ParentModel): MyResult<String> {
        return withContext(Dispatchers.IO){mainRepo.uploadAnyModel(child, model)}
    }


    suspend fun<T> getAnyModelByKey(child: String, key: String , clazz:Class<T>): T?{
        return withContext(Dispatchers.IO){mainRepo.getAnyModelByKey(child, key , clazz)}
    }




    suspend fun updateAnyModel(child: String, model: ParentModel): MyResult<String> {
        return withContext(Dispatchers.IO){mainRepo.updateAnyModel(child, model)}
    }


    suspend fun deleteAnyModel(child: String, key: String): MyResult<String> {
        return withContext(Dispatchers.IO){mainRepo.deleteAnyModel(child, key)}
    }

}