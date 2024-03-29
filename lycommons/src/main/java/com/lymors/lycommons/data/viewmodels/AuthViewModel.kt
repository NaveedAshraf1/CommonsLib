package com.lymors.lycommons.data.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.lymors.lycommons.data.auth.email.AuthRepositoryWithEmail
import com.lymors.lycommons.data.auth.phone.AuthRepositoryWithPhone
import com.lymors.lycommons.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepositoryWithEmail: AuthRepositoryWithEmail, private val authRepositoryWithPhone: AuthRepositoryWithPhone) : ViewModel() {

    // Firebase Auth (with email)
    suspend fun registerUser(email:String,password:String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithEmail.registerUser(email, password)}
    }
    suspend fun loginUser(email:String, password:String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithEmail.loginUser(email, password)}
    }


    // Firebase Auth (with phone number)
    suspend fun signInUser(phone:String, context: Activity, phoneAuthCredential:(PhoneAuthCredential)->Unit, authVerificationFailed:(String)->Unit, codeSend:(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)->Unit): PhoneAuthProvider.OnVerificationStateChangedCallbacks{
        return withContext(Dispatchers.IO){authRepositoryWithPhone.signInUser(phone, context, phoneAuthCredential, authVerificationFailed, codeSend)}
    }
    suspend fun loginUser(credential: PhoneAuthCredential): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithPhone.loginUser(credential)}
    }
    suspend fun resendOtp(token: PhoneAuthProvider.ForceResendingToken, callBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks, context: Activity, phoneNumber: String): MyResult<String> {
        return withContext(Dispatchers.IO){authRepositoryWithPhone.resendOtp(token, callBack, context, phoneNumber)}
    }



}