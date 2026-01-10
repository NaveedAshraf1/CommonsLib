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
import android.util.Log

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.lymors.lycommons.extensions.DataExtensions.isNull
import com.lymors.lycommons.extensions.ImageViewExtensions.createImageUploadWorkRequest
import com.lymors.lycommons.extensions.ImageViewExtensions.uploadImageUsingWorkManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KProperty

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

    private val _longClickedState = MutableStateFlow(false)
    val longClickedState = _longClickedState.asStateFlow()

    val map = HashMap<Class<*>, AlphaModel<*>>()

    private val _mapFlow = MutableStateFlow<MyResult<Map<String, Any>>>(MyResult.Success(emptyMap()))
    val mapFlow = _mapFlow.asStateFlow()

    val singleModelMap = HashMap<Class<*>, SingleAlphaModel<*>>()

    var c: Class<*>? = null

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
        return firestoreRepo.uploadAnyModel(path, model)
    }

    suspend fun <T> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): MyResult<List<T>> {
        return firestoreRepo.getAllChildByKeys(path, keys, clazz)
    }

    suspend fun <T : Any> collectAnyModels(
        path: String,
        clazz: Class<T>,
        numberOfItems: Int = 0
    ): StateFlow<MyResult<List<T>>> {
        return suspendCancellableCoroutine { continuation ->
            if (map.containsKey(clazz) && map[clazz]?.path == path && map[clazz]?.more == numberOfItems) {
                continuation.resume(map[clazz]?.stateFlow as StateFlow<MyResult<List<T>>>)
            } else {
                val mutableStateFlow = MutableStateFlow<MyResult<List<T>>>(MyResult.Success(emptyList()))
                val stateFlow = mutableStateFlow.asStateFlow()
                val a = AlphaModel(path, mutableStateFlow, numberOfItems)
                a.stateFlow = stateFlow
                map[clazz] = a
                viewModelScope.launch {
                    firestoreRepo.collectAnyModel(path, clazz, numberOfItems).collect { result: MyResult<List<T>> ->
                        mutableStateFlow.value = result
                    }
                }
                continuation.resume(map[clazz]?.stateFlow as StateFlow<MyResult<List<T>>>)
            }
        }
    }

    suspend fun <T : Any> uploadAllModelsAtOnce(path: String, models: List<T>): MyResult<String> {
        return firestoreRepo.uploadAllModelsAtOnce(path, models)
    }

    suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String> {
        return firestoreRepo.updateAnyModel(path, updatedMap)
    }

    suspend fun <T : Any> queryModelByAProperty(
        path: String,
        property: String,
        value: String,
        clazz: Class<T>
    ): MyResult<T> {
        return firestoreRepo.queryModelByAProperty(path, property, value, clazz)
    }

    suspend fun <T : Any> queryList(
        path: String,
        field: String,
        value: String,
        clazz: Class<T>
    ): MyResult<List<T>> {
        return firestoreRepo.queryList(path, field, value, clazz)
    }

    suspend fun deleteAnyModel(path: String): MyResult<String> {
        return firestoreRepo.deleteAnyModel(path)
    }

    suspend fun getMap(path: String): MyResult<Map<String, String>> {
        return firestoreRepo.getMap(path)
    }

    fun collectMap(path: String): StateFlow<MyResult<Map<String, Any>>> {
        viewModelScope.launch {
            firestoreRepo.collectMap<Any>(path).collect { result: MyResult<Map<String, Any>> ->
                _mapFlow.value = result
            }
        }
        return mapFlow
    }

    suspend fun <T : Any> getAnyData(path: String, clazz: Class<T>): MyResult<T> {
        return firestoreRepo.getAnyData(path, clazz)
    }

    suspend fun <T : Any> getModelsWithChildren(path: String, clazz: Class<T>): Flow<MyResult<List<T>>> {
        return firestoreRepo.getModelsWithChildren(path, clazz)
    }

    fun setLongClickedState(longClicked: Boolean) {
        _longClickedState.value = longClicked
    }

    fun setSearchingState(searching: Boolean) {
        _searchingState.value = searching
    }

    fun <T :Any> collectSingleModel(): StateFlow<MyResult<T>> {
        return singleModelMap[c]?.stateFlow as StateFlow<MyResult<T>>
    }

    fun <T : Any> collectSingleModel(clazz: Class<T>): StateFlow<MyResult<T>> {
        return singleModelMap[clazz]?.stateFlow as StateFlow<MyResult<T>>
    }

    suspend fun <T : Any> collectSingleModel(path: String, clazz: Class<T>): StateFlow<MyResult<T>> {
        if (c.isNull()) {
            c = clazz
        }
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
                    firestoreRepo.collectAModel(path, clazz).collect { result: MyResult<T> ->
                        mutableStateFlow.value = result
                    }
                }
                continuation.resume(singleModelMap[clazz]?.stateFlow as StateFlow<MyResult<T>>)
            }
        }
    }

    suspend fun uploadModelWithImages(
        context: Context,
        firestorePath: String,
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
                return@async MyResult.Error("Firebase Storage is not initialized. Enable it in the Firebase Console.")
            } catch (e: Exception) {
                return@async MyResult.Error("Failed to access Firebase Storage: ${e.message}")
            }

            // Step 2: Upload the model to Firestore
            val modelKeyResult = uploadAnyModel(firestorePath, model)
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
                    val imagePath = "$firestorePath/$key/${property.name}"
                    try {
                        uploadImageUsingWorkManager(context, imageUri, imagePath)
                    } catch (e: StorageException) {
                        when (e.errorCode) {
                            StorageException.ERROR_UNKNOWN -> {
                                return@async MyResult.Error("Firebase Storage may not be enabled or is inaccessible: ${e.message}")
                            }
                            StorageException.ERROR_NOT_AUTHENTICATED,
                            StorageException.ERROR_NOT_AUTHORIZED -> {
                                return@async MyResult.Error("Authentication error with Firebase Storage: ${e.message}. Check Firebase setup.")
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
        firestorePath: String,
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
                return@async MyResult.Error("Firebase Storage is not initialized. Enable it in the Firebase Console.")
            } catch (e: Exception) {
                // Catch other unexpected initialization errors
                return@async MyResult.Error("Failed to access Firebase Storage: ${e.message}")
            }

            // Step 2: Proceed with model upload
            val modelKeyResult = uploadAnyModel(firestorePath, model)
            if (modelKeyResult is MyResult.Error) {
                return@async modelKeyResult // Return the error result immediately
            }

            // Step 3: Handle image upload if applicable
            if (imageUri.isEmpty()) {
                return@async MyResult.Error("imageUri is empty")
            }

            if (modelKeyResult is MyResult.Success) {
                val imagePath = "$firestorePath/${modelKeyResult.data}/${property.name}"
                try {
                    uploadImageUsingWorkManager(context, imageUri, imagePath)
                } catch (e: StorageException) {
                    // Check specific StorageException error codes
                    when (e.errorCode) {
                        StorageException.ERROR_UNKNOWN -> {
                            // Could indicate Storage isn’t enabled or a general failure
                            return@async MyResult.Error("Firebase Storage may not be enabled or is inaccessible: ${e.message}")
                        }
                        StorageException.ERROR_NOT_AUTHENTICATED,
                        StorageException.ERROR_NOT_AUTHORIZED -> {
                            // Indicates auth issues, which could mean Storage isn’t properly set up
                            return@async MyResult.Error("Authentication error with Firebase Storage: ${e.message}. Check Firebase setup.")
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
                val workRequest = createImageUploadWorkRequest(imageUri, "$path/$fieldName")
                workRequests.add(workRequest)
            }
            // Enqueue all the work requests
            workManager.enqueue(workRequests)
        }
    }

    suspend fun <T : Any> getDataList(path: String, clazz: Class<T>): MyResult<List<T>> {
        return firestoreRepo.getDataList(path, clazz)
    }

    fun <T : Any> collectAModel(path: String, clazz: Class<T>) = firestoreRepo.collectAModel(path, clazz)

    // ===================================
    // NEW METHODS DELEGATION
    // ===================================

    suspend fun incrementField(path: String, field: String, amount: Double): MyResult<String> {
        return firestoreRepo.incrementField(path, field, amount)
    }

    suspend fun <T : Any> runTransaction(block: suspend (com.google.firebase.firestore.Transaction) -> T): MyResult<T> {
        return firestoreRepo.runTransaction(block)
    }

    suspend fun deleteAllModels(paths: List<String>): MyResult<String> {
        return firestoreRepo.deleteAllModels(paths)
    }
    
    suspend fun batchUpdate(updates: Map<String, Map<String, Any>>): MyResult<String> {
        return firestoreRepo.batchUpdate(updates)
    }

    suspend fun <T : Any> queryWithOptions(
        path: String,
        clazz: Class<T>,
        whereField: String? = null,
        whereValue: Any? = null,
        orderByField: String? = null,
        descending: Boolean = false,
        limit: Int = 0,
        startAfterValue: Any? = null
    ): MyResult<List<T>> {
        return firestoreRepo.queryWithOptions(
            path, clazz, whereField, whereValue, orderByField, descending, limit, startAfterValue
        )
    }
}

