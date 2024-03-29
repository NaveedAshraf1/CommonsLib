package com.lymors.lycommons.data.auth.googleauth

import com.google.firebase.auth.FirebaseAuth
import com.lymors.lycommons.utils.MyResult
import javax.inject.Inject

class AuthRepositoryWithGoogleImpl @Inject constructor(private val auth:FirebaseAuth):
    AuthRepositoryWithGoogle {
    override suspend fun loginWithGoogle(): MyResult<String> {
        TODO("Not yet implemented")
    }

    override suspend fun signUpWithGoogle(): MyResult<String> {
        TODO("Not yet implemented")
    }
}