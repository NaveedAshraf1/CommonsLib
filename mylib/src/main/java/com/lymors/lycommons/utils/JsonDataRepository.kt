package com.lymors.lycommons.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class JsonDataRepository(context: Context, fileName: String = "data.json") {
    private val gson = Gson()
    private val file: File = File(context.filesDir, fileName)

    init {
        if (!file.exists()) {
            file.writeText("{}")
        }
    }

    private fun readJson(): MutableMap<String, Any> {
        val json = file.readText()
        val type = object : TypeToken<MutableMap<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun writeJson(data: MutableMap<String, Any>) {
        val jsonString = gson.toJson(data)
        file.writeText(jsonString)
    }

    fun getAllData(): MutableMap<String, Any> {
        return readJson()
    }

    fun <T : Any> getData(path: String, clazz: KClass<T>): T? {
        val data = readJson()
        val result: Any? = getDataAtPath<Any>(data, path.split("/"))
        return result?.let {
            gson.fromJson(gson.toJson(it), clazz.java)
        }
    }

    fun <T : Any> getDataList(path: String, clazz: KClass<T>): List<T> {
        val data = readJson()
        val jsonMap: Any? = getDataAtPath<Any>(data, path.split("/"))
        if (jsonMap is Map<*, *>) {
            return jsonMap.values.mapNotNull { item ->
                gson.fromJson(gson.toJson(item), clazz.java)
            }
        }
        return emptyList()
    }

    fun insert(path: String, value: Any): String {
        val data = readJson()
        val keyProperty = value::class.memberProperties.find { it.name == "key" }

        if (keyProperty != null) {
            var key = keyProperty.call(value)?.toString() ?: ""

            if (key.isEmpty()) {
                if (keyProperty is KMutableProperty1<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val mutableKeyProperty = keyProperty as KMutableProperty1<Any, String>
                    mutableKeyProperty.isAccessible = true
                    val newKey = UUID.randomUUID().toString()
                    mutableKeyProperty.set(value, newKey)
                    key = newKey
                } else {
                    throw IllegalStateException("Cannot set 'key' property")
                }
            }

            val finalPath = "$path/$key"
            setDataAtPath(data, finalPath.split("/"), gson.toJsonTree(value).asJsonObject)
            writeJson(data)
            return finalPath
        } else {
            setDataAtPath(data, path.split("/"), value)
            writeJson(data)
            return path
        }
    }

    fun delete(path: String) {
        val data = readJson()
        val pathList = path.split("/")
        var current: MutableMap<String, Any> = data
        for (i in pathList.indices) {
            val key = pathList[i]
            if (i == pathList.size - 1) {
                current.remove(key)
            } else {
                val next = current[key]
                if (next is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    current = next as MutableMap<String, Any>
                } else {
                    return // Path does not exist or is not a nested map
                }
            }
        }
        writeJson(data)
    }

    private fun <T> getDataAtPath(data: MutableMap<String, Any>, path: List<String>): T? {
        var current: Any? = data
        for (key in path) {
            current = (current as? Map<*, *>)?.get(key)
        }
        @Suppress("UNCHECKED_CAST")
        return current as? T
    }

    private fun setDataAtPath(data: MutableMap<String, Any>, path: List<String>, value: Any) {
        var current: MutableMap<String, Any> = data
        Log.i("TAG", "inserted: $value at ${path.joinToString("/")}")
        for (i in path.indices) {
            val key = path[i]
            if (i == path.lastIndex) {
                current[key] = value
            } else {
                current = current.getOrPut(key) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
            }
        }
    }
}
