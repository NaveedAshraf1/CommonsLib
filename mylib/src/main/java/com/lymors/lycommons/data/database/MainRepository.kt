package com.lymors.lycommons.data.database

import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String>
    suspend fun checkExists(path: String): MyResult<String>
    suspend fun <T : Any> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): MyResult<List<T>>
    suspend fun <T : Any> queryModelByAProperty(path: String, property: String, value: String, clazz: Class<T>): MyResult<T>
    suspend fun <T : Any> queryList(path: String, field: String, value: String, clazz: Class<T>): MyResult<List<T>>
    suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String>
    suspend fun deleteAnyModel(path: String): MyResult<String>
    fun <T : Any> collectAModel(path: String, clazz: Class<T>): Flow<MyResult<T>>
    suspend fun getMap(path: String): MyResult<Map<String, String>>
    fun <T : Any> collectMap(path: String): Flow<MyResult<Map<String, T>>>
    suspend fun <T : Any> getAnyData(path: String, clazz: Class<T>): MyResult<T>
    fun <T : Any> getModelsWithChildren(path: String, clazz: Class<T>): Flow<MyResult<List<T>>>
    suspend fun <T : Any> getDataList(path: String, clazz: Class<T>): MyResult<List<T>>
    fun <T : Any> collectAnyModel(path: String, clazz: Class<T>, numberOfItems: Int = 0): Flow<MyResult<List<T>>>
    suspend fun <T : Any> uploadAllModelsAtOnce(path: String, models: List<T>): MyResult<String>
}