package com.lymors.lycommons.data.auth.googleauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.lymors.lycommons.R
import com.lymors.lycommons.utils.MyConstants
import com.lymors.lycommons.utils.MyConstants.REQUEST_CODE_GOOGLE_SIGN_IN
import com.lymors.lycommons.utils.MyResult
import javax.inject.Inject

class AuthRepositoryWithGoogleImpl @Inject constructor(private val auth:FirebaseAuth):
    AuthRepositoryWithGoogle {


    private lateinit var googleSignInClient: GoogleSignInClient
    override fun requestGoogleSignIn(activity: AppCompatActivity,serverClientId:String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(activity , signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN , null)
    }

    override fun signInWithGoogle(activity: AppCompatActivity , account: GoogleSignInAccount? , callback:(task:Task<AuthResult>) ->Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                callback(task)
            }
    }
}

