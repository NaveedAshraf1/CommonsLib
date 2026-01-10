package com.lymors.lycommons.data.database


import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lymors.lycommons.extensions.DataExtensions.shrink
import com.lymors.lycommons.utils.MyExtensions.logT
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


class MainRepositoryImpl @Inject constructor(
    private val databaseReference: DatabaseReference
) : MainRepository {

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun errorResult(op: String, path: String, message: String?): MyResult.Error {
        val reason = message?.takeIf { it.isNotBlank() } ?: "Unknown error"
        return MyResult.Error(
            "RTDB $op failed | path='$path' | reason='$reason'"
        )
    }

    private fun errorResult(op: String, path: String, e: Exception): MyResult.Error {
        val reason = e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
        return MyResult.Error(
            "RTDB $op failed | path='$path' | ex='${e.javaClass.simpleName}' | reason='$reason'"
        )
    }

    private fun errorResult(op: String, path: String, e: DatabaseError): MyResult.Error {
        val message = e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
        val details = e.details?.takeIf { it.isNotBlank() }
        return MyResult.Error(
            buildString {
                append("RTDB $op failed | path='$path' | code=${e.code} | message='$message'")
                if (details != null) append(" | details='$details'")
            }
        )
    }
    override suspend fun <T : Any> uploadAllModelsAtOnce(
        path: String,
        models: List<T>
    ): MyResult<String> {
        // Log the start of the method execution
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return pathResult
        }
        return try {
            val updates = mutableMapOf<String, Any>()

            for (model in models) {
                val modelName = model::class.simpleName ?: "UnknownModel"
                val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }

                val keyToUse: String = if (keyProperty != null) {
                    keyProperty.isAccessible = true
                    val existingKey = keyProperty.call(model)?.toString().orEmpty()
                    if (existingKey.isNotBlank()) {
                        existingKey
                    } else {
                        val generated = databaseReference.push().key
                            ?: return errorResult(
                                "Batch upload",
                                path,
                                "model='$modelName' | reason='Failed to generate new key (push().key returned null)'"
                            )

                        if (keyProperty is KMutableProperty<*>) {
                            keyProperty.setter.call(model, generated)
                            generated
                        } else {
                            return errorResult(
                                "Batch upload",
                                path,
                                "model='$modelName' | reason='Model has key property but it is not mutable; cannot auto-assign generated key'"
                            )
                        }
                    }
                } else {
                    databaseReference.push().key
                        ?: return errorResult(
                            "Batch upload",
                            path,
                            "model='$modelName' | reason='Model has no key property and push().key returned null'"
                        )
                }

                updates[keyToUse] = model.shrink()
            }

            databaseReference.child(path).updateChildren(updates).await()
            MyResult.Success("All models uploaded successfully")
        } catch (e: Exception) {
            Log.e("MainRepository", "Batch upload failed (path: $path)", e)
            errorResult("Batch upload", path, e)
        }
    }


    override fun <T : Any> collectAnyModel(
        path: String,
        clazz: Class<T>,
        numberOfItems: Int,
    ): Flow<MyResult<List<T>>> = callbackFlow {
        path.logT("collectAnyModel->path ", "path")
        numberOfItems.logT("numberOfItems", "path")
        clazz.simpleName.logT("clazz.simpleName" , "firebase")
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error) {
            trySend(MyResult.Error("Invalid path ($path): ${pathResult.message}"))
            close()
            return@callbackFlow
        }
        val query: Query = if (numberOfItems == 0) {
            databaseReference.child(path)
        } else {
            databaseReference.child(path).limitToLast(numberOfItems)
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    gson.toJson(dataSnapshot.value).logT("collectAnyModel->dataSnapshot:", "firebase")
                }
                val messagesList = arrayListOf<T>()
                dataSnapshot.children.filterNotNull().forEach { childSnapshot ->
                    try {
                        val message = childSnapshot.getValue(clazz)
                        message?.let { m ->
                            messagesList.add(m)
                        }
                    } catch (e: DatabaseException) {
                        val jsonData = gson.toJson(childSnapshot.value)
                        val jsonSnippet = jsonData.take(700)
                        val exType = e.javaClass.simpleName
                        val exMessage = e.message?.takeIf { it.isNotBlank() } ?: "Unknown deserialization error"
                        Log.e(
                            "MainRepository",
                            "Deserialize failed | path='$path' | model='${clazz.simpleName}' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'",
                            e
                        )
                        trySend(
                            errorResult(
                                "Deserialize",
                                path,
                                "model='${clazz.simpleName}' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'"
                            )
                        )
                        return
                    }
                }
                trySend(MyResult.Success(messagesList)).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                trySend(errorResult("Listen", path, databaseError))
                close()
            }
        }

        query.addValueEventListener(valueEventListener)
        awaitClose {
            query.removeEventListener(valueEventListener)
        }
    }




    override suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        // Log the start of the method execution
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return pathResult
        }

        // Log the valid path
        path.logT("uploadAnyModel->path", "path")

        return try {

            // Find the 'key' property
            val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
            keyProperty?.let {
                "Found 'key' property in the model".logT("uploadAnyModel->keyProperty", "debug")
            } ?: "No 'key' property found".logT("uploadAnyModel->keyProperty", "warning")

            if (keyProperty != null) {
                // Make the key property accessible and retrieve the key value
                keyProperty.isAccessible = true
                val key = keyProperty.call(model)?.toString() ?: ""

                // If the key is empty, generate a new key (without throwing)
                val newKey = if (key.isNotBlank()) {
                    key
                } else {
                    val generated = databaseReference.push().key
                        ?: return errorResult(
                            "Upload",
                            path,
                            "model='${model::class.simpleName ?: "UnknownModel"}' | reason='Failed to generate new key (push().key returned null)'"
                        )

                    "New key generated: $generated".logT("uploadAnyModel->newKey", "debug")
                    if (keyProperty is KMutableProperty<*>) {
                        keyProperty.setter.call(model, generated)
                        generated
                    } else {
                        return errorResult(
                            "Upload",
                            path,
                            "model='${model::class.simpleName ?: "UnknownModel"}' | reason='Model has key property but it is not mutable; cannot auto-assign generated key'"
                        )
                    }
                }

                var shrinked = model.shrink()
                shrinked.logT("uploadAnyModel->shrinked", "firebase")

                databaseReference.child(path).child(newKey).setValue(shrinked).await()

                "Model uploaded successfully with key: $newKey".logT(
                    "uploadAnyModel->success",
                    "firebase"
                )
                MyResult.Success(newKey)
            } else {
                // Log upload without a key
                "Uploading model without a key to path: $path".logT(
                    "uploadAnyModel->uploadNoKey",
                    "degub"
                )
                databaseReference.child(path).setValue(model).await()

                // Log successful upload
                val uploadedKey = path.split("/").last()
                "Model uploaded successfully with last segment as key: $uploadedKey".logT(
                    "uploadAnyModel->successNoKey",
                    "debug"
                )
                MyResult.Success(uploadedKey)
            }
        } catch (e: Exception) {
            // Log the error
            "Error uploading model: ${e.message}".logT("uploadAnyModel->error", "error")
            Log.e("MainRepository", "Upload failed (path: $path)", e)
            errorResult("Upload", path, e)
        } finally {
            // Log the end of the method
            "Finished uploadAnyModel execution".logT("uploadAnyModel->end", "debug")
        }
    }


    override suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String> {
        // Log the start of the method execution
        "Started updateAnyModel execution".logT("updateAnyModel->start", "debug")
        // Log the start of the method execution
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return pathResult
        }

        // Log the valid path
        path.logT("updateAnyModel->path", "path")

        return try {
            // Update Firebase data
            databaseReference.child(path).updateChildren(updatedMap).await()

            // Log successful update
            "Data updated successfully at path: $path".logT("updateAnyModel->success", "firebase")
            MyResult.Success("Data updated successfully")
        } catch (e: Exception) {
            // Log the error
            "Error updating data: ${e.message}".logT("updateAnyModel->error", "error")
            Log.e("MainRepository", "Update failed (path: $path)", e)
            errorResult("Update", path, e)
        } finally {
            // Log the end of the method
            "Finished updateAnyModel execution".logT("updateAnyModel->end", "debug")
        }
    }


    override suspend fun deleteAnyModel(path: String): MyResult<String> {
        // Log the start of the method execution
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return pathResult
        }
        path.logT("deleteAnyModel->path", "path")
        return try {
            databaseReference.child(path).removeValue().await()
            MyResult.Success("deleted Successfully")
        } catch (e: Exception) {
            Log.e("MainRepository", "Delete failed (path: $path)", e)
            errorResult("Delete", path, e)
        }
    }


    override suspend fun <T : Any> getAllChildByKeys(
        path: String,
        keys: List<String>,
        clazz: Class<T>
    ): MyResult<List<T>> = withContext(Dispatchers.IO) {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error) {
            return@withContext MyResult.Error("Invalid path ($path): ${pathResult.message}")
        }
        path.logT("getAllChildByKeys->path", "path")

        if (keys.isEmpty()) return@withContext MyResult.Success(emptyList())

        return@withContext try {
            val results = mutableListOf<T>()
            val firstError = mutableListOf<Exception>()

            keys.forEach { key ->
                try {
                    val snapshot = databaseReference.child(path).child(key).get().await()
                    snapshot.logT("getAllChildByKeys->snapshot-$key", "firebase")
                    val value = snapshot.getValue(clazz)
                    if (value != null) results.add(value)
                } catch (e: Exception) {
                    if (firstError.isEmpty()) firstError.add(e)
                }
            }

            if (results.isEmpty() && firstError.isNotEmpty()) {
                errorResult("Get", path, firstError.first())
            } else {
                MyResult.Success(results)
            }
        } catch (e: Exception) {
            errorResult("Get", path, e)
        }
    }

    override fun <T : Any> collectAModel(path: String, clazz: Class<T>): Flow<MyResult<T>> = callbackFlow {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            trySend(MyResult.Error("Invalid path ($path): ${pathResult.message}"))
            close()
            return@callbackFlow
        }
        path.logT("collectAModel->path", "path")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gson.toJson(dataSnapshot.value).logT("collectAModel->dataSnapshot", "firebase")
                val message = try {
                    dataSnapshot.getValue(clazz)
                } catch (e: DatabaseException) {
                    val jsonData = gson.toJson(dataSnapshot.value)
                    val jsonSnippet = jsonData.take(700)
                    val exType = e.javaClass.simpleName
                    val exMessage = e.message?.takeIf { it.isNotBlank() } ?: "Unknown deserialization error"
                    Log.e(
                        "MainRepository",
                        "Deserialize failed | path='$path' | model='${clazz.simpleName}' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'",
                        e
                    )
                    trySend(
                        errorResult(
                            "Deserialize",
                            path,
                            "model='${clazz.simpleName}' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'"
                        )
                    )
                    return
                }
                if (message != null) {
                    trySend(MyResult.Success(message)).isSuccess
                } else {
                    trySend(MyResult.Error("Not found"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                trySend(errorResult("Listen", path, databaseError))
                close()
            }
        }
        databaseReference.child(path).addValueEventListener(valueEventListener)
        awaitClose {
            databaseReference.child(path).removeEventListener(valueEventListener)
        }

    }


    override suspend fun <T : Any> getAnyData(path: String, clazz: Class<T>): MyResult<T> {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return MyResult.Error("Invalid path ($path): ${pathResult.message}")
        }
        path.logT("getAnyData->path", "path")
        return try {
            val snapshot = databaseReference.child(path).get().await()
            gson.toJson(snapshot.value).logT("getAnyData->snapshot", "firebase")
            val model = snapshot.getValue(clazz)
            if (model != null) {
                MyResult.Success(model)
            } else {
                MyResult.Error("Not found")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data: ${e.message}")
            errorResult("Get", path, e)
        }
    }

    override suspend fun <T : Any> getDataList(path: String, clazz: Class<T>): MyResult<List<T>> {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return MyResult.Error("Invalid path ($path): ${pathResult.message}")
        }
        path.logT("getDataList->path", "path")
        return try {
            val snapshot = databaseReference.child(path).get().await()
            gson.toJson(snapshot.value).logT("getDataList->snapshot", "firebase")
            MyResult.Success(snapshot.children.mapNotNull { it.getValue(clazz) })
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data list: ${e.message}")
            errorResult("Get", path, e)
        }
    }


    override fun <T : Any> getModelsWithChildren(path: String, clazz: Class<T>): Flow<MyResult<List<T>>> = callbackFlow {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            trySend(MyResult.Error("Invalid path ($path): ${pathResult.message}"))
            close()
            return@callbackFlow
        }

        path.logT("getModelsWithChildren->path", "path")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentsList = mutableListOf<T>()
                for (classSnap in dataSnapshot.children.filterNotNull()) {
                    gson.toJson(classSnap.value)
                        .logT("getModelsWithChildren->classSnap", "firebase")
                    for (studentSnap in classSnap.children) {
                        try {
                            val studentModel = studentSnap.getValue(clazz)
                            studentModel?.let { studentsList.add(it) }
                        } catch (e: DatabaseException) {
                            val childPath = buildString {
                                append(path)
                                classSnap.key?.let { append("/").append(it) }
                                studentSnap.key?.let { append("/").append(it) }
                            }
                            val jsonSnippet = gson.toJson(studentSnap.value).take(700)
                            val exType = e.javaClass.simpleName
                            val exMessage = e.message?.takeIf { it.isNotBlank() } ?: "Unknown deserialization error"
                            Log.e(
                                "MainRepository",
                                "Deserialize failed | path='$childPath' | model='${clazz.simpleName}' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'",
                                e
                            )
                            trySend(
                                errorResult(
                                    "Deserialize",
                                    childPath,
                                    "model='${clazz.simpleName}' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'"
                                )
                            )
                            return
                        }
                    }
                }
                trySend(MyResult.Success(studentsList)).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                trySend(errorResult("Listen", path, databaseError))
                close()
            }
        }
        databaseReference.child(path).addValueEventListener(valueEventListener)
        awaitClose {
            databaseReference.child(path).removeEventListener(valueEventListener)
        }
    }


    override suspend fun checkExists(path: String): MyResult<String> {
        // Log the start of the method execution
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return pathResult
        }
        path.logT("checkExists->path", "path")
        return suspendCancellableCoroutine { continuation ->
            val reference = databaseReference.child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.logT("checkExists->snapshot", "firebase")
                        continuation.resume(MyResult.Success("$path exists"))
                    } else {
                        continuation.resume(MyResult.Error("$path does not exist"))
                    }
                    reference.removeEventListener(this) // Remove listener after successful completion
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(errorResult("Check", path, error))
                    reference.removeEventListener(this) // Remove listener on error
                }
            }
            reference.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { reference.removeEventListener(listener) }
        }
    }


    override suspend fun <T : Any> queryModelByAProperty(
        path: String,
        property: String,
        value: String,
        clazz: Class<T>
    ): MyResult<T> {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return MyResult.Error("Invalid path ($path): ${pathResult.message}")
        }
        path.logT("queryModelByAProperty->path", "path")
        return try {
            val querySnapshot = databaseReference.child(path).orderByChild(property).equalTo(value).get().await()
            querySnapshot.logT("queryModelByAProperty->query", "firebase")
            val model = querySnapshot.children.firstOrNull()?.getValue(clazz)
            if (model != null) {
                MyResult.Success(model)
            } else {
                MyResult.Error("Not found")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data: ${e.message}")
            errorResult("Query", path, e)
        }
    }

    override suspend fun <T : Any> queryList(
        path: String,
        field: String,
        value: String,
        clazz: Class<T>
    ): MyResult<List<T>> {
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            return MyResult.Error("Invalid path ($path): ${pathResult.message}")
        }
        if (field.isBlank()) {
            return MyResult.Error("Field cannot be empty")
        }
        return try {
            val querySnapshot = databaseReference.child(path).orderByChild(field).equalTo(value).get().await()
            val list = querySnapshot.children.mapNotNull { it.getValue(clazz) }
            MyResult.Success(list)
        } catch (e: Exception) {
            errorResult("Query", path, e)
        }
    }


    override suspend fun getMap(path: String): MyResult<Map<String, String>> {
        path.logT("getMap->path", "path")
        val newMap = HashMap<String, String>()
        return try {
            val dataSnapshot = databaseReference.child(path).get().await()
            dataSnapshot.logT("getMap->dataSnapshot", "firebase")
            for (snap in dataSnapshot.children) {
                snap.getValue(String::class.java)?.let { value ->
                    newMap[snap.key ?: Random.nextInt().toString()] = value
                }
            }
            MyResult.Success(newMap)
        } catch (e: Exception) {
            Log.e("MainRepository", "Get map failed (path: $path)", e)
            errorResult("Get map", path, e)
        }
    }


    // Flow-based function to collect the map from Firebase
    override fun <T : Any> collectMap(path: String): Flow<MyResult<Map<String, T>>> = callbackFlow {
        path.logT("collectMap->path", "path")
        val pathResult = path.isValidPath()
        if (pathResult is MyResult.Error){
            trySend(MyResult.Error("Invalid path ($path): ${pathResult.message}"))
            close()
            return@callbackFlow
        }
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val map: Map<String, T> =
                        dataSnapshot.getValue(object : GenericTypeIndicator<Map<String, T>>() {})
                            ?: emptyMap()
                    map.logT("collectMap->snap.value", "firebase")
                    trySend(MyResult.Success(map))
                } catch (e: DatabaseException) {
                    val jsonSnippet = gson.toJson(dataSnapshot.value).take(700)
                    val exType = e.javaClass.simpleName
                    val exMessage = e.message?.takeIf { it.isNotBlank() } ?: "Unknown deserialization error"
                    Log.e(
                        "MainRepository",
                        "Deserialize failed | path='$path' | model='Map<String, T>' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'",
                        e
                    )
                    trySend(
                        errorResult(
                            "Deserialize",
                            path,
                            "model='Map<String, T>' | ex='$exType' | message='$exMessage' | json='$jsonSnippet'"
                        )
                    )
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                trySend(errorResult("Listen", path, databaseError))
                close()
            }
        }
        val databaseReference = databaseReference.child(path)
        databaseReference.addValueEventListener(valueEventListener)

        awaitClose {
            // Clean up by removing the listener when the flow is cancelled or completed
            databaseReference.removeEventListener(valueEventListener)
        }
    }.flowOn(Dispatchers.IO)


    fun String.isValidPath(): MyResult<Unit> {
        // Check if the path is empty
        if (this.isEmpty()) {
            return MyResult.Error("Path cannot be empty")
        }

        // Split the path into components, ignoring leading slashes
        val components = this.trimStart('/').split('/')

        // Check for trailing slash (except for root "/")
        if (this.endsWith('/') && this != "/") {
            return MyResult.Error("Path cannot end with a slash")
        }

        // Check each component for forbidden characters and emptiness
        val forbiddenCharacters = listOf('.', '#', '$', '[', ']')
        for (component in components) {
            if (component.isEmpty() && this != "/") {
                return MyResult.Error("Path contains empty components")
            }
            if (forbiddenCharacters.any { it in component }) {
                return MyResult.Error("Component '$component' contains forbidden characters: . # $ [ ]")
            }
        }

        // If all checks pass, return success
        return MyResult.Success(Unit)
    }




}































//    override fun <T> collectAnyModel(path: String, clazz: Class<T>): Flow<List<T>> = callbackFlow {
//        path.logT("collectAnyModel->path","path")
//        val valueEventListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                dataSnapshot.logT("collectAnyModel->dataSnapshot:","firebase")
//                val messagesList = mutableListOf<T>()
//                for (childSnapshot in dataSnapshot.children) {
//                    val message = childSnapshot.getValue(clazz)
//                    message?.let {
//                        messagesList.add(it)
//                    }
//                }
//                trySend(messagesList as List<T>).isSuccess
//            }
//            override fun onCancelled(databaseError: DatabaseError) {
//                close(databaseError.toException())
//            }
//        }
//        databaseReference.child(path).addValueEventListener(valueEventListener)
//        awaitClose {
//            databaseReference.child(path).removeEventListener(valueEventListener)
//        }
//    }
