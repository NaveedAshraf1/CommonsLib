package com.lymors.lycommons.managers

import android.content.Context
import com.lymors.lycommons.utils.MyExtensions.toJsonString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

class DataStoreManager(var context: Context){

//    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "UserData")
//
//    suspend fun  saveData(key: String, data: Any) {
//        val preferencesKey = stringPreferencesKey(key)
//        context.dataStore.edit { preferences ->
//               preferences[preferencesKey] = data.toJsonString()
//        }
//    }
//
//    suspend fun <T>  getData(key: String, clazz:Class<T>): T  {
//        val preferences = context.dataStore.data.first()
//        val storedValue = preferences[stringPreferencesKey(key)]
//         return storedValue?.fromJson(clazz) as T
//    }
//
//    fun <T> collectData(key: String, clazz: Class<T>): Flow<T> {
//        return context.dataStore.data.map { preferences ->
//            val storedValue = preferences[stringPreferencesKey(key)]
//            storedValue?.fromJson(clazz) as T
//        }
//    }
//
//    suspend fun  updateData(key: String, data: Any) {
//        saveData(key, data)
//    }



}