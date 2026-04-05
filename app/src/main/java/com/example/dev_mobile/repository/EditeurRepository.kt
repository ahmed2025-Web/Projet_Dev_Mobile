package com.example.dev_mobile.repository

import com.example.dev_mobile.network.ContactEditeurDto
import com.example.dev_mobile.network.CreateEditeurRequest
import com.example.dev_mobile.network.EditeurApiService
import com.example.dev_mobile.network.EditeurSummaryDto
import com.example.dev_mobile.network.JeuEditeurDto
import com.example.dev_mobile.network.RetrofitClient
import org.json.JSONObject

class EditeurRepository {
    private val api = RetrofitClient.retrofit.create(EditeurApiService::class.java)

    suspend fun getAll(): ApiResult<List<EditeurSummaryDto>> = try {
        val r = api.getEditeurs()
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur ${r.code()}"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun getJeux(editeurId: Int): ApiResult<List<JeuEditeurDto>> = try {
        val r = api.getJeuxEditeur(editeurId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur ${r.code()}"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun getContacts(editeurId: Int): ApiResult<List<ContactEditeurDto>> = try {
        val r = api.getContactsEditeur(editeurId)
        if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur ${r.code()}"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun create(request: CreateEditeurRequest): ApiResult<EditeurSummaryDto> = try {
        val r = api.createEditeur(request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!.editeur)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur création"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun update(id: Int, request: CreateEditeurRequest): ApiResult<EditeurSummaryDto> = try {
        val r = api.updateEditeur(id, request)
        if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
        else ApiResult.Error(extractError(r.errorBody()?.string(), "Erreur modification"))
    } catch (e: Exception) {
        ApiResult.Error("Serveur injoignable : ${e.message}")
    }

    suspend fun delete(id: Int): ApiResult<Unit> = try {
        val r = api.deleteEditeur(id)
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
