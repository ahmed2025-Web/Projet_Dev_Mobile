package com.example.dev_mobile.network


import retrofit2.Response
import retrofit2.http.*

data class FullUserDto(
    val id: Int,
    val nom: String? = null,
    val prenom: String? = null,
    val email: String,
    val login: String,
    val role: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class PendingUserDto(
    val id: Int,
    val nom: String? = null,
    val prenom: String? = null,
    val email: String,
    val login: String,
    val created_at: String? = null
)

data class ChangeRoleRequest(
    val role: String
)

data class ValidateAccountRequest(
    val role: String
)

data class CreateUserRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val login: String,
    val password: String,
    val role: String
)

data class UserActionResponse(
    val message: String? = null,
    val user: FullUserDto? = null,
    val error: String? = null
)

interface UsersApiService {

    @GET("api/users")
    suspend fun getAllUsers(): Response<List<FullUserDto>>

    @GET("api/users/pending")
    suspend fun getPendingUsers(): Response<List<PendingUserDto>>

    @PATCH("api/users/{id}/role")
    suspend fun changeRole(
        @Path("id") id: Int,
        @Body body: ChangeRoleRequest
    ): Response<UserActionResponse>

    @PATCH("api/users/{id}/validate")
    suspend fun validateAccount(
        @Path("id") id: Int,
        @Body body: ValidateAccountRequest
    ): Response<UserActionResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<UserActionResponse>

    @POST("api/users")
    suspend fun createUser(@Body body: CreateUserRequest): Response<UserActionResponse>
}