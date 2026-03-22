
package com.example.dev_mobile.session

/**
 * Stocke les infos de l'utilisateur connecté en mémoire.
 * Remis à null au logout.
 */
object UserSession {

    var login: String? = null
    var role: String? = null

    fun isLoggedIn() = role != null

    // Compte en attente de validation admin
    fun isPending() = role == "user"

    fun isAdmin() = role == "admin"
    fun isSuperOrganisateur() = role == "super organisateur"
    fun isOrganisateur() = role == "organisateur"
    fun isBenevole() = role == "benevole"
    fun isVisiteur() = role == "visiteur"

    // A accès aux fonctions de base (pas juste en attente)
    fun hasAccess() = role != null && role != "user"

    // Peut gérer les festivals, réservations, etc.
    fun canManage() = role in listOf("admin", "super organisateur", "organisateur")

    // Peut tout faire
    fun canAdmin() = role in listOf("admin", "super organisateur")

    fun clear() {
        login = null
        role = null
    }

    fun getRoleLabel(): String = when (role) {
        "admin"              -> "Administrateur"
        "super organisateur" -> "Super Organisateur"
        "organisateur"       -> "Organisateur"
        "benevole"           -> "Bénévole"
        "visiteur"           -> "Visiteur"
        "user"               -> "En attente de validation"
        else                 -> "Inconnu"
    }

    fun getRoleColor(): Long = when (role) {
        "admin"              -> 0xFF7B1FA2 // violet
        "super organisateur" -> 0xFF1565C0 // bleu foncé
        "organisateur"       -> 0xFF1976D2 // bleu
        "benevole"           -> 0xFF388E3C // vert
        "visiteur"           -> 0xFF0288D1 // bleu clair
        "user"               -> 0xFFE65100 // orange (en attente)
        else                 -> 0xFF757575 // gris
    }
}