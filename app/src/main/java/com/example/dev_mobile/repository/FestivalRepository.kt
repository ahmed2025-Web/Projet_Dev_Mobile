package com.example.dev_mobile.repository

import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.data.local.toDto
import com.example.dev_mobile.data.local.toEntity
import com.example.dev_mobile.network.*
import com.example.dev_mobile.utils.NetworkConnectivityObserver


/**
 * FestivalRepository avec support offline-first.
 * - Si connecté : appel API puis mise en cache locale.
 * - Si hors ligne : données du cache Room.
 */
class FestivalRepository(
    private val db: AppDatabase,
    private val connectivity: NetworkConnectivityObserver
) {
    private val api  = RetrofitClient.retrofit.create(FestivalApiService::class.java)
    private val dao  = db.festivalDao()

    suspend fun getFestivalCourant(): FestivalDashboardDto? {
        return if (connectivity.isCurrentlyConnected()) {
            try {
                val r = api.getFestivalCourant()
                if (r.isSuccessful && r.body() != null) {
                    val dto = r.body()!!
                    dao.insert(dto.toEntity())
                    dto
                } else {
                    dao.getCourant()?.toDto()
                }
            } catch (e: Exception) {
                dao.getCourant()?.toDto()
            }
        } else {
            dao.getCourant()?.toDto()
        }
    }

    suspend fun getAll(): ApiResult<List<FestivalDashboardDto>> {
        return if (connectivity.isCurrentlyConnected()) {
            try {
                val r = api.getAll()
                if (r.isSuccessful) {
                    val list = r.body() ?: emptyList()
                    dao.deleteAll()
                    dao.insertAll(list.map { it.toEntity() })
                    ApiResult.Success(list)
                } else {
                    // Fallback cache
                    val cached = dao.getAll().map { it.toDto() }
                    if (cached.isNotEmpty()) ApiResult.Success(cached)
                    else ApiResult.Error("Erreur ${r.code()}")
                }
            } catch (e: Exception) {
                val cached = dao.getAll().map { it.toDto() }
                if (cached.isNotEmpty()) ApiResult.Success(cached)
                else ApiResult.Error("Serveur injoignable : ${e.message}")
            }
        } else {
            val cached = dao.getAll().map { it.toDto() }
            ApiResult.Success(cached) // toujours succès, peut être vide
        }
    }

    suspend fun create(request: CreateFestivalRequest): ApiResult<FestivalDashboardDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Création impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.create(request)
            if (r.isSuccessful && r.body() != null) {
                val dto = r.body()!!
                dao.insert(dto.toEntity())
                ApiResult.Success(dto)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun update(id: Int, request: UpdateFestivalRequest): ApiResult<FestivalDashboardDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Modification impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.update(id, request)
            if (r.isSuccessful && r.body() != null) {
                val dto = r.body()!!
                dao.insert(dto.toEntity())
                ApiResult.Success(dto)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur modification")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun setCourant(id: Int): ApiResult<FestivalDashboardDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne.")
        }
        return try {
            val r = api.setCourant(id)
            if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
            else ApiResult.Error(r.errorBody()?.string() ?: "Erreur")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun canDelete(id: Int): ApiResult<CanDeleteResponse> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Vérification impossible hors ligne.")
        }
        return try {
            val r = api.canDelete(id)
            if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
            else ApiResult.Error("Erreur vérification")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun delete(id: Int): ApiResult<Unit> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Suppression impossible hors ligne.")
        }
        return try {
            val r = api.delete(id)
            if (r.isSuccessful) {
                // Supprimer du cache local aussi
                // Note: on recharge toute la liste après
                ApiResult.Success(Unit)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    fun hasCachedData(): Boolean = true // vérifié via count() si besoin
}