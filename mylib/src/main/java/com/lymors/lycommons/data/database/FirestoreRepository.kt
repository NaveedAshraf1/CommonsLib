package com.lymors.lycommons.data.database

import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    suspend fun updateAnyModel(path: String, updatedMap: Map<String, Any>): MyResult<String>
    suspend fun checkExists(path: String): MyResult<String>
    suspend fun <T> getAllChildByKeys(path: String, keys: List<String>, clazz: Class<T>): MyResult<List<T>>
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

    // === NEW: HIGH PRIORITY ===
    
    /**
     * Atomically increment or decrement a numeric field.
     * @param path Document path
     * @param field Field name to increment
     * @param amount Amount to add (can be negative for decrement)
     */
    suspend fun incrementField(path: String, field: String, amount: Double): MyResult<String>
    
    /**
     * Execute multiple reads and writes atomically in a transaction.
     * Critical for balance updates, transfers, etc.
     * @param block Transaction logic that receives the Firestore transaction object
     */
    suspend fun <T : Any> runTransaction(block: suspend (com.google.firebase.firestore.Transaction) -> T): MyResult<T>
    
    // === NEW: MEDIUM PRIORITY ===
    
    /**
     * Delete multiple documents in a single batch operation.
     * @param paths List of document paths to delete
     */
    suspend fun deleteAllModels(paths: List<String>): MyResult<String>
    
    /**
     * Query with ordering and pagination support.
     * @param path Collection path
     * @param clazz Model class
     * @param whereField Optional field to filter on
     * @param whereValue Optional value to match (uses equality)
     * @param orderByField Optional field to order by
     * @param descending If true, order descending
     * @param limit Maximum number of results (0 = no limit)
     * @param startAfterValue Optional value to start after for pagination
     */
    suspend fun <T : Any> queryWithOptions(
        path: String,
        clazz: Class<T>,
        whereField: String? = null,
        whereValue: Any? = null,
        orderByField: String? = null,
        descending: Boolean = false,
        limit: Int = 0,
        startAfterValue: Any? = null
    ): MyResult<List<T>>
    
    /**
     * Update multiple documents in a single batch operation.
     * Each entry in the map is a document path -> fields to update.
     * @param updates Map of document paths to their update maps
     */
    suspend fun batchUpdate(updates: Map<String, Map<String, Any>>): MyResult<String>
}

