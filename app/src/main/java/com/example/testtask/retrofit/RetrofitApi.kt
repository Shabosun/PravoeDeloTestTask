package com.example.testtask.retrofit

import com.example.testtask.retrofit.model.Code
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitApi {



    @Headers("Content-Type:application/json")
    @GET("getCode")
    suspend fun getCode(@Header("Authorization") auth : String, @Query("login") number : String) : Response<Code>

    @Headers("Content-Type:application/json")
    @GET("getToken")
    suspend fun getToken(@Header("Authorization") auth : String, @Query("login") number : String, @Query("password") code : String) : Response<String>

    @Headers("Content-Type:application/json")
    @GET("regenerateCode")
    suspend fun regenerateCode(@Header("Authorization") auth : String, @Query("login") number : String) : Response<String>




}