package com.lymors.lycommons.extensions
//
//import android.content.Context
//import okhttp3.ConnectionPool
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import retrofit2.Converter
//import retrofit2.Retrofit
//import java.util.concurrent.TimeUnit
//
//
//object NetworkUtils {
//
//    private var CONNECT_TIMEOUT = 90L
//    private var READ_TIMEOUT = 90L
//    private var WRITE_TIMEOUT = 90L
//
//    fun buildRetrofit(
//        baseUrl: String,
//        converterFactory: Converter.Factory,
//        context: Context,
//        interceptor: Interceptor,
//        isDebug: Boolean
//    ): Retrofit {
//
//        return Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(createClientBuilder(context, interceptor, isDebug))
//            .addConverterFactory(converterFactory)
//            .build()
//    }
//
//    fun buildRetrofit(
//        baseUrl: String,
//        converterFactory: Converter.Factory,
//        isDebug: Boolean
//    ): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(createClientBuilder(isDebug).build())
//            .addConverterFactory(converterFactory)
//            .build()
//    }
//
//    private fun createClientBuilder(isDebug: Boolean): OkHttpClient.Builder {
//        val clientBuilder = OkHttpClient.Builder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
//            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
//            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)
//            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
//        if (isDebug) {
//            clientBuilder.addInterceptor(makeLoggingInterceptor())
//        }
//        return clientBuilder
//    }
//
//    private fun createClientBuilder(
//        context: Context,
//        interceptor: Interceptor,
//        isDebug: Boolean
//    ): OkHttpClient {
//
//        val builder = OkHttpClient.Builder()
//
//        with(builder) {
//
//            addInterceptor(interceptor)
//
//            val httpLoggingInterceptor =
//                HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//
//            if (isDebug) {
//                addNetworkInterceptor(DebugInterceptor(context))
//                addInterceptor(httpLoggingInterceptor)
//            }
//
//            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
//            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
//            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
//            retryOnConnectionFailure(true)
//
//        }
//
//        return builder.build()
//    }
//
//    private fun makeLoggingInterceptor(): HttpLoggingInterceptor {
//        val debugInterceptor = HttpLoggingInterceptor()
//        debugInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        return debugInterceptor
//    }
//
//}