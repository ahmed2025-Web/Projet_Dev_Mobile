package com.example.dev_mobile.repository

import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.data.local.toDto
import com.example.dev_mobile.data.local.toEntity
import com.example.dev_mobile.network.*
import com.example.dev_mobile.utils.NetworkConnectivityObserver

class ReservantRepository(
    private val db: AppDatabase,
    private val connectivity: NetworkConnectivityObserver
) {
    private val api = RetrofitClient.retrofit.create(ReservantApiService::class.java)
    private val dao = db.reservantDao()

    suspend fun getAll(): ApiResult<List<ReservantDto>> {
        return if (connectivity.isCurrentlyConnected()) {
            try {
                val r = api.getAll()
                if (r.isSuccessful) {
                    val list = r.body() ?: emptyList()
                    dao.deleteAll()
                    dao.insertAll(list.map { it.toEntity() })
                    ApiResult.Success(list)
                } else {
                    val cached = dao.getAll().map { it.toDto() }
                    ApiResult.Success(cached)
                }
            } catch (e: Exception) {
                val cached = dao.getAll().map { it.toDto() }
                ApiResult.Success(cached)
            }
        } else {
            val cached = dao.getAll().map { it.toDto() }
            ApiResult.Success(cached)
        }
    }

    suspend fun getById(id: Int): ApiResult<ReservantDetailDto> {
        return if (connectivity.isCurrentlyConnected()) {
            try {
                val r = api.getById(id)
                if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
                else {
                    // Try local fallback with minimal info
                    val cached = dao.getById(id)
                    if (cached != null) {
                        ApiResult.Success(
                            ReservantDetailDto(
                                id = cached.id,
                                nom = cached.nom,
                                type_reservant = cached.type_reservant,
                                editeur_id = cached.editeur_id,
                                editeur_nom = cached.editeur_nom,
                                contacts = emptyList(),
                                historique = emptyList()
                            )
                        )
                    } else ApiResult.Error("Réservant introuvable")
                }
            } catch (e: Exception) {
                val cached = dao.getById(id)
                if (cached != null) {
                    ApiResult.Success(
                        ReservantDetailDto(
                            id = cached.id,
                            nom = cached.nom,
                            type_reservant = cached.type_reservant,
                            editeur_id = cached.editeur_id,
                            editeur_nom = cached.editeur_nom,
                            contacts = emptyList(),
                            historique = emptyList()
                        )
                    )
                } else ApiResult.Error("Serveur injoignable : ${e.message}")
            }
        } else {
            val cached = dao.getById(id)
            if (cached != null) {
                ApiResult.Success(
                    ReservantDetailDto(
                        id = cached.id,
                        nom = cached.nom,
                        type_reservant = cached.type_reservant,
                        editeur_id = cached.editeur_id,
                        editeur_nom = cached.editeur_nom,
                        contacts = emptyList(),
                        historique = emptyList()
                    )
                )
            } else ApiResult.Error("Données non disponibles hors ligne")
        }
    }

    suspend fun create(request: CreateReservantRequest): ApiResult<ReservantDetailDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Création impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.create(request)
            if (r.isSuccessful && r.body() != null) {
                val detail = r.body()!!
                // Mettre en cache le nouveau réservant
                dao.insert(
                    com.example.dev_mobile.data.local.entity.ReservantEntity(
                        id = detail.id,
                        nom = detail.nom,
                        type_reservant = detail.type_reservant,
                        editeur_id = detail.editeur_id,
                        editeur_nom = detail.editeur_nom,
                        nb_contacts = detail.contacts?.size ?: 0
                    )
                )
                ApiResult.Success(detail)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun update(id: Int, request: CreateReservantRequest): ApiResult<ReservantDetailDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Modification impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.update(id, request)
            if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
            else ApiResult.Error(r.errorBody()?.string() ?: "Erreur modification")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun delete(id: Int): ApiResult<Unit> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Suppression impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.delete(id)
            if (r.isSuccessful) {
                dao.deleteById(id)
                ApiResult.Success(Unit)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }
}