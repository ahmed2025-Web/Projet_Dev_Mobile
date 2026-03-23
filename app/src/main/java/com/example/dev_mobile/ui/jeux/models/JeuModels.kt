package com.example.dev_mobile.ui.jeux.models

// Modèle UI pour l'état du formulaire (uniquement les modèles UI, les modèles réseau sont dans network/)

data class FormAuteur(
    val nom: String = "",
    val prenom: String = ""
)

data class JeuFormState(
    val nom: String = "",
    val editeurId: Int? = null,
    val typeJeu: String = "",
    val ageMini: String = "",
    val ageMaxi: String = "",
    val joueursMin: String = "",
    val joueursMax: String = "",
    val tailleTable: String = "",
    val dureeMoyenne: String = "",
    val auteurs: List<FormAuteur> = listOf(FormAuteur()),
    val errors: Map<String, String> = emptyMap()
)
