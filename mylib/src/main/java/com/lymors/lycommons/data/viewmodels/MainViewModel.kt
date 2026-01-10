package com.lymors.lycommons.data.viewmodels


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.lymors.lycommons.data.database.MainRepository
import com.lymors.lycommons.extensions.DataExtensions.isNull
import com.lymors.lycommons.extensions.ImageViewExtensions.createImageUploadWorkRequest
import com.lymors.lycommons.extensions.ImageViewExtensions.uploadImageUsingWorkManager
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.reflect.KProperty

class MainViewModel @Inject constructor(private val mainRepo: MainRepository) : ViewModel() {
    private val _longClickedState = MutableStateFlow(false)
    val longClickedState = _longClickedState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private  val _searchingState = MutableStateFlow(false)
    val searchingState = _searchingState.asStateFlow()

    val _anyState = MutableStateFlow(Any())
    val anyState = _anyState.asStateFlow()

    fun setQuery(query:String) {
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
        return mainRepo.checkExists(path)
    }

    suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        return mainRepo.uploadAnyModel(path, model)
    }

    suspend fun deleteAnyModel(child: String): MyResult<String> {
        return mainRepo.deleteAnyModel(child)
    }

    suspend fun <T : Any> uploadAllModelsAtOnce(path: String, models: List<T>): MyResult<String> {
        return mainRepo.uploadAllModelsAtOnce(path, models)
    }
    suspend fun <T : Any> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): MyResult<List<T>> {
        return mainRepo.getAllChildByKeys(path, keys, clazz)
    }


    val map = HashMap<Class<*>, AlphaModel<*>>()
    suspend fun <T : Any> collectAnyModels(path: String, clazz: Class<T> ,  numberOfItems: Int = 0): StateFlow<MyResult<List<T>>> {
        return suspendCancellableCoroutine { continuation ->
            if (map.containsKey(clazz) && map[clazz]?.path ==path && map[clazz]?.more == numberOfItems ) {
                continuation.resume(map[clazz]?.stateFlow as StateFlow<MyResult<List<T>>>)
            } else {
                val mutableStateFlow = MutableStateFlow<MyResult<List<T>>>(MyResult.Success(emptyList()))
                val stateFlow = mutableStateFlow.asStateFlow()
                val a = AlphaModel(path, mutableStateFlow , numberOfItems )
                a.stateFlow = stateFlow
                map[clazz] = a
                viewModelScope.launch {
                    try {
                        mainRepo.collectAnyModel(path, clazz,numberOfItems).collect { result ->
                            mutableStateFlow.value = result
                        }
                    } catch (e: Exception) {
                        mutableStateFlow.value = MyResult.Error(e.message ?: "Unknown error")
                    }
                }
                continuation.resume(map[clazz]?.stateFlow as StateFlow<MyResult<List<T>>>)
            }
        }
    }



    suspend fun  getMap(child: String): MyResult<Map<String, String>> {
        return mainRepo.getMap(child)
    }

    private  val _mapFlow = MutableStateFlow<MyResult<Map<String,Any>>>(MyResult.Success(emptyMap()))
    val mapFlow = _mapFlow.asStateFlow()

    fun collectMap(child: String): StateFlow<MyResult<Map<String,Any>>> {
        viewModelScope.launch {
            try {
                mainRepo.collectMap<Any>(child).collect { result ->
                    _mapFlow.value = result
                }
            } catch (e: Exception) {
                _mapFlow.value = MyResult.Error(e.message ?: "Unknown error")
            }
        }
        return mapFlow
    }

    suspend fun <T : Any> getAnyData(path: String, clazz: Class<T>): MyResult<T> {
        return mainRepo.getAnyData(path, clazz)
    }

    fun <T : Any> getModelsWithChildren(path: String, clazz: Class<T>): Flow<MyResult<List<T>>> {
        return mainRepo.getModelsWithChildren(path, clazz)
    }

    suspend fun <T : Any> queryModelByAProperty(path: String, clazz: Class<T>, property: String, value: String): MyResult<T> {
        return mainRepo.queryModelByAProperty(path, property, value , clazz)
    }

    suspend fun <T : Any> queryList(path: String, clazz: Class<T>, field: String, value: String): MyResult<List<T>> {
        return mainRepo.queryList(path, field, value, clazz)
    }

    fun setLongClickedState(longClicked: Boolean) {
        _longClickedState.value = longClicked
    }
    fun setSearchingState(searching: Boolean) {
        _searchingState.value = searching
    }

    private val singleModelMap = HashMap<Class<*>, SingleAlphaModel<*>>()

    var c:Class<*>? = null
    fun <T : Any> collectSingleModel():StateFlow<MyResult<T>>{
        return singleModelMap[c]?.stateFlow as StateFlow<MyResult<T>>
    }
    fun <T : Any> collectSingleModel(clazz:Class<T>):StateFlow<MyResult<T>>{
       return singleModelMap[clazz]?.stateFlow as StateFlow<MyResult<T>>
    }
    suspend fun <T : Any> collectSingleModel(path: String, clazz: Class<T>): StateFlow<MyResult<T>> {
        if (c.isNull()){c = clazz}
        return suspendCancellableCoroutine { continuation ->
            if (singleModelMap.containsKey(clazz) && singleModelMap[clazz]?.path == path) {
                continuation.resume(singleModelMap[clazz]?.stateFlow as StateFlow<MyResult<T>>)
            } else {
                val mutableStateFlow = MutableStateFlow<MyResult<T>>(MyResult.Error("No data"))
                val stateFlow = mutableStateFlow.asStateFlow()
                val a = SingleAlphaModel(path, mutableStateFlow)
                a.stateFlow = stateFlow
                singleModelMap[clazz] = a
                viewModelScope.launch {
                    try {
                        mainRepo.collectAModel(path, clazz).collect { result ->
                            mutableStateFlow.value = result
                        }
                    } catch (e: Exception) {
                        mutableStateFlow.value = MyResult.Error(e.message ?: "Unknown error")
                    }
                }
                continuation.resume(singleModelMap[clazz]?.stateFlow as StateFlow<MyResult<T>>)
            }
        }
    }

    suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String>{
        return mainRepo.updateAnyModel(path, updatedMap)
    }

    suspend fun uploadModelWithImages(
        context: Context,
        realTimePath: String,
        model: Any,
        imageUris: List<String>,
        properties: List<KProperty<*>>
    ): MyResult<String> {
        return viewModelScope.async {
            // Step 1: Check if Firebase Storage is initialized
            try {
                val storage = FirebaseStorage.getInstance()
                storage.reference // Accessing root reference to trigger initialization check
            } catch (e: IllegalStateException) {
                throw StorageNotEnabledException("Firebase Storage is not initialized. Enable it in the Firebase Console.")
            } catch (e: Exception) {
                throw StorageNotEnabledException("Failed to access Firebase Storage: ${e.message}")
            }

            // Step 2: Upload the model to Realtime Database
            val modelKeyResult = uploadAnyModel(realTimePath, model)
            if (modelKeyResult is MyResult.Error) {
                return@async modelKeyResult // Return error if model upload fails
            }

            // Step 3: Validate the input lists
            if (imageUris.isEmpty()) {
                return@async MyResult.Error("imageUris list is empty")
            }
            if (imageUris.size != properties.size) {
                return@async MyResult.Error("imageUris and properties lists must have the same size")
            }

            // Step 4: Schedule image uploads if model upload succeeded
            if (modelKeyResult is MyResult.Success) {
                val key = modelKeyResult.data
                for (i in imageUris.indices) {
                    val imageUri = imageUris[i]
                    val property = properties[i]
                    val imagePath = "$realTimePath/$key/${property.name}"
                    try {
                        uploadImageUsingWorkManager(context, imageUri, imagePath)
                    } catch (e: StorageException) {
                        when (e.errorCode) {
                            StorageException.ERROR_UNKNOWN -> {
                                throw StorageNotEnabledException("Firebase Storage may not be enabled or is inaccessible: ${e.message}")
                            }
                            StorageException.ERROR_NOT_AUTHENTICATED,
                            StorageException.ERROR_NOT_AUTHORIZED -> {
                                throw StorageNotEnabledException("Authentication error with Firebase Storage: ${e.message}. Check Firebase setup.")
                            }
                            else -> {
                                return@async MyResult.Error("Image upload scheduling failed for ${property.name}: ${e.message} (Error code: ${e.errorCode})")
                            }
                        }
                    }
                }
                return@async modelKeyResult // Return model key if all images are scheduled
            }

            return@async modelKeyResult // Fallback return (shouldn’t reach here due to earlier checks)
        }.await()
    }

    suspend fun <P> uploadModelWithImage(
        context: Context,
        realTimePath: String,
        model: Any,
        imageUri: String,
        property: KProperty<P>
    ): MyResult<String> {
        return viewModelScope.async {
            // Step 1: Check if Firebase Storage is initialized
            try {
                val storage = FirebaseStorage.getInstance()
                // Accessing root reference to trigger initialization check
                storage.reference
            } catch (e: IllegalStateException) {
                // Thrown if FirebaseApp isn’t initialized properly
                throw StorageNotEnabledException("Firebase Storage is not initialized. Enable it in the Firebase Console.")
            } catch (e: Exception) {
                // Catch other unexpected initialization errors
                throw StorageNotEnabledException("Failed to access Firebase Storage: ${e.message}")
            }

            // Step 2: Proceed with model upload
            val modelKeyResult = uploadAnyModel(realTimePath, model)
            if (modelKeyResult is MyResult.Error) {
                return@async modelKeyResult // Return the error result immediately
            }

            // Step 3: Handle image upload if applicable
            if (imageUri.isEmpty()) {
                return@async MyResult.Error("imageUri is empty")
            }

            if (modelKeyResult is MyResult.Success) {
                val imagePath = "$realTimePath/${modelKeyResult.data}/${property.name}"
                try {
                    uploadImageUsingWorkManager(context, imageUri, imagePath)
                } catch (e: StorageException) {
                    // Check specific StorageException error codes
                    when (e.errorCode) {
                        StorageException.ERROR_UNKNOWN -> {
                            // Could indicate Storage isn’t enabled or a general failure
                            throw StorageNotEnabledException("Firebase Storage may not be enabled or is inaccessible: ${e.message}")
                        }
                        StorageException.ERROR_NOT_AUTHENTICATED,
                        StorageException.ERROR_NOT_AUTHORIZED -> {
                            // Indicates auth issues, which could mean Storage isn’t properly set up
                            throw StorageNotEnabledException("Authentication error with Firebase Storage: ${e.message}. Check Firebase setup.")
                        }
                        else -> {
                            // Other errors (e.g., file not found, network issues)
                            return@async MyResult.Error("Image upload failed: ${e.message} (Error code: ${e.errorCode})")
                        }
                    }
                }
                return@async modelKeyResult
            }

            return@async modelKeyResult
        }.await()
    }

    // Custom exception class for clarity
    class StorageNotEnabledException(message: String) : Exception(message)
    fun uploadImagesToFirebaseStorage(context: Context, path: String, imagesMap: HashMap<String, String> // Map of field names to URIs onCompletion: () -> Unit
    ) {
        viewModelScope.launch {
            val workManager = WorkManager.getInstance(context)
            val workRequests = mutableListOf<OneTimeWorkRequest>()

            for ((fieldName, imageUri) in imagesMap) {
                val workRequest = createImageUploadWorkRequest( imageUri, "$path/$fieldName")
                workRequests.add(workRequest)
            }
            // Enqueue all the work requests
            workManager.enqueue(workRequests)
        }
    }

    

    suspend fun <T : Any> getAnyModelsList(path: String, clazz: Class<T>): MyResult<List<T>> {
        return mainRepo.getDataList(path, clazz)
    }

}


data class AlphaModel<T : Any>(
    var path: String,
    var _stateFlow:MutableStateFlow<MyResult<List<T>>>,
    var more:Int = 0,
    ){
    var stateFlow:StateFlow<MyResult<List<T>>> = _stateFlow.asStateFlow()
}

data class SingleAlphaModel<T : Any>(
    var path: String,
    var _stateFlow: MutableStateFlow<MyResult<T>>
) {
    var stateFlow: StateFlow<MyResult<T>> = _stateFlow.asStateFlow()
}
