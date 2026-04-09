// app/src/main/java/com/example/dev_mobile/repository/ZoneRepository.kt
package com.example.dev_mobile.repository

import com.example.dev_mobile.network.*

class ZoneRepository {

    private val zoneTarifaireApi =
        RetrofitClient.retrofit.create(ZoneTarifaireFullApiService::class.java)
    private val zonePlanApi =
        RetrofitClient.retrofit.create(ZonePlanFullApiService::class.java)
    private val jeuxApi =
        RetrofitClient.retrofit.create(JeuxFestivalApiService::class.java)

    // ── Zones Tarifaires ───────────────────────────────────────────────────────

    suspend fun getZonesTarifaires(festivalId: Int): ApiResult<List<ZoneTarifaireDetailDto>> = try {
        val r = zoneTarifaireApi.getByFestival(festivalId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur ${r.code()}"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun createZoneTarifaire(
        festivalId: Int,
        request: CreateZoneTarifaireRequest
    ): ApiResult<ZoneTarifaireDetailDto> = try {
        val r = zoneTarifaireApi.create(festivalId, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur création (${r.code()})"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun updateZoneTarifaire(
        id: Int,
        request: UpdateZoneTarifaireRequest
    ): ApiResult<ZoneTarifaireDetailDto> = try {
        val r = zoneTarifaireApi.update(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur modification"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun deleteZoneTarifaire(id: Int): ApiResult<Unit> = try {
        val r = zoneTarifaireApi.delete(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur suppression"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    // ── Zones du Plan ──────────────────────────────────────────────────────────

    suspend fun getZonesPlan(festivalId: Int): ApiResult<List<ZonePlanDetailDto>> = try {
        val r = zonePlanApi.getByFestival(festivalId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur ${r.code()}"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun createZonePlan(
        festivalId: Int,
        request: CreateZonePlanRequest
    ): ApiResult<ZonePlanDetailDto> = try {
        val r = zonePlanApi.create(festivalId, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur création (${r.code()})"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun updateZonePlan(
        id: Int,
        request: UpdateZonePlanRequest
    ): ApiResult<ZonePlanDetailDto> = try {
        val r = zonePlanApi.update(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur modification"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun deleteZonePlan(id: Int): ApiResult<Unit> = try {
        val r = zonePlanApi.delete(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur suppression"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    // ── Placement des jeux ─────────────────────────────────────────────────────

    suspend fun placerJeu(
        zoneId: Int,
        request: PlacerJeuRequest
    ): ApiResult<ZonePlanDetailDto> = try {
        val r = zonePlanApi.placerJeu(zoneId, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur placement (${r.code()})"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun retirerJeu(zoneId: Int, jeuId: Int): ApiResult<Unit> = try {
        val r = zonePlanApi.retirerJeu(zoneId, jeuId)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur retrait"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun getJeuxFestival(festivalId: Int): ApiResult<List<JeuDisponibleDto>> = try {
        val r = jeuxApi.getJeuxFestival(festivalId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur chargement jeux"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private fun extractError(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        return try {
            org.json.JSONObject(raw).optString("error").ifBlank { fallback }
        } catch (_: Exception) { raw }
    }
}