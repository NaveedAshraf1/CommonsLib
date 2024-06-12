package com.lymors.lycommons.data.database


import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun checkExists(path: String): MyResult<String>
    suspend fun<T : Any> queryModelByAProperty(path: String , property: String, value: String , clazz: Class<T>): T?
    suspend fun< T :Any> uploadAnyModel(path:String, model: T): MyResult<String>
    suspend fun deleteAnyModel(path:String): MyResult<String>
    fun <T>  collectAModel(path:String, clazz: Class<T>):Flow<T>
    suspend fun getMap(path: String): MyResult<Map<String, String>>
    suspend fun < T : Any> collectMap(path:String): Flow<Map<String , T>>
    suspend fun <T> getAnyData(path:String, clazz: Class<T>): T?
    suspend fun<T> getModelsWithChildren(path:String , clazz:Class<T>): Flow<List<T>>

    fun <T> collectAnyModel(path: String, clazz: Class<T>, numberOfItems: Int = 0): Flow<List<T>>

}