package com.lymors.lycommons.data.viewmodels

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.lymors.lycommons.data.auth.email.AuthRepositoryWithEmail
import com.lymors.lycommons.data.auth.googleauth.AuthRepositoryWithGoogle
import com.lymors.lycommons.data.auth.phone.AuthRepositoryWithPhone
import com.lymors.lycommons.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepositoryWithEmail: AuthRepositoryWithEmail, private val authRepositoryWithPhone: AuthRepositoryWithPhone ,private val authRepositoryWithGoogle: AuthRepositoryWithGoogle) : ViewModel() {

    private var callBack :PhoneAuthProvider.OnVerificationStateChangedCallbacks?=null
    private var verificationId :String?=null
    private var token :PhoneAuthProvider.ForceResendingToken?=null
    private var phoneNumber :String?=null


    fun registerGoogleSignInLauncher(activity: AppCompatActivity){
        authRepositoryWithGoogle.registerGoogleSignInLauncher(activity)
    }


    suspend fun signInWithPhone( context: Activity, phone:String, otpVerifyActivity: Class<*> , mainActivity:Class<*> , onResult:(MyResult<String>) ->Unit){
        Log.i("TAG", "viewmodel signInWithPhone")
        phoneNumber = phone
        withContext(Dispatchers.IO){ callBack = authRepositoryWithPhone.signInUser(phone, context,
            {  phoneAuthCredential ->
                viewModelScope.launch {
                    Log.i("TAG", "auto phoneAuthCredential")
                    var r = loginWithPhoneCredential(phoneAuthCredential)
                    r.whenSuccess {
                        val intent = Intent(context,mainActivity)
                        context.startActivity(intent)
                        onResult.invoke( MyResult.Success("Automatic verification completed"))
                    }
                    r.whenError {
                        onResult.invoke( MyResult.Success(it.message.toString()))
                    }
                }

            }, {
                onResult.invoke( MyResult.Success(it))
            }, { verificationID , tokeN  ->
                Log.i("TAG", "verificationID $verificationID")
                Log.i("TAG", "tokeN $tokeN")
                verificationId = verificationID
                token = tokeN
                var intent = Intent(context , otpVerifyActivity)
                intent.putExtra("data","data")
                context.startActivity(intent)
                onResult.invoke(MyResult.Success("Otp Sent"))
            })}
    }

    private suspend fun loginWithPhoneCredential(credential: PhoneAuthCredential): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithPhone.loginUser(credential)}
    }

    suspend fun verifyOtp(otp:String): MyResult<String> {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        return withContext(Dispatchers.IO){authRepositoryWithPhone.loginUser(credential)}
    }

    suspend fun resendOtp( context: Activity): MyResult<String> {
        if (callBack==null){
            return MyResult.Error("CallBack is null")
        }
        if (verificationId==null){
            return MyResult.Error("VerificationId is null")
        }
        if (token==null) {
            return MyResult.Error("Token is null")
        }
        return if (phoneNumber==null){
            MyResult.Error("PhoneNumber is null")
        }else{
            withContext(Dispatchers.IO){
                authRepositoryWithPhone.resendOtp(token!!, callBack!!, context, phoneNumber!!)}
        }
    }



    // Firebase Auth (with email)
    suspend fun signUpWithEmailAndPassword(email:String,password:String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithEmail.signUpUserWithEmailAndPassword(email, password)}
    }
    suspend fun loginWithEmailAndPassword(email:String, password:String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithEmail.loginUserWithEmailAndPassword(email, password)}
    }

    // google
    fun signInWithGoogle(activity: AppCompatActivity, serverClientId: String, callback: ( account: GoogleSignInAccount?, exception: Exception?) -> Unit) {
        authRepositoryWithGoogle.signInWithGoogle(activity,serverClientId, callback)
    }

    // Google Auth
    fun getGoogleAccount(
        activity: AppCompatActivity,
        serverClientId: String,
        accountCallback: (account: GoogleSignInAccount?) -> Unit
    ) {
        authRepositoryWithGoogle.getGoogleAccount(activity, serverClientId, accountCallback)
    }

    fun signOut(
        activity: AppCompatActivity,
        serverClientId: String,
        onSignOutResult: (MyResult<String>) -> Unit
    ) {
        authRepositoryWithGoogle.signOut(activity, serverClientId, onSignOutResult)
    }




}