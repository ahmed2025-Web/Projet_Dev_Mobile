package com.example.dev_mobile.data.local

import com.example.dev_mobile.data.local.entity.FestivalEntity
import com.example.dev_mobile.data.local.entity.ReservantEntity
import com.example.dev_mobile.data.local.entity.ReservationEntity
import com.example.dev_mobile.network.FestivalDashboardDto
import com.example.dev_mobile.network.ReservantDto
import com.example.dev_mobile.network.ReservationDto

// ── Festival ──────────────────────────────────────────────────────────────────

fun FestivalDashboardDto.toEntity() = FestivalEntity(
    id = id,
    nom = nom,
    est_actif = est_actif,
    est_courant = est_courant,
    espace_tables_total = espace_tables_total,
    date_debut = date_debut,
    date_fin = date_fin,
    stock_tables_standard = stock_tables_standard,
    stock_tables_grandes = stock_tables_grandes,
    stock_tables_mairie = stock_tables_mairie,
    stock_chaises_standard = stock_chaises_standard,
    stock_chaises_mairie = stock_chaises_mairie,
    prix_prise_electrique = prix_prise_electrique,
    nb_zones_tarifaires = nb_zones_tarifaires,
    nb_zones_plan = nb_zones_plan,
    tables_totales_tarifaires = tables_totales_tarifaires,
    nb_reservations_totales = nb_reservations_totales,
    nb_reservations_confirmees = nb_reservations_confirmees,
    nb_presents = nb_presents,
    nb_absents = nb_absents,
    nb_factures = nb_factures,
    montant_total_factures = montant_total_factures,
    nb_factures_payees = nb_factures_payees,
    montant_paye = montant_paye,
    created_at = created_at
)

fun FestivalEntity.toDto() = FestivalDashboardDto(
    id = id,
    nom = nom,
    est_actif = est_actif,
    est_courant = est_courant,
    espace_tables_total = espace_tables_total,
    date_debut = date_debut,
    date_fin = date_fin,
    stock_tables_standard = stock_tables_standard,
    stock_tables_grandes = stock_tables_grandes,
    stock_tables_mairie = stock_tables_mairie,
    stock_chaises_standard = stock_chaises_standard,
    stock_chaises_mairie = stock_chaises_mairie,
    prix_prise_electrique = prix_prise_electrique,
    nb_zones_tarifaires = nb_zones_tarifaires,
    nb_zones_plan = nb_zones_plan,
    tables_totales_tarifaires = tables_totales_tarifaires,
    nb_reservations_totales = nb_reservations_totales,
    nb_reservations_confirmees = nb_reservations_confirmees,
    nb_presents = nb_presents,
    nb_absents = nb_absents,
    nb_factures = nb_factures,
    montant_total_factures = montant_total_factures,
    nb_factures_payees = nb_factures_payees,
    montant_paye = montant_paye,
    created_at = created_at
)

// ── Réservant ─────────────────────────────────────────────────────────────────

fun ReservantDto.toEntity() = ReservantEntity(
    id = id,
    nom = nom,
    type_reservant = type_reservant,
    editeur_id = editeur_id,
    editeur_nom = editeur_nom,
    nb_contacts = nb_contacts,
    nb_reservations = nb_reservations,
    created_at = created_at,
    updated_at = updated_at
)

fun ReservantEntity.toDto() = ReservantDto(
    id = id,
    nom = nom,
    type_reservant = type_reservant,
    editeur_id = editeur_id,
    editeur_nom = editeur_nom,
    nb_contacts = nb_contacts,
    nb_reservations = nb_reservations,
    created_at = created_at,
    updated_at = updated_at
)

// ── Réservation ───────────────────────────────────────────────────────────────

fun ReservationDto.toEntity() = ReservationEntity(
    id = id,
    festival_id = festival_id,
    festival_nom = festival_nom,
    reservant_id = reservant_id,
    reservant_nom = reservant_nom,
    type_reservant = type_reservant,
    editeur_id = editeur_id,
    editeur_nom = editeur_nom,
    etat_contact = etat_contact,
    etat_presence = etat_presence,
    date_dernier_contact = date_dernier_contact,
    nb_prises_electriques = nb_prises_electriques,
    viendra_animer = viendra_animer,
    remise_tables = remise_tables,
    remise_montant = remise_montant,
    notes = notes,
    created_at = created_at,
    updated_at = updated_at,
    nb_contacts = nb_contacts,
    nb_tables_reservees = nb_tables_reservees,
    montant_tables = montant_tables,
    montant_prises = montant_prises,
    montant_brut = montant_brut,
    nb_jeux = nb_jeux,
    nb_jeux_places = nb_jeux_places,
    nb_jeux_recus = nb_jeux_recus
)

fun ReservationEntity.toDto() = ReservationDto(
    id = id,
    festival_id = festival_id,
    festival_nom = festival_nom,
    reservant_id = reservant_id,
    reservant_nom = reservant_nom,
    type_reservant = type_reservant,
    editeur_id = editeur_id,
    editeur_nom = editeur_nom,
    etat_contact = etat_contact,
    etat_presence = etat_presence,
    date_dernier_contact = date_dernier_contact,
    nb_prises_electriques = nb_prises_electriques,
    viendra_animer = viendra_animer,
    remise_tables = remise_tables,
    remise_montant = remise_montant,
    notes = notes,
    created_at = created_at,
    updated_at = updated_at,
    nb_contacts = nb_contacts,
    nb_tables_reservees = nb_tables_reservees,
    montant_tables = montant_tables,
    montant_prises = montant_prises,
    montant_brut = montant_brut,
    nb_jeux = nb_jeux,
    nb_jeux_places = nb_jeux_places,
    nb_jeux_recus = nb_jeux_recus
)