package com.lymors.lycommons.data.auth.googleauth

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.lymors.lycommons.utils.MyResult

interface AuthRepositoryWithGoogle {

    fun registerGoogleSignInLauncher(activity: FragmentActivity)
    fun signInWithGoogle(activity: FragmentActivity, serverClientId: String, callback: ( account: GoogleSignInAccount?, exception: Exception?) -> Unit)
    fun getGoogleAccount(activity: FragmentActivity,serverClientId: String, accountCallback: ( account: GoogleSignInAccount?) ->Unit)
    fun signOut(activity: FragmentActivity,     serverClientId: String , onSignOutResult:(MyResult<String>) ->Unit)

}