package com.example.dev_mobile.repository

import com.example.dev_mobile.network.*

class UsersRepository {
    private val api = RetrofitClient.retrofit.create(UsersApiService::class.java)

    suspend fun getAllUsers(): ApiResult<List<FullUserDto>> = try {
        val r = api.getAllUsers()
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error("Erreur ${r.code()}")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun getPendingUsers(): ApiResult<List<PendingUserDto>> = try {
        val r = api.getPendingUsers()
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error("Erreur ${r.code()}")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun changeRole(id: Int, role: String): ApiResult<UserActionResponse> = try {
        val r = api.changeRole(id, ChangeRoleRequest(role))
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur changement de rôle")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun validateAccount(id: Int, role: String): ApiResult<UserActionResponse> = try {
        val r = api.validateAccount(id, ValidateAccountRequest(role))
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur validation compte")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun deleteUser(id: Int): ApiResult<Unit> = try {
        val r = api.deleteUser(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun createUser(request: CreateUserRequest): ApiResult<UserActionResponse> = try {
        val r = api.createUser(request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création utilisateur")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
}
