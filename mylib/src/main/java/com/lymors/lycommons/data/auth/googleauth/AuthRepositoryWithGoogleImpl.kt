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
import com.lymors.lycommons.extensions.MyExtensions.logT
import com.lymors.lycommons.utils.MyResult
import javax.inject.Inject

class AuthRepositoryWithGoogleImpl @Inject constructor(private val auth: FirebaseAuth) : AuthRepositoryWithGoogle {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var onSignInResult: (( account: GoogleSignInAccount?, exception: Exception?) -> Unit)? = null
    private var onGetGoogleAccountCallback: ((account: GoogleSignInAccount?) -> Unit)? = null

    private fun createGoogleSignInOptions(serverClientId: String): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
    }

    override fun registerGoogleSignInLauncher(activity: FragmentActivity) {
        signInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == FragmentActivity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)

                    // Invoke Firebase authentication
                    firebaseAuthWithGoogle(activity, account)

                    // Invoke the callback for getGoogleAccount
                    onGetGoogleAccountCallback?.invoke(account)

                    // Invoke the callback for signInWithGoogle (if set)
                    onSignInResult?.invoke(account, null)
                } catch (e: ApiException) {
                    // Invoke the callback for getGoogleAccount with null account
                    onGetGoogleAccountCallback?.invoke(null)

                    // Invoke the callback for signInWithGoogle (if set) with exception
                    onSignInResult?.invoke(null, e)
                }
            } else {
                // Invoke the callback for getGoogleAccount with null account
                onGetGoogleAccountCallback?.invoke(null)

                // Invoke the callback for signInWithGoogle (if set) with exception
                onSignInResult?.invoke(null, Exception("Sign in canceled resultCode:${result.resultCode} ${result.data}"))
            }
        }
    }


    override fun signInWithGoogle(activity: FragmentActivity, serverClientId: String, callback: (account: GoogleSignInAccount?, exception: Exception?) -> Unit) {
        val gso = createGoogleSignInOptions(serverClientId)
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

        // Store the callback
        onGetGoogleAccountCallback = accountCallback

        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }


    // Sign out method
    override fun signOutFromGoogle(activity: FragmentActivity ,  serverClientId: String , onSignOutResult:(MyResult<String>) ->Unit) {
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
        account?.idToken?.let { token ->
            val credential = GoogleAuthProvider.getCredential(token, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        onSignInResult?.invoke(account, null)
                    } else {
                        onSignInResult?.invoke(account, task.exception)
                    }
                }
        } ?: run {
            onSignInResult?.invoke(null, Exception("Account ID token is null"))
        }
    }


}
