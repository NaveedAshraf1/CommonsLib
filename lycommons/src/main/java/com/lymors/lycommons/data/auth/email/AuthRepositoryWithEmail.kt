package com.lymors.lycommons.data.auth.email


import com.lymors.lycommons.utils.MyResult


interface AuthRepositoryWithEmail {
    suspend fun registerUser(email:String,password:String): MyResult<String>
    suspend fun loginUser(email:String, password:String): MyResult<String>

}