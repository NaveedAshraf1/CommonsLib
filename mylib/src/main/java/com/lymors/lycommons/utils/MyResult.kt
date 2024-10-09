package com.lymors.lycommons.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class MyResult<out T : Any> {



    data class Success<out T : Any>(val data: T) : MyResult<T>()
    data class Error(val msg: String) : MyResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$msg]"
        }
    }

    fun whenResult(onSuccess: (T) -> Unit, onError: (Exception) -> Unit) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(Exception(msg))  // Create an Exception object
        }
    }

    fun whenIt(isSuccessFull: (Boolean) -> Unit) {
        when (this) {
            is Success -> isSuccessFull(true)
            is Error -> isSuccessFull(false)
        }
    }

    fun whenSuccess(onSuccess: (T) -> Unit ) {
        if (this is Success) {
            onSuccess(data)
        }
    }


    fun whenError(onError: (Exception) -> Unit) {
        if (this is Error) {
            onError(Exception(msg))  // Create an Exception object
        }
    }

    fun showInToast(context: Context, duration: Int = Toast.LENGTH_SHORT) {
       CoroutineScope(Dispatchers.Main).launch {
           when (this@MyResult) {
               is Success<*> ->{Toast.makeText(context, data.toString(), duration).show()}
               is Error -> {Toast.makeText(context, msg, duration).show()}
           }
       }
    }


}