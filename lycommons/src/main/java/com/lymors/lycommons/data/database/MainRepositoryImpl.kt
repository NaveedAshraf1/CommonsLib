package com.lymors.lycommons.data.database


import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.lymors.lycommons.R
import com.lymors.lycommons.utils.MyExtensions.logT
import com.lymors.lycommons.utils.MyExtensions.shrink
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


class MainRepositoryImpl @Inject constructor(
    private val databaseReference: DatabaseReference
) : MainRepository {





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


   override fun <T> collectAnyModel(
        path: String,
        clazz: Class<T>,
        numberOfItems: Int ,
    ): Flow<List<T>> = callbackFlow {
        path.logT("collectAnyModel->path ", "path")
        numberOfItems.logT("numberOfItems", "path")
        clazz.simpleName.logT("clazz.simpleName")

        val query:Query =  if (numberOfItems == 0){
            databaseReference.child(path)
        }else{
           databaseReference.child(path).limitToLast(numberOfItems)
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.logT("collectAnyModel->dataSnapshot:", "firebase")
                val messagesList = mutableListOf<T>()
               dataSnapshot.children.forEach {
                   val message = it.getValue(clazz)
                   message?.let { m ->
                       messagesList.add(m)
                   }
               }
                trySend(messagesList).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        query.addValueEventListener(valueEventListener)
        awaitClose {
            query.removeEventListener(valueEventListener)
        }
    }




    override suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        path.logT("uploadAnyModel->path","path")
        return try {
            val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
            if (keyProperty != null) {
                keyProperty.isAccessible = true
                val key = keyProperty.call(model)?.toString() ?: ""
                val updatedKey = key.ifEmpty {
                    databaseReference.push().key.toString().also { newKey ->
                        if (keyProperty is KMutableProperty<*>) {
                            (keyProperty as KMutableProperty<*>).setter.call(model, newKey)
                        } else {
                            throw IllegalStateException("The 'key' property is not mutable")
                        }
                    }
                }
                databaseReference.child(path).child(updatedKey).setValue(model.shrink())
                MyResult.Success(if (key.isEmpty()) updatedKey else "Updated")
            } else {
                databaseReference.child(path).setValue(model)
                MyResult.Success("Success")
            }


        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }




    override suspend fun deleteAnyModel(path: String): MyResult<String> {
        path.logT("deleteAnyModel->path","path")
        return try {
            databaseReference.child(path).removeValue().await()
            MyResult.Success("deleted Successfully")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }

    override fun <T> collectAModel(path: String, clazz: Class<T>): Flow<T> = callbackFlow {
        path.logT("collectAModel->path","path")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.logT("collectAModel->dataSnapshot","firebase")
                val message = dataSnapshot.getValue(clazz)
                if (message != null) {
                    trySend(message).isSuccess
                }
                }
            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
                }
        }
        databaseReference.child(path).addValueEventListener(valueEventListener)
        awaitClose {
            databaseReference.child(path).removeEventListener(valueEventListener)
        }

    }


    override suspend fun <T> getAnyData(path: String, clazz: Class<T>): T? {
       path.logT("getAnyData->path","path")
        return try {
            val snapshot = databaseReference.child(path).get().await()
            snapshot.logT("getAnyData->snapshot","firebase")
            snapshot.getValue(clazz)
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data: ${e.message}")
            null
        }
    }

    override suspend fun <T> getModelsWithChildren(path: String, clazz: Class<T>):Flow< List<T> > = callbackFlow {
       path.logT("getModelsWithChildren->path","path")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentsList = mutableListOf<T>()
                for (classSnap in dataSnapshot.children) {
                    classSnap.logT("getModelsWithChildren->classSnap", "firebase")
                    for (studentSnap in classSnap.children){
                        val studentModel = studentSnap.getValue(clazz)
                        studentModel?.let {
                            studentsList.add(it)
                        }
                    }
                }
                trySend(studentsList).isSuccess
            }
            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }
        databaseReference.child(path).addValueEventListener(valueEventListener)
        awaitClose {
            databaseReference.child(path).removeEventListener(valueEventListener)
        }
    }


    override suspend fun checkExists(path: String): MyResult<String> {
      path.logT("checkExists->path","path")
        return suspendCancellableCoroutine { continuation ->
            val reference = databaseReference.child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        snapshot.logT("checkExists->snapshot","firebase")
                        continuation.resume(MyResult.Success("$path exists"))
                    }else{
                        continuation.resume(MyResult.Error("$path does not exist"))
                    }
                    reference.removeEventListener(this) // Remove listener after successful completion
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(MyResult.Error("Failed to check path existence."))
                    reference.removeEventListener(this) // Remove listener on error
                }
            }
            reference.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { reference.removeEventListener(listener) }
        }
    }

    override suspend fun <T : Any> queryModelByAProperty(path: String, property: String, value: String, clazz: Class<T>): T? {
        path.logT("queryModelByAProperty->path", "path")
        return try {
            val querySnapshot = databaseReference.child(path).orderByChild(property).equalTo(value).get().await()

            querySnapshot.logT("queryModelByAProperty->query", "firebase")

            if (querySnapshot.exists()) {
                querySnapshot.getValue(clazz)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data: ${e.message}")
            null
        }
    }



    override suspend fun getMap(path: String): MyResult<Map<String, String>> {
     path.logT("getMap->path","path")
        val newMap = HashMap<String, String>()
        return try {
            val dataSnapshot = databaseReference.child(path).get().await()
          dataSnapshot.logT("getMap->dataSnapshot","firebase")
            for (snap in dataSnapshot.children) {
                snap.getValue(String::class.java)?.let { value ->
                    newMap[snap.key ?: Random.nextInt().toString()] = value
                }
            }
            MyResult.Success(newMap)
        } catch (e: Exception) {
            MyResult.Error("Failed to retrieve map: ${e.message}")
        }
    }



    // Flow-based function to collect the map from Firebase
    override suspend fun <T : Any> collectMap(path: String): Flow<Map<String, T>> = callbackFlow {
        path.logT( "collectMap->path" , "path")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val map: Map<String, T> = dataSnapshot.getValue(object : GenericTypeIndicator<Map<String, T>>() {}) ?: emptyMap()
                map.logT("collectMap->snap.value","firebase")
                trySend(map)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                trySend(emptyMap())
            }
        }
        val databaseReference = databaseReference.child(path)
        databaseReference.addValueEventListener(valueEventListener)

        awaitClose {
            // Clean up by removing the listener when the flow is cancelled or completed
            databaseReference.removeEventListener(valueEventListener)
        }
    }.flowOn(Dispatchers.IO)






}


