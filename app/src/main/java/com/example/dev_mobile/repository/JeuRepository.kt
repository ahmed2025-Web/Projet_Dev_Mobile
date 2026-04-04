package com.example.dev_mobile.repository

import com.example.dev_mobile.network.CreateJeuRequest
import com.example.dev_mobile.network.JeuApiService
import com.example.dev_mobile.network.JeuSummaryDto
import com.example.dev_mobile.network.RetrofitClient
import org.json.JSONObject

class JeuRepository {
    private val api = RetrofitClient.retrofit.create(JeuApiService::class.java)

    suspend fun getAll(): ApiResult<List<JeuSummaryDto>> = try {
        val r = api.getJeux()
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur ${r.code()}"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun create(request: CreateJeuRequest): ApiResult<JeuSummaryDto> = try {
        val r = api.createJeu(request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!.jeu)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur création"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun update(id: Int, request: CreateJeuRequest): ApiResult<JeuSummaryDto> = try {
        val r = api.updateJeu(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!.jeu)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur modification"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun delete(id: Int): ApiResult<Unit> = try {
        val r = api.deleteJeu(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur suppression"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    private fun extractError(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        return try {
            JSONObject(raw).optString("error").ifBlank { fallback }
        } catch (_: Exception) {
            raw
        }
    }
}
