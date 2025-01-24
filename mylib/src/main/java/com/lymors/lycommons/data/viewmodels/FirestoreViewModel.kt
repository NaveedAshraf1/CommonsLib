                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          package com.lymors.lycommons.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lymors.lycommons.data.database.FirestoreRepositoryImpl
import com.lymors.lycommons.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class FirestoreViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepositoryImpl
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _searchingState = MutableStateFlow(false)
    val searchingState = _searchingState.asStateFlow()

    private val _anyState = MutableStateFlow(Any())
    val anyState = _anyState.asStateFlow()

    val map = HashMap<Class<*>, AlphaModel<*>>()

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setAnyState(any: Any) {
        _anyState.value = any
    }

    inline fun <reified T> getAnyStateValue(): T {
        return anyState.value as T
    }

    fun <T> getAnyStateFlow(): StateFlow<T> {
        return anyState as StateFlow<T>
    }

    suspend fun checkExists(path: String): MyResult<String> {
        return firestoreRepo.checkExists(path)
    }

    suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        return firestoreRepo.updateAnyModel(path, model as Map<String, Any>)
    }

    suspend fun <T> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): List<T> {
        return firestoreRepo.getAllChildByKeys(path, keys, clazz)
    }

    suspend fun <T> collectAnyModels(
        path: String, 
        clazz: Class<T>, 
        numberOfItems: Int = 0
    ): StateFlow<List<T>> {
        return suspendCancellableCoroutine { continuation ->
            if (map.containsKey(clazz) && 
                map[clazz]?.path == path && 
                map[clazz]?.more == numberOfItems
            ) {
                continuation.resume(map[clazz]?.stateFlow as StateFlow<List<T>>)
            } else {
                val mutableStateFlow = MutableStateFlow<List<T>>(mutableListOf())
                val stateFlow = mutableStateFlow.asStateFlow()
                val alphaModel = AlphaModel(path, mutableStateFlow, numberOfItems)
                alphaModel.stateFlow = stateFlow
                map[clazz] = alphaModel

                viewModelScope.launch {
                    // Since Firestore doesn't have a direct equivalent to Firebase Realtime Database's limitToLast,
                    // we'll simulate it by getting all documents and taking the last N
                    val documents = firestoreRepo.getAllChildByKeys(path, emptyList(), clazz)
                    val limitedDocuments = if (numberOfItems > 0) {
                        documents.takeLast(numberOfItems)
                    } else {
                        documents
                    }
                    mutableStateFlow.value = limitedDocuments
                }
                continuation.resume(map[clazz]?.stateFlow as StateFlow<List<T>>)
            }
        }
    }
}
