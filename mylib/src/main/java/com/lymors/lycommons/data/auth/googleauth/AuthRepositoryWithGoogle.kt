package com.lymors.lycommons.data.auth.googleauth

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.lymors.lycommons.utils.MyResult

interface AuthRepositoryWithGoogle {

    fun registerGoogleSignInLauncher(activity: AppCompatActivity)
    fun signInWithGoogle(activity: AppCompatActivity, serverClientId: String, callback: ( account: GoogleSignInAccount?, exception: Exception?) -> Unit)
    fun getGoogleAccount(activity: AppCompatActivity,serverClientId: String, accountCallback: ( account: GoogleSignInAccount?) ->Unit)
    fun signOut(activity: AppCompatActivity,     serverClientId: String , onSignOutResult:(MyResult<String>) ->Unit)

}