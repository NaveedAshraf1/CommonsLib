package com.lymors.lycommons.data.auth.email


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.lymors.lycommons.utils.MyResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryWithEmailImpl @Inject constructor(private val auth:FirebaseAuth):
    AuthRepositoryWithEmail {

    override suspend fun resetPassword(email: String): MyResult<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            MyResult.Success("Password reset email sent successfully")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }


    override suspend fun signUpUserWithEmailAndPassword(email: String, password: String): MyResult<String> = suspendCoroutine { cont ->
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                cont.resume(MyResult.Success("Successfully Registered."))
            }
            .addOnCanceledListener {
                cont.resume(MyResult.Error("Registration cancelled or interrupted."))
            }
            .addOnFailureListener { exception ->
                when (exception) {
                    is FirebaseAuthWeakPasswordException -> {
                        // Handle weak password exception
                        cont.resume(MyResult.Error("Weak password. Choose a stronger one."))
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Handle invalid email address exception
                        cont.resume(MyResult.Error("Invalid email format."))
                    }
                    is FirebaseAuthUserCollisionException -> {
                        // Handle collision (existing user) exception
                        cont.resume(MyResult.Error("User already exists. Please choose another email."))
                    }
                    is FirebaseAuthEmailException -> {
                        // Handle email exception (e.g., malformed email address)
                        cont.resume(MyResult.Error("Invalid email address."))
                    }
                    is FirebaseAuthRecentLoginRequiredException -> {
                        // Handle recent login required exception
                        cont.resume(MyResult.Error("Recent authentication required. Please re-authenticate."))
                    }
                    is FirebaseAuthActionCodeException -> {
                        // Handle action code exception (e.g., invalid action code)
                        cont.resume(MyResult.Error("Invalid action code."))
                    }
                    is FirebaseAuthInvalidUserException -> {
                        // Handle invalid user exception
                        cont.resume(MyResult.Error("Invalid user."))
                    }
                    else -> {
                        // Handle other Firebase Authentication exceptions
                        cont.resume(MyResult.Error("Registration failed: ${exception.message}"))
                    }
                }
            }
    }


    override suspend fun loginUserWithEmailAndPassword(email: String, password: String): MyResult<String> = suspendCoroutine { cont ->
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            cont.resume(MyResult.Success("Successfully Login."))
        }.addOnFailureListener { exception ->
            when (exception) {
                is FirebaseAuthInvalidUserException -> {
                    cont.resume(MyResult.Error("This user does not exit."))
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    cont.resume(MyResult.Error("Wrong email or password."))
                }
                else -> {
                    cont.resume(MyResult.Error("Something wrong or check email or password."))
                }
            }
        }.addOnCanceledListener {
            cont.resume(MyResult.Error("Something wrong or check internet."))
        }
    }

}