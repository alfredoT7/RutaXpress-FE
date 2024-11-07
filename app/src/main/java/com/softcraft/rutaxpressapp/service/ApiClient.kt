package com.softcraft.rutaxpressapp.service

import com.softcraft.rutaxpressapp.routes.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_BACKEND_EXPRESS_URL = "https://ruta-xpress-backend-express-js.vercel.app/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_BACKEND_EXPRESS_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}