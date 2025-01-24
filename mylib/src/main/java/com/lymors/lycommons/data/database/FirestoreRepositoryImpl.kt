package com.lymors.lycommons.data.database

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.lymors.lycommons.extensions.MyExtensions.logT
import com.lymors.lycommons.extensions.MyExtensions.shrink
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

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun String.isValidPath(): Boolean = this.isNotBlank()

    // Helper method to create a reference for nested collections
    private fun createNestedReference(path: String): DocumentReference {
        val pathParts = path.split("/")
        
        // Ensure we have an even number of parts for a valid document path
        require(pathParts.size % 2 == 0) { "Invalid path format for nested collection" }
        
        // Start with the root collection
        var currentRef: CollectionReference = firestore.collection(pathParts[0])
        
        // Traverse through nested collections and documents
        for (i in 1 until pathParts.size - 1 step 2) {
            currentRef = currentRef.document(pathParts[i]).collection(pathParts[i + 1])
        }
        
        // Return the final document reference
        return currentRef.document(pathParts.last())
    }

    // Helper method to create a collection reference for nested collections
    private fun createNestedCollectionReference(path: String): CollectionReference {
        val pathParts = path.split("/")
        
        // Start with the root collection
        var currentRef: CollectionReference = firestore.collection(pathParts[0])
        
        // Traverse through nested collections and documents
        for (i in 1 until pathParts.size - 1 step 2) {
            currentRef = currentRef.document(pathParts[i]).collection(pathParts[i + 1])
        }
        
        return currentRef
    }

    override suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String> {
        return try {
            val docRef = createNestedReference(path)
            docRef.update(updatedMap).await()
            MyResult.Success("Updated successfully")
        } catch (e: Exception) {
            MyResult.Error("Update failed: ${e.message}")
        }
    }

    override suspend fun checkExists(path: String): MyResult<String> {
        return try {
            val docRef = createNestedReference(path)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                MyResult.Success("Document exists")
            } else {
                MyResult.Error("Document does not exist")
            }
        } catch (e: Exception) {
            MyResult.Error("Check existence failed: ${e.message}")
        }
    }

    override suspend fun <T> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): List<T> {
        return try {
            val collectionRef = createNestedCollectionReference(path)
            val documents = collectionRef.whereIn("key", keys).get().await()
            documents.toObjects(clazz)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error getting child by keys: ${e.message}")
            emptyList()
        }
    }

    override suspend fun <T : Any> queryModelByAProperty(
        path: String,
        property: String,
        value: String,
        clazz: Class<T>
    ): T? {
        return try {
            val collectionRef = createNestedCollectionReference(path)
            val querySnapshot = collectionRef.whereEqualTo(property, value).limit(1).get().await()
            querySnapshot.documents.firstOrNull()?.toObject(clazz)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error querying model: ${e.message}")
            null
        }
    }

    override suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        if (!path.isValidPath()) {
            return MyResult.Error("Invalid path")
        }

        return try {
            val pathParts = path.split("/")
            
            // Add server timestamp
            val modelMap = (model as? Map<String, Any>)?.toMutableMap() 
                ?: gson.fromJson(gson.toJson(model), Map::class.java).toMutableMap()

            val documentId = if (pathParts.size % 2 == 0) {
                // Even number of parts: Specific document path
                val docRef = createNestedReference(path)
                docRef.set(modelMap).await()
                pathParts.last()
            } else {
                // Odd number of parts: Collection path, generate new document
                val collectionRef = createNestedCollectionReference(path)
                val newDocRef = collectionRef.document()
                newDocRef.set(modelMap).await()
                
                // Update the key property if exists
                val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
                if (keyProperty is KMutableProperty<*>) {
                    keyProperty.isAccessible = true
                    keyProperty.setter.call(model, newDocRef.id)
                }
                
                newDocRef.id
            }

            MyResult.Success(documentId)
        } catch (e: Exception) {
            MyResult.Error("Upload failed in nested collection: ${e.message}")
        }
    }

    override suspend fun deleteAnyModel(path: String): MyResult<String> {
        return try {
            val docRef = createNestedReference(path)
            docRef.delete().await()
            MyResult.Success("Deleted successfully")
        } catch (e: Exception) {
            MyResult.Error("Delete failed: ${e.message}")
        }
    }

    override fun <T> collectAModel(path: String, clazz: Class<T>): Flow<T> = callbackFlow {
        val pathParts = path.split("/")
        val docRef = createNestedReference(path)

        val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            snapshot?.let {
                val model = it.toObject(clazz)
                model?.let { m -> trySend(m) }
            }
        }

        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun getMap(path: String): MyResult<Map<String, String>> {
        return try {
            val docRef = createNestedReference(path)
            val snapshot = docRef.get().await()
            val map = snapshot.data as? Map<String, String>
            if (map != null) {
                MyResult.Success(map)
            } else {
                MyResult.Error("No map found")
            }
        } catch (e: Exception) {
            MyResult.Error("Get map failed: ${e.message}")
        }
    }

    override suspend fun <T : Any> collectMap(path: String): Flow<Map<String, T>> = callbackFlow {
        val collectionRef = createNestedCollectionReference(path)

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            val resultMap = mutableMapOf<String, T>()
            snapshot?.documents?.forEach { doc ->
                val data = doc.toObject(Any::class.java)
                @Suppress("UNCHECKED_CAST")
                data?.let { resultMap[doc.id] = it as T }
            }

            trySend(resultMap)
        }

        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun <T> getAnyData(path: String, clazz: Class<T>): T? {
        return try {
            val docRef = createNestedReference(path)
            val snapshot = docRef.get().await()
            snapshot.toObject(clazz)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error getting data: ${e.message}")
            null
        }
    }

    override suspend fun <T> getModelsWithChildren(path: String, clazz: Class<T>): Flow<List<T>> = callbackFlow {
        val collectionRef = createNestedCollectionReference(path)

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            val models = snapshot?.documents?.mapNotNull { it.toObject(clazz) } ?: emptyList()
            trySend(models)
        }

        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun <T> getDataList(path: String, clazz: Class<T>): List<T> {
        return try {
            val collectionRef = createNestedCollectionReference(path)
            val snapshot = collectionRef.get().await()
            snapshot.toObjects(clazz)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error getting data list: ${e.message}")
            emptyList()
        }
    }

    override fun <T> collectAnyModel(
        path: String,
        clazz: Class<T>,
        numberOfItems: Int
    ): Flow<List<T>> = callbackFlow {
        val collectionRef = createNestedCollectionReference(path)

        var query: Query = collectionRef
        if (numberOfItems > 0) {
            query = query.limit(numberOfItems.toLong())
        }

        val listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }

            val models = snapshot?.documents?.mapNotNull { it.toObject(clazz) } ?: emptyList()
            trySend(models)
        }

        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun <T : Any> uploadAllModelsAtOnce(path: String, models: List<T>): MyResult<String> {
        if (!path.isValidPath()) {
            return MyResult.Error("Invalid path")
        }

        return try {
            val pathParts = path.split("/")
            
            val batch = firestore.batch()

            models.forEach { model ->
                // Add server timestamp
                val modelMap = (model as? Map<String, Any>)?.toMutableMap() 
                    ?: gson.fromJson(gson.toJson(model), Map::class.java).toMutableMap()

                val documentId = if (pathParts.size % 2 == 0) {
                    // Even number of parts: Specific document path
                    val docRef = createNestedReference(path)
                    batch.set(docRef, modelMap)
                    pathParts.last()
                } else {
                    // Odd number of parts: Collection path, generate new document
                    val collectionRef = createNestedCollectionReference(path)
                    val newDocRef = collectionRef.document()
                    batch.set(newDocRef, modelMap)
                    
                    // Update the key property if exists
                    val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
                    if (keyProperty is KMutableProperty<*>) {
                        keyProperty.isAccessible = true
                        keyProperty.setter.call(model, newDocRef.id)
                    }
                    
                    newDocRef.id
                }
            }

            batch.commit().await()
            MyResult.Success("All models uploaded successfully")
        } catch (e: Exception) {
            MyResult.Error("Batch upload failed in nested collection: ${e.message}")
        }
    }
}
