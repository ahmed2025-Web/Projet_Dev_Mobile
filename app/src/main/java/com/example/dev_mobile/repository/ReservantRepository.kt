// app/src/main/java/com/example/dev_mobile/repository/ReservantRepository.kt
package com.example.dev_mobile.repository

import com.example.dev_mobile.network.*

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class ReservantRepository {
    private val api = RetrofitClient.retrofit.create(ReservantApiService::class.java)

    suspend fun getAll(): ApiResult<List<ReservantDto>> = try {
        val r = api.getAll()
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error("Erreur ${r.code()}")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun getById(id: Int): ApiResult<ReservantDetailDto> = try {
        val r = api.getById(id)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error("Réservant introuvable")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun create(request: CreateReservantRequest): ApiResult<ReservantDetailDto> = try {
        val r = api.create(request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun update(id: Int, request: CreateReservantRequest): ApiResult<ReservantDetailDto> = try {
        val r = api.update(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur modification")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun delete(id: Int): ApiResult<Unit> = try {
        val r = api.delete(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
}