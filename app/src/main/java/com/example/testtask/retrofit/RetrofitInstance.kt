package com.example.testtask.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://lk.pravoe-delo.su/api/v1/"

    private val interceptor = HttpLoggingInterceptor()

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    private val gson : Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    init {
        interceptor.level = HttpLoggingInterceptor.Level.BODY
    }

    fun<T> create(service : Class<T>) : T{
        return retrofit.create(service)
    }
}