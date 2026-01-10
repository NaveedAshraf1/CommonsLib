package com.lymors.lycommons.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class MyResult<out T : Any> {


    data class Success<out T : Any>(val data: T) : MyResult<T>()
    data class Error(val exception: Throwable) : MyResult<Nothing>() {
    constructor(message: String) : this(Exception(message))

    val message: String get() = exception.message ?: "Unknown error"
}
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=${exception.message}]"
        }
    }

    fun whenResult(onSuccess: (T) -> Unit, onError: (Exception) -> Unit) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception as? Exception ?: Exception(exception))
        }
    }

    fun whenIt(isSuccessFull: (Boolean) -> Unit) {
        when (this) {
            is Success -> isSuccessFull(true)
            is Error -> isSuccessFull(false)
        }
    }

    fun whenSuccess(onSuccess: (T) -> Unit ): MyResult<T> {
        if (this is Success) {
            onSuccess(data)
        }
        return this
    }


    fun whenError(onError: (Exception) -> Unit): MyResult<T> {
        if (this is Error) {
            val ex = exception as? Exception ?: Exception(exception)
            Log.e("MyResult", "Error: ${ex.message}", ex)
            onError(ex)
        }
        return this
    }



}