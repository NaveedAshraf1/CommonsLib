package com.lymors.lycommons.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lymors.lycommons.utils.MyExtensions.empty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DataStoreHelpers {
    fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore?.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun savePreference(key: Preferences.Key<Int>, value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore?.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun savePreference(key: Preferences.Key<Long>, value: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore?.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun savePreference(key: Preferences.Key<Float>, value: Float) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore?.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun savePreference(key: Preferences.Key<String>, value: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore?.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun savePreference(key: Preferences.Key<Boolean>, value: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore?.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun <T> getPreference(
        key: Preferences.Key<T>
    ) = runBlocking {
        dataStore?.data?.first()?.toPreferences()?.get(key)
    }

    fun getPreference(
        key: Preferences.Key<Int>,
        default: Int = Int.empty()
    ) = runBlocking {
        dataStore?.data?.first()?.toPreferences()?.get(key) ?: default
    }

    fun getPreference(
        key: Preferences.Key<Long>,
        default: Long = Long.empty()
    ) = runBlocking {
        dataStore?.data?.first()?.toPreferences()?.get(key) ?: default
    }

    fun getPreference(
        key: Preferences.Key<Float>,
        default: Float = Float.empty()
    ) = runBlocking {
        dataStore?.data?.first()?.toPreferences()?.get(key) ?: default
    }

    fun getPreference(
        key: Preferences.Key<String>,
        default: String = String.empty()
    ) = runBlocking {
        dataStore?.data?.first()?.toPreferences()?.get(key) ?: default
    }

    fun getPreference(
        key: Preferences.Key<Boolean>,
        default: Boolean = false
    ) = runBlocking {
        dataStore?.data?.first()?.toPreferences()?.get(key) ?: default
    }

    companion object {
        var dataStore: DataStore<Preferences>? = null
        val Context.defaultPreferencesDataStore by preferencesDataStore(name = "default")

        fun initDataStore(context: Context) {
            dataStore = context.defaultPreferencesDataStore
        }

        fun intPrefsKey(name: String) = intPreferencesKey(name)
        fun floatPrefsKey(name: String) = floatPreferencesKey(name)
        fun longPrefsKey(name: String) = longPreferencesKey(name)
        fun doublePrefsKey(name: String) = doublePreferencesKey(name)
        fun stringPrefsKey(name: String) = stringPreferencesKey(name)
        fun booleanPrefsKey(name: String) = booleanPreferencesKey(name)

        fun <T> DataStore<Preferences>.getValueSync(
            key: Preferences.Key<T>
        ) = runBlocking { data.first() }[key]

        fun DataStore<Preferences>.getValueSync(
            key: Preferences.Key<Int>,
            default: Int = Int.empty()
        ) = runBlocking { data.first() }[key] ?: default

        fun DataStore<Preferences>.getValueSync(
            key: Preferences.Key<Long>,
            default: Long = Long.empty()
        ) = runBlocking { data.first() }[key] ?: default

        fun DataStore<Preferences>.getValueSync(
            key: Preferences.Key<Float>,
            default: Float = Float.empty()
        ) = runBlocking { data.first() }[key] ?: default

        fun DataStore<Preferences>.getValueSync(
            key: Preferences.Key<String>,
            default: String = String.empty()
        ) = runBlocking { data.first() }[key] ?: default

        fun DataStore<Preferences>.getValueSync(
            key: Preferences.Key<Boolean>,
            default: Boolean = false
        ) = runBlocking { data.first() }[key] ?: default

    }
}