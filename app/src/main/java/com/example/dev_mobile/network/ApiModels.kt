package com.example.dev_mobile.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// ==================== MODÈLES PARTAGÉS ====================

// Modèles pour les auteurs
data class Auteur(
    val id: Int? = null,
    val nom: String,
    val prenom: String? = null
)

// Modèle unifié pour les éditeurs (utilisé partout)
data class Editeur(
    val id: Int,
    val nom: String,
    val nb_jeux: Int = 0,
    val nb_contacts: Int = 0
)

// Modèle pour les contacts
data class Contact(
    val id: Int? = null,
    val nom: String,
    val email: String? = null,
    val telephone: String? = null,
    val role_profession: String? = null,
    val reservant_id: Int? = null,
    val reservant_nom: String? = null,
    val type_reservant: String? = null
)

// Modèle pour un jeu (lecture)
data class Jeu(
    val id: Int,
    val nom: String,
    val editeur_id: Int,
    @SerializedName("editeur_nom")
    val editeur_nom: String?,
    val type_jeu: String? = null,
    val age_mini: Int? = null,
    val age_maxi: Int? = null,
    val joueurs_mini: Int? = null,
    val joueurs_maxi: Int? = null,
    val taille_table: String? = null,
    val duree_moyenne: Int? = null,
    val auteurs: List<Auteur> = emptyList()
)

// Modèle pour créer/modifier un jeu (payload)
data class CreateJeuPayload(
    val nom: String,
    val editeur_id: Int,
    val type_jeu: String? = null,
    val age_mini: Int? = null,
    val age_maxi: Int? = null,
    val joueurs_mini: Int? = null,
    val joueurs_maxi: Int? = null,
    val taille_table: String? = null,
    val duree_moyenne: Int? = null,
    val auteurs: List<Auteur> = emptyList()
)

// Modèle pour créer/modifier un éditeur avec contacts
data class CreateEditeurPayload(
    val nom: String,
    val contacts: List<Contact> = emptyList()
)

// ==================== AUTH MODELS ====================

data class RegisterRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val login: String,
    val password: String
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class AuthResponse(
    val message: String? = null,
    val error: String? = null
)
