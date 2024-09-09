package com.lymors.lycommons.data.auth.email


import com.lymors.lycommons.utils.MyResult


interface AuthRepositoryWithEmail {
    suspend fun signUpUserWithEmailAndPassword(email:String, password:String): MyResult<String>
    suspend fun loginUserWithEmailAndPassword(email:String, password:String): MyResult<String>

    suspend fun resetPassword(email: String): MyResult<String>
}