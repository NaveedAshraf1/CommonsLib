package com.lymors.lycommons.data.auth.googleauth

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.lymors.lycommons.utils.MyExtensions.logT
import com.lymors.lycommons.utils.MyResult
import javax.inject.Inject

class AuthRepositoryWithGoogleImpl @Inject constructor(private val auth: FirebaseAuth) : AuthRepositoryWithGoogle {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var onSignInResult: (( account: GoogleSignInAccount?, exception: Exception?) -> Unit)? = null

    override fun registerGoogleSignInLauncher(activity: FragmentActivity) {
        "registerGoogleSignInLauncher".logT()
        signInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == FragmentActivity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(activity, account)
                    onSignInResult?.invoke( account, null)
                } catch (e: ApiException) {
                    onSignInResult?.invoke( null, e)
                }
            } else {
                onSignInResult?.invoke( null, Exception("Sign in canceled"))
            }
        }
    }

    override fun signInWithGoogle(activity: FragmentActivity, serverClientId: String, callback: ( account: GoogleSignInAccount?, exception: Exception?) -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        onSignInResult = callback
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    override fun getGoogleAccount(
        activity: FragmentActivity,
        serverClientId: String,
        accountCallback: (account: GoogleSignInAccount?) -> Unit
    ) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }


    // Sign out method
    override fun signOut(activity: FragmentActivity ,  serverClientId: String , onSignOutResult:(MyResult<String>) ->Unit) {
        auth.signOut() // Firebase sign out
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        googleSignInClient.signOut().addOnCompleteListener(activity) {
            if (it.isSuccessful) {
                onSignOutResult.invoke(MyResult.Success("Signed out successfully"))
            } else {
                onSignOutResult.invoke(MyResult.Error("Failed to sign out due to ${it.exception?.message}"))
            }
        }
    }

    private fun firebaseAuthWithGoogle(activity: FragmentActivity, account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSignInResult?.invoke(account , null)
                } else {
                    onSignInResult?.invoke( account, task.exception)
                }
            }
    }


}
