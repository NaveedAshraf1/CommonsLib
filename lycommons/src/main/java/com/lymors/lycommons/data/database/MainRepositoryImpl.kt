package com.lymors.lycommons.data.database


import com.lymors.lycommons.models.ParentModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.lymors.lycommons.utils.MyExtensions.shrink
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class MainRepositoryImpl @Inject constructor(
    private val databaseReference: DatabaseReference
) : MainRepository {
    var ref = databaseReference.child("students")

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

    override suspend fun uploadAnyModel(child: String, model: ParentModel): MyResult<String> {
        return try {
            var key=""
            if (model.key.isEmpty()){
                key = databaseReference.push().key.toString()
                model.key = key
            }
            databaseReference.child(child).child(model.key).setValue(model.shrink()).await()
            MyResult.Success("Success")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }


    override suspend fun deleteAnyModel(child: String, key: String): MyResult<String> {
        return try {
            databaseReference.child(child).child(key).removeValue().await()
            MyResult.Success("Success")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }


    override suspend fun <T> getAnyModelByKey(path: String, key: String, clazz: Class<T>):T?{
        return try {
            val snapshot = databaseReference.child(path).child(key).get().await()
            val obj = snapshot.getValue(clazz)
            obj as T
        } catch (e: Exception) {
            null
        }
    }





    override suspend fun updateAnyModel(child: String, model: ParentModel): MyResult<String> {
        return try {
            databaseReference.child(child).child(model.key).setValue(model.shrink()).await()
            MyResult.Success("Success")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }

    override suspend fun updateAnyValue(child: String, map: HashMap<String,Any>): MyResult<String> {
        return try {
            databaseReference.child(child).updateChildren(map).await()
            MyResult.Success("Updated Successfully")
        }catch (e:Exception){
            MyResult.Error(e.message.toString())
        }
    }

}


