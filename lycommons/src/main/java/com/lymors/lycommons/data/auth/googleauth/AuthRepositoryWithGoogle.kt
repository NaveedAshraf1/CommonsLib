package com.lymors.lycommons.data.auth.googleauth

import com.lymors.lycommons.utils.MyResult

interface AuthRepositoryWithGoogle {
    suspend fun loginWithGoogle(): MyResult<String>
    suspend fun signUpWithGoogle(): MyResult<String>
}