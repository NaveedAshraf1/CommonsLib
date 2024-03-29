package com.lymors.lycommons.data.auth.phone

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.lymors.lycommons.utils.MyResult

interface AuthRepositoryWithPhone {
    suspend fun signInUser(phone:String, context: Activity, phoneAuthCredential:(PhoneAuthCredential)->Unit, authVerificationFailed:(String)->Unit, codeSend:(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)->Unit): PhoneAuthProvider.OnVerificationStateChangedCallbacks
    suspend fun loginUser(credential: PhoneAuthCredential): MyResult<String>
    suspend fun resendOtp(token: PhoneAuthProvider.ForceResendingToken, callBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks, context: Activity, phoneNumber: String): MyResult<String>
}