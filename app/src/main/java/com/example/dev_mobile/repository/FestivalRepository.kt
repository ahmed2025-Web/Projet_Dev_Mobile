package com.example.dev_mobile.repository

import com.example.dev_mobile.network.*

class FestivalRepository {
    private val api = RetrofitClient.retrofit.create(FestivalApiService::class.java)

    suspend fun getFestivalCourant(): FestivalDashboardDto? = try {
        val r = api.getFestivalCourant()
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun getAll(): ApiResult<List<FestivalDashboardDto>> = try {
        val r = api.getAll()
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error("Erreur ${r.code()}")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun create(request: CreateFestivalRequest): ApiResult<FestivalDashboardDto> = try {
        val r = api.create(request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun update(id: Int, request: UpdateFestivalRequest): ApiResult<FestivalDashboardDto> = try {
        val r = api.update(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur modification")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun setCourant(id: Int): ApiResult<FestivalDashboardDto> = try {
        val r = api.setCourant(id)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun canDelete(id: Int): ApiResult<CanDeleteResponse> = try {
        val r = api.canDelete(id)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error("Erreur vérification")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun delete(id: Int): ApiResult<Unit> = try {
        val r = api.delete(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
}