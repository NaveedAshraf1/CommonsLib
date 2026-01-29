package com.lymors.lycommons.data.auth.googleauth

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.lymors.lycommons.utils.MyResult

interface AuthRepositoryWithGoogle {
    fun requestGoogleSignIn(activity: AppCompatActivity, serverClientId:String)
    fun signInWithGoogle(activity: AppCompatActivity, account: GoogleSignInAccount?, callback:(task: Task<AuthResult>) ->Unit)
}