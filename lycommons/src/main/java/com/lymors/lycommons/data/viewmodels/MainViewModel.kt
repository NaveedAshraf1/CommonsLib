package com.lymors.lycommons.data.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lymors.lycommons.data.database.MainRepository
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


class MainViewModel @Inject constructor(private val mainRepo: MainRepository) : ViewModel() {
    private val _longClickedState = MutableStateFlow<Boolean>(false)
    val longClickedState = _longClickedState.asStateFlow()


    private  val _searchingState = MutableStateFlow(false)
    val searchingState = _searchingState.asStateFlow()



    val _anyState = MutableStateFlow(Any())
    val anyState = _anyState.asStateFlow()

    fun setAnyState(any: Any) {
        _anyState.value = any
    }
    fun <T> getAnyStateFlow(): StateFlow<T> {
        return anyState as StateFlow<T>
    }
    suspend fun checkExists(path: String): MyResult<String> {
        return mainRepo.checkExists(path)
    }

    suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        return mainRepo.uploadAnyModel(path, model)
    }

    suspend fun deleteAnyModel(child: String): MyResult<String> {
        return mainRepo.deleteAnyModel(child)
    }

    val map = HashMap<Class<*>, AlphaModel<*>>()
    suspend fun <T> collectAnyModels(path: String, clazz: Class<T>): StateFlow<List<T>> {
        return suspendCancellableCoroutine { continuation ->
            if (map.containsKey(clazz) && map[clazz]?.path ==path ) {
                continuation.resume(map[clazz]?.stateFlow as StateFlow<List<T>>)
            } else {
                val mutableStateFlow = MutableStateFlow<List<T>>(emptyList())
                val stateFlow = mutableStateFlow.asStateFlow()
                val a = AlphaModel(path, mutableStateFlow)
                a.stateFlow = stateFlow
                map[clazz] = a
                viewModelScope.launch {
                    mainRepo.collectAnyModel(path, clazz).collect {
                        mutableStateFlow.value = it
                    }
                }
                continuation.resume(map[clazz]?.stateFlow as StateFlow<List<T>>)
            }
        }
    }

    suspend fun <T : Any> getMap(child: String, clazz: Class<T>): MyResult<Map<String, T>> {
        return mainRepo.getMap(child, clazz)
    }

    private  val _mapFlow = MutableStateFlow(emptyMap<String,Any>())
    val mapFlow = _mapFlow.asStateFlow()
    fun collectMap(child: String) {
        viewModelScope.launch {
            mainRepo.collectMap<Any>(child).collect{
                _mapFlow.value = it
            }
        }
    }

    suspend fun <T> getAnyData(path: String, clazz: Class<T>): T? {
        return mainRepo.getAnyData(path, clazz)
    }

    suspend fun <T> getModelsWithChildren(path: String, clazz: Class<T>): Flow<List<T>> {
        return mainRepo.getModelsWithChildren(path, clazz)
    }

    fun setLongClickedState(longClicked: Boolean) {
        _longClickedState.value = longClicked
    }
    fun setSearchingState(searching: Boolean) {
        _searchingState.value = searching
    }
}


data class AlphaModel<T>(
    var path: String,
    var _stateFlow:MutableStateFlow<List<T>>,

    ){
    var stateFlow:StateFlow<List<T>> = _stateFlow.asStateFlow()
}