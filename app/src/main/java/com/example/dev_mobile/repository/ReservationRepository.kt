package com.example.dev_mobile.repository

import com.example.dev_mobile.network.*

class ReservationRepository {
    private val api = RetrofitClient.retrofit.create(ReservationApiService::class.java)
    private val zonesApi = RetrofitClient.retrofit.create(ZoneTarifaireApiService::class.java)

    suspend fun getByFestival(festivalId: Int): ApiResult<List<ReservationDto>> = try {
        val r = api.getByFestival(festivalId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error("Erreur ${r.code()}")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun getById(id: Int): ApiResult<ReservationDetailDto> = try {
        val r = api.getById(id)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error("Réservation introuvable")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun create(festivalId: Int, request: CreateReservationRequest): ApiResult<ReservationDto> = try {
        val r = api.create(festivalId, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun update(id: Int, request: UpdateReservationRequest): ApiResult<ReservationDto> = try {
        val r = api.update(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur modification")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun delete(id: Int): ApiResult<Unit> = try {
        val r = api.delete(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun updateWorkflowContact(id: Int, etat: String): ApiResult<ReservationDto> = try {
        val r = api.updateWorkflowContact(id, UpdateWorkflowContactRequest(etat))
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur workflow")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun updateWorkflowPresence(id: Int, etat: String): ApiResult<ReservationDto> = try {
        val r = api.updateWorkflowPresence(id, UpdateWorkflowPresenceRequest(etat))
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur workflow")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun addContactRelance(id: Int, request: AddContactRelanceRequest): ApiResult<ContactRelanceDto> = try {
        val r = api.addContactRelance(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(r.errorBody()?.string() ?: "Erreur ajout contact")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }

    suspend fun getZonesTarifaires(festivalId: Int): ApiResult<List<ZoneTarifaireDto>> = try {
        val r = zonesApi.getByFestival(festivalId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error("Erreur ${r.code()}")
    } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
}