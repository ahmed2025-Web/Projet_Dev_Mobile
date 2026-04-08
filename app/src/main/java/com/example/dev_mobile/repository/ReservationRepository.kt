package com.example.dev_mobile.repository

import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.data.local.toDto
import com.example.dev_mobile.data.local.toEntity
import com.example.dev_mobile.network.*
import com.example.dev_mobile.utils.NetworkConnectivityObserver

class ReservationRepository(
    private val db: AppDatabase,
    private val connectivity: NetworkConnectivityObserver
) {
    private val api      = RetrofitClient.retrofit.create(ReservationApiService::class.java)
    private val zonesApi = RetrofitClient.retrofit.create(ZoneTarifaireApiService::class.java)
    private val dao      = db.reservationDao()

    suspend fun getByFestival(festivalId: Int): ApiResult<List<ReservationDto>> {
        return if (connectivity.isCurrentlyConnected()) {
            try {
                val r = api.getByFestival(festivalId)
                if (r.isSuccessful) {
                    val list = r.body() ?: emptyList()
                    dao.deleteByFestival(festivalId)
                    dao.insertAll(list.map { it.toEntity() })
                    ApiResult.Success(list)
                } else {
                    val cached = dao.getByFestival(festivalId).map { it.toDto() }
                    ApiResult.Success(cached)
                }
            } catch (e: Exception) {
                val cached = dao.getByFestival(festivalId).map { it.toDto() }
                ApiResult.Success(cached)
            }
        } else {
            val cached = dao.getByFestival(festivalId).map { it.toDto() }
            ApiResult.Success(cached)
        }
    }

    suspend fun getById(id: Int): ApiResult<ReservationDetailDto> {
        return if (connectivity.isCurrentlyConnected()) {
            try {
                val r = api.getById(id)
                if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
                else buildDetailFromCache(id)
            } catch (e: Exception) {
                buildDetailFromCache(id)
            }
        } else {
            buildDetailFromCache(id)
        }
    }

    private suspend fun buildDetailFromCache(id: Int): ApiResult<ReservationDetailDto> {
        val cached = dao.getById(id) ?: return ApiResult.Error("Données non disponibles hors ligne")
        return ApiResult.Success(
            ReservationDetailDto(
                id = cached.id,
                festival_id = cached.festival_id,
                festival_nom = cached.festival_nom,
                reservant_id = cached.reservant_id,
                reservant_nom = cached.reservant_nom,
                type_reservant = cached.type_reservant,
                editeur_id = cached.editeur_id,
                editeur_nom = cached.editeur_nom,
                etat_contact = cached.etat_contact,
                etat_presence = cached.etat_presence,
                date_dernier_contact = cached.date_dernier_contact,
                nb_prises_electriques = cached.nb_prises_electriques,
                viendra_animer = cached.viendra_animer,
                remise_tables = cached.remise_tables,
                remise_montant = cached.remise_montant,
                notes = cached.notes,
                created_at = cached.created_at,
                updated_at = cached.updated_at,
                nb_contacts = cached.nb_contacts,
                nb_tables_reservees = cached.nb_tables_reservees,
                montant_tables = cached.montant_tables,
                montant_prises = cached.montant_prises,
                montant_brut = cached.montant_brut,
                nb_jeux = cached.nb_jeux,
                nb_jeux_places = cached.nb_jeux_places,
                nb_jeux_recus = cached.nb_jeux_recus,
                contacts = emptyList(),
                zones_reservees = emptyList(),
                jeux = emptyList()
            )
        )
    }

    suspend fun create(festivalId: Int, request: CreateReservationRequest): ApiResult<ReservationDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Création impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.create(festivalId, request)
            if (r.isSuccessful && r.body() != null) {
                val dto = r.body()!!
                dao.insert(dto.toEntity())
                ApiResult.Success(dto)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur création")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun update(id: Int, request: UpdateReservationRequest): ApiResult<ReservationDto> {
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

    suspend fun updateWorkflowContact(id: Int, etat: String): ApiResult<ReservationDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.updateWorkflowContact(id, UpdateWorkflowContactRequest(etat))
            if (r.isSuccessful && r.body() != null) {
                dao.updateEtatContact(id, etat)
                ApiResult.Success(r.body()!!)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur workflow")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun updateWorkflowPresence(id: Int, etat: String): ApiResult<ReservationDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.updateWorkflowPresence(id, UpdateWorkflowPresenceRequest(etat))
            if (r.isSuccessful && r.body() != null) {
                dao.updateEtatPresence(id, etat)
                ApiResult.Success(r.body()!!)
            } else ApiResult.Error(r.errorBody()?.string() ?: "Erreur workflow")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun addContactRelance(id: Int, request: AddContactRelanceRequest): ApiResult<ContactRelanceDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.addContactRelance(id, request)
            if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
            else ApiResult.Error(r.errorBody()?.string() ?: "Erreur ajout contact")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun getZonesTarifaires(festivalId: Int): ApiResult<List<ZoneTarifaireDto>> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Success(emptyList())
        }
        return try {
            val r = zonesApi.getByFestival(festivalId)
            if (r.isSuccessful) ApiResult.Success(r.body() ?: emptyList())
            else ApiResult.Error("Erreur ${r.code()}")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    // ── Gestion des jeux de réservation ───────────────────────────────────────

    suspend fun addJeu(reservationId: Int, jeuId: Int, nbExemplaires: Int, tablesAllouees: Double): ApiResult<JeuFestivalDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.addJeu(reservationId, AddJeuFestivalRequest(
                jeu_id = jeuId,
                nombre_exemplaires = nbExemplaires,
                tables_allouees = tablesAllouees
            ))
            if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
            else ApiResult.Error(r.errorBody()?.string() ?: "Erreur ajout jeu")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun removeJeu(reservationId: Int, jeuFestivalId: Int): ApiResult<Unit> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.removeJeu(reservationId, jeuFestivalId)
            if (r.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(r.errorBody()?.string() ?: "Erreur suppression jeu")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }

    suspend fun updateJeuRecu(reservationId: Int, jeuFestivalId: Int, recu: Boolean): ApiResult<JeuFestivalDto> {
        if (!connectivity.isCurrentlyConnected()) {
            return ApiResult.Error("Action impossible hors ligne. Reconnectez-vous.")
        }
        return try {
            val r = api.updateJeuRecu(reservationId, jeuFestivalId, UpdateJeuRecuRequest(recu))
            if (r.isSuccessful && r.body() != null) ApiResult.Success(r.body()!!)
            else ApiResult.Error(r.errorBody()?.string() ?: "Erreur mise à jour")
        } catch (e: Exception) { ApiResult.Error("Serveur injoignable : ${e.message}") }
    }
}