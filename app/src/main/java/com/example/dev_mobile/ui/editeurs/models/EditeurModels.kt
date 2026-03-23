package com.example.dev_mobile.ui.editeurs.models

// Modèles UI pour l'état du formulaire

data class FormContact(
    val nom: String = "",
    val email: String = "",
    val telephone: String = "",
    val roleProfession: String = ""
)

data class EditeurFormState(
    val nom: String = "",
    val contacts: List<FormContact> = listOf(FormContact()),
    val errors: Map<String, String> = emptyMap()
)
