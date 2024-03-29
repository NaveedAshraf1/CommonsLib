package com.lymors.lycommons.data.database


import com.lymors.lycommons.models.ParentModel
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    suspend fun uploadAnyModel(child:String , model: ParentModel): MyResult<String>
    suspend fun updateAnyModel(child:String , model: ParentModel): MyResult<String>

    suspend fun updateAnyValue(child:String , map: HashMap<String,Any>): MyResult<String>
    suspend fun deleteAnyModel(child:String , key: String): MyResult<String>
    suspend fun <T> getAnyModelByKey(path:String , key:String , clazz: Class<T>): T?
    fun <T>  collectAnyModel(path:String, clazz: Class<T>):Flow<List<T>>

}