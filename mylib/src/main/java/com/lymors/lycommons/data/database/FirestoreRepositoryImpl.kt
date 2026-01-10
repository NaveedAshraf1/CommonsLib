package com.lymors.lycommons.data.database
 
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.lymors.lycommons.extensions.DataExtensions.gson
import com.lymors.lycommons.extensions.DataExtensions.shrink
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

 class FirestoreRepositoryImpl @Inject constructor(
     private val firestore: FirebaseFirestore
 ) : FirestoreRepository {


     /**
      * Wraps Firestore operations with standard logging and error handling.
      */
     private suspend fun <T : Any> safeFirestoreCall(
         op: String,
         path: String,
         block: suspend () -> T
     ): MyResult<T> {
         return try {
             MyResult.Success(block())
         } catch (e: Exception) {
             Log.e("FirestoreRepository", "$op failed (path: $path)", e)
             errorResult(op, path, e)
         }
     }

     private fun String.isValidPath(): MyResult<String> {
         if (this.isBlank()) {
             return MyResult.Error(Exception("Path cannot be empty"))
         }

         val normalized = this.trim()
         if (normalized.endsWith('/') && normalized != "/") {
             return MyResult.Error(Exception("Path cannot end with a slash"))
         }

         val components = normalized.trimStart('/').split('/')
         if (components.any { it.isBlank() }) {
             return MyResult.Error(Exception("Path contains empty components"))
         }

         return MyResult.Success("path is ok")
     }

     private fun errorResult(op: String, path: String, message: String?): MyResult.Error {
         val reason = message?.takeIf { it.isNotBlank() } ?: "Unknown error"
         val fullMsg = "FS $op failed | path='$path' | reason='$reason'"
         return MyResult.Error(Exception(fullMsg))
     }

     private fun errorResult(op: String, path: String, e: Exception, extra: String? = null): MyResult.Error {
         val exType = e.javaClass.simpleName
         val exMessage = e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"

         val details = if (e is FirebaseFirestoreException) {
             val code = e.code
             val hint = when (code) {
                 FirebaseFirestoreException.Code.CANCELLED -> "Operation cancelled"
                 FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Check Firestore rules"
                 FirebaseFirestoreException.Code.UNAUTHENTICATED -> "User not authenticated"
                 FirebaseFirestoreException.Code.NOT_FOUND -> "Doc/Collection not found"
                 FirebaseFirestoreException.Code.UNAVAILABLE -> "Network/Service unavailable"
                 else -> null
             }
             
             buildString {
                 append("code=$code")
                 if (hint != null) append(" | hint='$hint'")
             }
         } else ""

         val fullMessage = buildString {
             append("FS $op failed | path='$path'")
             if (!extra.isNullOrBlank()) append(" | ").append(extra)
             append(" | ex='$exType': '$exMessage'")
             if (details.isNotBlank()) append(" | ").append(details)
         }

         return MyResult.Error(Exception(fullMessage, e))
     }

     private fun validateDocumentPath(op: String, path: String): MyResult<Unit> {
         val pathValidation = path.isValidPath()
         if (pathValidation is MyResult.Error) {
             return MyResult.Error(pathValidation.exception) 
         }

         val segments = path.trim('/').split('/')
         if (segments.size % 2 != 0) {
             return MyResult.Error(Exception("FS $op invalid document path | path='$path' | reason='Must have even segments'"))
         }
         
         return MyResult.Success(Unit)
     }

     private fun validateCollectionPath(op: String, path: String): MyResult<Unit> {
         val pathValidation = path.isValidPath()
         if (pathValidation is MyResult.Error) {
             return MyResult.Error(pathValidation.exception)
         }

         val segments = path.trim('/').split('/')
         if (segments.size % 2 == 0) {
             return MyResult.Error(Exception("FS $op invalid collection path | path='$path' | reason='Must have odd segments'"))
         }

         return MyResult.Success(Unit)
     }

     private fun createNestedReference(path: String): DocumentReference {
         val pathParts = path.split("/")
         var currentRef: CollectionReference = firestore.collection(pathParts[0])
         for (i in 1 until pathParts.size - 1 step 2) {
             currentRef = currentRef.document(pathParts[i]).collection(pathParts[i + 1])
         }
         return currentRef.document(pathParts.last())
     }

     private fun createNestedCollectionReference(path: String): CollectionReference {
         val pathParts = path.split("/")
         var currentRef: CollectionReference = firestore.collection(pathParts[0])
         for (i in 1 until pathParts.size - 1 step 2) {
             currentRef = currentRef.document(pathParts[i]).collection(pathParts[i + 1])
         }
         return currentRef
     }

     override suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String> {
         val validation = validateDocumentPath("Update", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Update", path) {
             createNestedReference(path).update(updatedMap).await()
             "Updated successfully"
         }
     }

     override suspend fun checkExists(path: String): MyResult<String> {
         val validation = validateDocumentPath("Check", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Check", path) {
             val snapshot = createNestedReference(path).get().await()
             if (snapshot.exists()) "Document exists" else throw Exception("Document does not exist")
         }
     }

     override suspend fun <T> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): MyResult<List<T>> {
         val validation = validateCollectionPath("Query", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         if (keys.isEmpty()) return MyResult.Success(emptyList())
         
         return safeFirestoreCall("Query", path) {
             val collectionRef = createNestedCollectionReference(path)
             val all = mutableListOf<T>()
             for (chunk in keys.chunked(10)) {
                 val documents = collectionRef.whereIn("key", chunk).get().await()
                 all.addAll(documents.toObjects(clazz))
             }
             all
         }
     }

     override suspend fun <T : Any> queryModelByAProperty(path: String, property: String, value: String, clazz: Class<T>): MyResult<T> {
         val validation = validateCollectionPath("Query", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Query", path) {
             val collectionRef = createNestedCollectionReference(path)
             val snapshot = collectionRef.whereEqualTo(property, value).limit(1).get().await()
             snapshot.documents.firstOrNull()?.toObject(clazz) 
                 ?: throw Exception("Not found")
         }
     }

     override suspend fun <T : Any> queryList(path: String, field: String, value: String, clazz: Class<T>): MyResult<List<T>> {
         if (field.isBlank()) return MyResult.Error(Exception("Field cannot be empty"))
         val validation = validateCollectionPath("Query", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)
         
         return safeFirestoreCall("Query", path) {
             val collectionRef = createNestedCollectionReference(path)
             val snapshot = collectionRef.whereEqualTo(field, value).get().await()
             snapshot.toObjects(clazz)
         }
     }

     override suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        val pathValidation = path.isValidPath()
        if (pathValidation is MyResult.Error) return MyResult.Error(pathValidation.exception)

        return safeFirestoreCall("Upload", path) {
            val pathParts = path.split("/")
            val collectionRef = createNestedCollectionReference(path)

            val documentId = if (pathParts.size % 2 == 0) pathParts.last() else collectionRef.document().id

            // Auto-inject key if present
            val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
            keyProperty?.isAccessible = true // Set accessible FIRST
            if (keyProperty is KMutableProperty<*>) {
                keyProperty.setter.call(model, documentId)
            } else if (keyProperty != null) {
                throw Exception("Model '${model::class.simpleName}' has immutable 'key' property. Cannot assign ID.")
            }

            val modelMap = model.shrink().toMutableMap()
            collectionRef.document(documentId).set(modelMap).await()
       
            documentId
        }
     }

    // Removed getFirestoreCollectionReference - use createNestedCollectionReference directly

     override suspend fun deleteAnyModel(path: String): MyResult<String> {
         val validation = validateDocumentPath("Delete", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Delete", path) {
             createNestedReference(path).delete().await()
             "Deleted successfully"
         }
     }

     override fun <T : Any> collectAModel(path: String, clazz: Class<T>): Flow<MyResult<T>> = callbackFlow {
         val validate = validateDocumentPath("Listen", path)
         if (validate is MyResult.Error) {
             trySend(MyResult.Error(validate.exception))
             close()
             return@callbackFlow
         }
         
         val docRef = createNestedReference(path)
         val listener = docRef.addSnapshotListener { snapshot, e ->
             if (e != null) {
                 trySend(errorResult("Listen", path, e))
                 close() 
                 return@addSnapshotListener
             }
             snapshot?.let {
                 try {
                     val model = it.toObject(clazz)
                     if (model != null) trySend(MyResult.Success(model))
                     else trySend(MyResult.Error(Exception("No data")))
                 } catch (ex: Exception) {
                     val json = gson.toJson(it.data).take(200)
                     trySend(errorResult("Deserialize", path, ex, "json snippet='$json'"))
                 }
             }
         }
         awaitClose { listener.remove() }
     }.flowOn(Dispatchers.IO)

     override suspend fun getMap(path: String): MyResult<Map<String, String>> {
         val validation = validateDocumentPath("Get", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Get", path) {
             val snapshot = createNestedReference(path).get().await()
             @Suppress("UNCHECKED_CAST")
             snapshot.data as? Map<String, String> 
                 ?: throw Exception("Data is not Map<String, String>")
         }
     }

     override fun <T : Any> collectMap(path: String): Flow<MyResult<Map<String, T>>> = callbackFlow {
         val validate = validateCollectionPath("Listen", path)
         if (validate is MyResult.Error) {
             trySend(MyResult.Error(validate.exception))
             close()
             return@callbackFlow
         }
         
         val ref = createNestedCollectionReference(path)
         val listener = ref.addSnapshotListener { snapshot, e ->
             if (e != null) {
                 trySend(errorResult("Listen", path, e))
                 close()
                 return@addSnapshotListener
             }
             
             val resultMap = mutableMapOf<String, T>()
             snapshot?.documents?.forEach { doc ->
                 try {
                     val data = doc.toObject(Any::class.java)
                     @Suppress("UNCHECKED_CAST")
                     val casted = data as? T
                     if (casted != null) {
                         resultMap[doc.id] = casted
                     }
                 } catch (ex: Exception) {
                     Log.e("FirestoreRepository", "collectMap cast failed", ex)
                 }
             }
             trySend(MyResult.Success(resultMap))
         }
         awaitClose { listener.remove() }
     }.flowOn(Dispatchers.IO)

     override suspend fun <T : Any> getAnyData(path: String, clazz: Class<T>): MyResult<T> {
         val validation = validateDocumentPath("Get", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Get", path) {
             createNestedReference(path).get().await().toObject(clazz)
                 ?: throw Exception("Not found")
         }
     }

     override fun <T : Any> getModelsWithChildren(path: String, clazz: Class<T>): Flow<MyResult<List<T>>> = callbackFlow {
         val validate = validateCollectionPath("Listen", path)
         if (validate is MyResult.Error) {
             trySend(MyResult.Error(validate.exception))
             close()
             return@callbackFlow
         }

         val ref = createNestedCollectionReference(path)
         val listener = ref.addSnapshotListener { snapshot, e ->
             if (e != null) {
                 trySend(errorResult("Listen", path, e))
                 close()
                 return@addSnapshotListener
             }
             try {
                 val models = snapshot?.documents?.mapNotNull { it.toObject(clazz) } ?: emptyList()
                 trySend(MyResult.Success(models))
             } catch (ex: Exception) {
                 trySend(errorResult("Deserialize", path, ex))
             }
         }
         awaitClose { listener.remove() }
     }.flowOn(Dispatchers.IO)

     override suspend fun <T : Any> getDataList(path: String, clazz: Class<T>): MyResult<List<T>> {
         val validation = validateCollectionPath("Get", path)
         if (validation is MyResult.Error) return MyResult.Error(validation.exception)

         return safeFirestoreCall("Get", path) {
             createNestedCollectionReference(path).get().await().toObjects(clazz)
         }
     }

     override fun <T : Any> collectAnyModel(path: String, clazz: Class<T>, numberOfItems: Int): Flow<MyResult<List<T>>> = callbackFlow {
         val validate = validateCollectionPath("Listen", path)
         if (validate is MyResult.Error) {
             trySend(MyResult.Error(validate.exception))
             close()
             return@callbackFlow
         }
         
         val ref = createNestedCollectionReference(path)
         val query = if (numberOfItems > 0) ref.limit(numberOfItems.toLong()) else ref
         
         val listener = query.addSnapshotListener { snapshot, e ->
             if (e != null) {
                 trySend(errorResult("Listen", path, e))
                 close()
                 return@addSnapshotListener
             }
             val models = snapshot?.documents?.mapNotNull { doc ->
                 try {
                     doc.toObject(clazz)
                 } catch (ex: Exception) {
                     Log.e("FirestoreRepository", "Deserialize failed doc=${doc.id}", ex)
                     null // Skip bad docs
                 }
             } ?: emptyList()
             trySend(MyResult.Success(models))
         }
         awaitClose { listener.remove() }
     }.flowOn(Dispatchers.IO)

     override suspend fun <T : Any> uploadAllModelsAtOnce(path: String, models: List<T>): MyResult<String> {
         val pathValidation = path.isValidPath()
         if (pathValidation is MyResult.Error) return MyResult.Error(pathValidation.exception)

         return safeFirestoreCall("BatchUpload", path) {
             val pathParts = path.split("/")
             val batch = firestore.batch()
             
             models.forEach { model ->
                 val modelMap = model.shrink().toMutableMap()
                 if (pathParts.size % 2 == 0) {
                     batch.set(createNestedReference(path), modelMap)
                 } else {
                     val newDocRef = createNestedCollectionReference(path).document()
                     batch.set(newDocRef, modelMap)
                     
                     val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
                     if (keyProperty is KMutableProperty<*>) {
                         keyProperty.isAccessible = true
                         keyProperty.setter.call(model, newDocRef.id)
                     } else if (keyProperty != null) {
                         throw Exception("Model key property immutable")
                     }
                 }
             }
             batch.commit().await()
             "All models uploaded successfully"
         }
     }

    // ============================================================
    // NEW: HIGH PRIORITY METHODS
    // ============================================================

    /**
     * Atomically increment or decrement a numeric field.
     * Uses FieldValue.increment() for atomic operation.
     */
    override suspend fun incrementField(path: String, field: String, amount: Double): MyResult<String> {
        val validation = validateDocumentPath("Increment", path)
        if (validation is MyResult.Error) return MyResult.Error(validation.exception)
        
        if (field.isBlank()) return MyResult.Error(Exception("Field name cannot be empty"))

        return safeFirestoreCall("Increment", path) {
            val docRef = createNestedReference(path)
            docRef.update(field, com.google.firebase.firestore.FieldValue.increment(amount)).await()
            "Incremented $field by $amount"
        }
    }

    /**
     * Execute multiple reads and writes atomically in a transaction.
     * Critical for race-condition-sensitive operations like balance updates.
     */
    override suspend fun <T : Any> runTransaction(block: suspend (com.google.firebase.firestore.Transaction) -> T): MyResult<T> {
        return try {
            val result = firestore.runTransaction { transaction ->
                // Note: runTransaction expects a synchronous block, so we use runBlocking inside.
                // This is a limitation of Firestore's API. For true async, consider Tasks API.
                kotlinx.coroutines.runBlocking {
                    block(transaction)
                }
            }.await()
            MyResult.Success(result)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Transaction failed", e)
            MyResult.Error(Exception("Transaction failed: ${e.message}", e))
        }
    }

    // ============================================================
    // NEW: MEDIUM PRIORITY METHODS
    // ============================================================

    /**
     * Delete multiple documents in a single batch operation.
     * Validates each path before deleting.
     */
    override suspend fun deleteAllModels(paths: List<String>): MyResult<String> {
        if (paths.isEmpty()) return MyResult.Success("No documents to delete")

        // Validate all paths first
        for (p in paths) {
            val validation = validateDocumentPath("BatchDelete", p)
            if (validation is MyResult.Error) return MyResult.Error(validation.exception)
        }

        return safeFirestoreCall("BatchDelete", paths.first()) {
            val batch = firestore.batch()
            paths.forEach { p ->
                batch.delete(createNestedReference(p))
            }
            batch.commit().await()
            "Deleted ${paths.size} documents"
        }
    }

    /**
     * Query with ordering and pagination support.
     * Supports optional where, orderBy, limit, and startAfter.
     */
    override suspend fun <T : Any> queryWithOptions(
        path: String,
        clazz: Class<T>,
        whereField: String?,
        whereValue: Any?,
        orderByField: String?,
        descending: Boolean,
        limit: Int,
        startAfterValue: Any?
    ): MyResult<List<T>> {
        val validation = validateCollectionPath("Query", path)
        if (validation is MyResult.Error) return MyResult.Error(validation.exception)

        return safeFirestoreCall("Query", path) {
            var query: com.google.firebase.firestore.Query = createNestedCollectionReference(path)

            // Apply where filter
            if (!whereField.isNullOrBlank() && whereValue != null) {
                query = query.whereEqualTo(whereField, whereValue)
            }

            // Apply ordering
            if (!orderByField.isNullOrBlank()) {
                query = if (descending) {
                    query.orderBy(orderByField, com.google.firebase.firestore.Query.Direction.DESCENDING)
                } else {
                    query.orderBy(orderByField, com.google.firebase.firestore.Query.Direction.ASCENDING)
                }
            }

            // Apply startAfter for pagination
            if (startAfterValue != null) {
                query = query.startAfter(startAfterValue)
            }

            // Apply limit
            if (limit > 0) {
                query = query.limit(limit.toLong())
            }

            val snapshot = query.get().await()
            snapshot.toObjects(clazz)
        }
    }

    /**
     * Update multiple documents in a single batch operation.
     * Each entry in the map is a document path -> fields to update.
     */
    override suspend fun batchUpdate(updates: Map<String, Map<String, Any>>): MyResult<String> {
        if (updates.isEmpty()) return MyResult.Success("No updates to apply")

        // Validate all paths first
        for (path in updates.keys) {
            val validation = validateDocumentPath("BatchUpdate", path)
            if (validation is MyResult.Error) return MyResult.Error(validation.exception)
        }

        return safeFirestoreCall("BatchUpdate", updates.keys.first()) {
            val batch = firestore.batch()
            updates.forEach { (path, fields) ->
                val docRef = createNestedReference(path)
                batch.update(docRef, fields)
            }
            batch.commit().await()
            "Updated ${updates.size} documents"
        }
    }
 }

