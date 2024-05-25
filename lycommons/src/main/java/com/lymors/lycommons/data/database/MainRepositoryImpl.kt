package com.lymors.lycommons.data.database


import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.lymors.lycommons.utils.MyExtensions.logT
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
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


class MainRepositoryImpl @Inject constructor(
    private val databaseReference: DatabaseReference
) : MainRepository {

    override fun <T> collectAnyModel(path: String, clazz: Class<T>): Flow<List<T>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messagesList = mutableListOf<T>()
                for (childSnapshot in dataSnapshot.children) {
                    val message = childSnapshot.getValue(clazz)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                trySend(messagesList as List<T>).isSuccess
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



    override suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
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
                databaseReference.child(path).child(updatedKey).setValue(model)
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
        Log.i("TAG", " path:$path")
        return try {
            databaseReference.child(path).removeValue().await()
            MyResult.Success("Success")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }




    override suspend fun <T> getAnyData(path: String, clazz: Class<T>): T? {
        Log.i("TAG", "path: $path")
        return try {
            val snapshot = databaseReference.child(path).get().await()
            snapshot.getValue(clazz)
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data: ${e.message}")
            null
        }
    }

    override suspend fun <T> getModelsWithChildren(path: String, clazz: Class<T>):Flow< List<T> > = callbackFlow {
        path.logT("path:")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val studentsList = mutableListOf<T>()
                for (classSnap in dataSnapshot.children) {
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
        return suspendCancellableCoroutine { continuation ->
            val reference = databaseReference.child(path)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
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




    override suspend fun <T : Any> getMap(child: String, clazz: Class<T>): MyResult<Map<String, T>> {
        Log.i("TAG", "child: $child")
        val newMap = HashMap<String, T>()
        return try {
            val dataSnapshot = databaseReference.child(child).get().await()
            Log.i("TAG", "getMap -> snap.value: ${dataSnapshot.value}")
            for (snap in dataSnapshot.children) {
                snap.getValue(clazz)?.let { value ->
                    newMap[snap.key ?: ""] = value
                }
            }
            MyResult.Success(newMap)
        } catch (e: Exception) {
            MyResult.Error("Failed to retrieve map: ${e.message}")
        }
    }



    // Flow-based function to collect the map from Firebase
    override suspend fun <T : Any> collectMap(path: String): Flow<Map<String, T>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val map: Map<String, T> = dataSnapshot.getValue(object : GenericTypeIndicator<Map<String, T>>() {}) ?: emptyMap()
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


