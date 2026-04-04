package com.example.dev_mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── Festival ──────────────────────────────────────────────────────────────────
@Entity(tableName = "festivals")
data class FestivalEntity(
    @PrimaryKey val id: Int,
    val nom: String,
    val est_actif: Boolean = true,
    val est_courant: Boolean = false,
    val espace_tables_total: Int,
    val date_debut: String? = null,
    val date_fin: String? = null,
    val stock_tables_standard: Int = 0,
    val stock_tables_grandes: Int = 0,
    val stock_tables_mairie: Int = 0,
    val stock_chaises_standard: Int = 0,
    val stock_chaises_mairie: Int = 0,
    val prix_prise_electrique: Double = 0.0,
    val nb_zones_tarifaires: Int = 0,
    val nb_zones_plan: Int = 0,
    val tables_totales_tarifaires: Int = 0,
    val nb_reservations_totales: Int = 0,
    val nb_reservations_confirmees: Int = 0,
    val nb_presents: Int = 0,
    val nb_absents: Int = 0,
    val nb_factures: Int = 0,
    val montant_total_factures: Double = 0.0,
    val nb_factures_payees: Int = 0,
    val montant_paye: Double = 0.0,
    val created_at: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)

// ── Réservant ─────────────────────────────────────────────────────────────────
@Entity(tableName = "reservants")
data class ReservantEntity(
    @PrimaryKey val id: Int,
    val nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val editeur_nom: String? = null,
    val nb_contacts: Int = 0,
    val nb_reservations: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)

// ── Réservation ───────────────────────────────────────────────────────────────
@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: Int,
    val festival_id: Int,
    val festival_nom: String,
    val reservant_id: Int,
    val reservant_nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val editeur_nom: String? = null,
    val etat_contact: String = "pas_contacte",
    val etat_presence: String = "non_defini",
    val date_dernier_contact: String? = null,
    val nb_prises_electriques: Int = 0,
    val viendra_animer: Boolean = true,
    val remise_tables: Int = 0,
    val remise_montant: Double = 0.0,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val nb_contacts: Int = 0,
    val nb_tables_reservees: Int = 0,
    val montant_tables: Double = 0.0,
    val montant_prises: Double = 0.0,
    val montant_brut: Double = 0.0,
    val nb_jeux: Int = 0,
    val nb_jeux_places: Int = 0,
    val nb_jeux_recus: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
)

// ── Opération en attente (mutations offline) ──────────────────────────────────
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,         // CREATE_RESERVATION, UPDATE_WORKFLOW_CONTACT, etc.
    val entityType: String,   // reservation, reservant, festival
    val entityId: Int? = null,
    val payload: String,      // JSON serialisé
    val festivalId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null
)