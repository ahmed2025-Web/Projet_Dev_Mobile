
package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Tous les modèles (RegisterRequest, LoginRequest, AuthResponse) 
// sont définis dans ApiModels.kt pour éviter la duplication

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<AuthResponse>
}

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<AuthResponse>
}