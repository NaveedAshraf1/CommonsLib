package com.lymors.lycommons.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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






    // Check if the network is available
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false

        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

}

