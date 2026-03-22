
package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val login: String,
    val password: String
)

data class LoginRequest(
    val login: String,
    val password: String
)


data class UserDto(
    val login: String,
    val role: String
)

data class LoginResponse(
    val message: String? = null,
    val user: UserDto? = null,
    val error: String? = null
)

data class AuthResponse(
    val message: String? = null,
    val error: String? = null
)

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<AuthResponse>
}