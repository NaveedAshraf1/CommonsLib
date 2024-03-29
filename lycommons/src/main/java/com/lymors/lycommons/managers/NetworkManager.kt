package com.lymors.lycommons.managers

import android.content.Context
import android.net.ConnectivityManager
import com.lymors.lycommons.models.UserModel
import com.lymors.lycommons.utils.MyResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

//    <uses-permission android:name="android.permission.INTERNET" />
//    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
//implementation 'com.squareup.retrofit2:converter-gson:2.9.0'



class NetworkManager(val context: Context , baseUrl:String) {


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl) // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }



    suspend fun fetchUsers(): MyResult<List<UserModel>> {
        return try {
            if (!isNetworkAvailable()) {
                MyResult.Error("No network available")
            } else {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    MyResult.Success(response.body() ?: emptyList())
                } else {
                    MyResult.Error("API error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            MyResult.Error("Network or unexpected error: ${e.message}")
        }
    }

    suspend fun uploadUser(userModel: UserModel): MyResult<String> {
        return try {
            if (!isNetworkAvailable()) {
                MyResult.Error("No network available")
            } else {
                val response = apiService.uploadUser(userModel)
                if (response.isSuccessful) {
                    MyResult.Success("User uploaded successfully")
                } else {
                    MyResult.Error("API error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            MyResult.Error("Network or unexpected error: ${e.message}")
        }
    }

    suspend fun updateUser(userModel: UserModel): MyResult<String> {
        return try {
            if (!isNetworkAvailable()) {
                MyResult.Error("No network available")
            } else {
                val response = apiService.updateUser(userModel.key, userModel)
                if (response.isSuccessful) {
                    MyResult.Success("User updated successfully")
                } else {
                    MyResult.Error("API error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            MyResult.Error("Network or unexpected error: ${e.message}")
        }
    }

    suspend fun deleteUser(userId: String): MyResult<String> {
        return try {
            if (!isNetworkAvailable()) {
                MyResult.Error("No network available")
            } else {
                val response = apiService.deleteUser(userId)
                if (response.isSuccessful) {
                    MyResult.Success("User deleted successfully")
                } else {
                    MyResult.Error("API error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            MyResult.Error("Network or unexpected error: ${e.message}")
        }
    }



    // Check if the network is available
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


}



interface ApiService {

    @GET("users")
    suspend fun getUsers(): retrofit2.Response<List<UserModel>>

    @POST("users")
    suspend fun uploadUser(@Body userModel: UserModel): retrofit2.Response<Void>

    @PUT("users/{userId}")
    suspend fun updateUser(@Path("userId") userId: String, @Body userModel: UserModel): retrofit2.Response<Void>

    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String):retrofit2. Response<Void>
}