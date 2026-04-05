package com.example.dev_mobile.repository

import com.example.dev_mobile.network.*
import com.example.dev_mobile.utils.NetworkConnectivityObserver

class PublicRepository(
    private val connectivity: NetworkConnectivityObserver
) {
    private val publicApi       = RetrofitClient.retrofit.create(PublicApiService::class.java)
    private val zonePlanApi     = RetrofitClient.retrofit.create(ZonePlanPublicApiService::class.java)

    // ── Jeux du festival courant ──────────────────────────────────────────────

    suspend fun getJeuxFestivalCourant(): ApiResult<List<JeuPublicDto>> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Données non disponibles hors ligne")
        }
        return try {
            val r = publicApi.getJeuxFestivalCourant()
            if (r.isSuccessful) {
                ApiResult.Success(r.body() ?: emptyList())
            } else {
                ApiResult.Error("Erreur ${r.code()} : ${r.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Serveur injoignable : ${e.message}")
        }
    }

    // ── Éditeurs représentés au festival courant ──────────────────────────────

    suspend fun getEditeursFestivalCourant(): ApiResult<List<EditeurPublicDto>> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Données non disponibles hors ligne")
        }
        return try {
            val r = publicApi.getEditeursFestivalCourant()
            if (r.isSuccessful) {
                ApiResult.Success(r.body() ?: emptyList())
            } else {
                ApiResult.Error("Erreur ${r.code()} : ${r.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Serveur injoignable : ${e.message}")
        }
    }

    // ── Festival courant (info publique) ──────────────────────────────────────

    suspend fun getFestivalCourant(): ApiResult<FestivalPublicDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Données non disponibles hors ligne")
        }
        return try {
            val r = publicApi.getFestivalCourant()
            if (r.isSuccessful && r.body() != null) {
                ApiResult.Success(r.body()!!)
            } else {
                ApiResult.Error("Aucun festival courant")
            }
        } catch (e: Exception) {
            ApiResult.Error("Serveur injoignable : ${e.message}")
        }
    }

    // ── Zones du plan d'un festival ───────────────────────────────────────────

    suspend fun getZonesPlan(festivalId: Int): ApiResult<List<ZonePlanPublicDto>> {
        if (festivalId <= 0) return ApiResult.Success(emptyList())
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Données non disponibles hors ligne")
        }
        return try {
            val r = zonePlanApi.getZonesByFestival(festivalId)
            if (r.isSuccessful) {
                ApiResult.Success(r.body() ?: emptyList())
            } else {
                ApiResult.Error("Erreur ${r.code()} : ${r.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Serveur injoignable : ${e.message}")
        }
    }
}