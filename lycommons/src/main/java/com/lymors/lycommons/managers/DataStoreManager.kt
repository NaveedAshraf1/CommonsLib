package com.lymors.lycommons.managers

import android.content.Context
import com.lymors.lycommons.utils.MyExtensions.fromJson
import com.lymors.lycommons.utils.MyExtensions.toJsonString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.lymors.lycommons.extensions.StringExtensions.fromJson

class DataStoreManager(var context: Context){

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "UserData")

    suspend fun  saveData(key: String, data: Any) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = data.toJsonString()
        }
    }

    suspend inline fun <reified T>  getData(key: String, clazz:Class<T>): T  {
        val preferences = context.dataStore.data.first()
        val storedValue = preferences[stringPreferencesKey(key)]
        return storedValue?.fromJson() as T
    }

    inline fun <reified T> collectData(key: String, clazz: Class<T>): Flow<T> {
        return context.dataStore.data.map { preferences ->
            val storedValue = preferences[stringPreferencesKey(key)]
            storedValue?.fromJson()!!
        }
    }

    suspend fun  updateData(key: String, data: Any) {
        saveData(key, data)
    }



}