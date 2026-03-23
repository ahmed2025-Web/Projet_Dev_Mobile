package com.example.dev_mobile.ui.jeux.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.Auteur
import com.example.dev_mobile.network.CreateJeuPayload
import com.example.dev_mobile.network.Editeur
import com.example.dev_mobile.network.Jeu
import com.example.dev_mobile.network.JeuxApiService
import com.example.dev_mobile.network.RetrofitClient
import com.example.dev_mobile.ui.jeux.models.FormAuteur
import com.example.dev_mobile.ui.jeux.models.JeuFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class JeuxListState(
    val jeux: List<Jeu> = emptyList(),
    val editeurs: List<Editeur> = emptyList(),
    val selectedJeu: Jeu? = null,
    val jeuToEdit: Jeu? = null,
    val formState: JeuFormState = JeuFormState(),
    val loadingList: Boolean = false,
    val loadingEditeurs: Boolean = false,
    val loadingForm: Boolean = false,
    val errorList: String? = null,
    val sortBy: SortBy = SortBy.NOM,
    val successMessage: String? = null,
    val jeuModificationTimes: Map<Int, Long> = emptyMap()
)

enum class SortBy {
    NOM, NOM_DESC, RECENT
}

class JeuxViewModel : ViewModel() {
    private val jeuxApiService = RetrofitClient.retrofit.create(JeuxApiService::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private val _state = MutableStateFlow(JeuxListState())
    val state: StateFlow<JeuxListState> = _state.asStateFlow()

    val jeuxTries: StateFlow<List<Jeu>> = MutableStateFlow(emptyList())
    private val _jeuxTries = jeuxTries as MutableStateFlow<List<Jeu>>

    init {
        loadJeux()
        loadEditeurs()
    }

    // ============== CHARGEMENT DES DONNÉES ==============

    private fun loadJeux() {
        _state.update { it.copy(loadingList = true, errorList = null) }
        viewModelScope.launch {
            try {
                val response = jeuxApiService.getJeux()
                if (response.isSuccessful && response.body() != null) {
                    _state.update { it.copy(jeux = response.body()!!, loadingList = false) }
                    updateTriedJeux()
                } else {
                    _state.update { it.copy(loadingList = false, errorList = "Erreur lors du chargement des jeux") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingList = false, errorList = "Erreur lors du chargement des jeux") }
            }
        }
    }

    private fun loadEditeurs() {
        _state.update { it.copy(loadingEditeurs = true) }
        viewModelScope.launch {
            try {
                val response = jeuxApiService.getEditeurs()
                if (response.isSuccessful && response.body() != null) {
                    _state.update { it.copy(editeurs = response.body()!!, loadingEditeurs = false) }
                } else {
                    _state.update { it.copy(loadingEditeurs = false, editeurs = emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingEditeurs = false, editeurs = emptyList()) }
            }
        }
    }

    // ============== TRI DES JEUX ==============

    private fun updateTriedJeux() {
        val sorted = when (_state.value.sortBy) {
            SortBy.NOM -> _state.value.jeux.sortedBy { it.nom }
            SortBy.NOM_DESC -> _state.value.jeux.sortedByDescending { it.nom }
            SortBy.RECENT -> _state.value.jeux.sortedByDescending { jeu ->
                _state.value.jeuModificationTimes[jeu.id] ?: 0L
            }
        }
        _jeuxTries.value = sorted
    }

    fun setSortBy(sortBy: SortBy) {
        _state.update { it.copy(sortBy = sortBy) }
        updateTriedJeux()
    }

    // ============== SÉLECTION ET ÉDITION ==============

    fun selectJeu(jeu: Jeu) {
        _state.update { it.copy(selectedJeu = jeu) }
    }

    fun startCreateJeu() {
        _state.update { it.copy(jeuToEdit = null, formState = JeuFormState()) }
    }

    fun startEditJeu(jeu: Jeu) {
        val formState = JeuFormState(
            nom = jeu.nom,
            editeurId = jeu.editeur_id,
            typeJeu = jeu.type_jeu ?: "",
            ageMini = jeu.age_mini?.toString() ?: "",
            ageMaxi = jeu.age_maxi?.toString() ?: "",
            joueursMin = jeu.joueurs_mini?.toString() ?: "",
            joueursMax = jeu.joueurs_maxi?.toString() ?: "",
            tailleTable = jeu.taille_table ?: "",
            dureeMoyenne = jeu.duree_moyenne?.toString() ?: "",
            auteurs = jeu.auteurs.map { FormAuteur(nom = it.nom, prenom = it.prenom ?: "") }
                .ifEmpty { listOf(FormAuteur()) }
        )
        _state.update { it.copy(jeuToEdit = jeu, formState = formState, selectedJeu = null) }
    }

    fun cancelEdit() {
        _state.update { it.copy(jeuToEdit = null, formState = JeuFormState()) }
    }

    // ============== GESTION DU FORMULAIRE ==============

    fun updateFormField(field: String, value: String) {
        val currentForm = _state.value.formState
        val newForm = when (field) {
            "nom" -> currentForm.copy(nom = value)
            "editeurId" -> currentForm.copy(editeurId = value.toIntOrNull())
            "typeJeu" -> currentForm.copy(typeJeu = value)
            "ageMini" -> currentForm.copy(ageMini = value)
            "ageMaxi" -> currentForm.copy(ageMaxi = value)
            "joueursMin" -> currentForm.copy(joueursMin = value)
            "joueursMax" -> currentForm.copy(joueursMax = value)
            "tailleTable" -> currentForm.copy(tailleTable = value)
            "dureeMoyenne" -> currentForm.copy(dureeMoyenne = value)
            else -> currentForm
        }
        _state.update { it.copy(formState = newForm) }
    }

    fun updateAuteur(index: Int, field: String, value: String) {
        val auteurs = _state.value.formState.auteurs.toMutableList()
        if (index < auteurs.size) {
            val current = auteurs[index]
            auteurs[index] = when (field) {
                "nom" -> current.copy(nom = value)
                "prenom" -> current.copy(prenom = value)
                else -> current
            }
            _state.update { it.copy(formState = it.formState.copy(auteurs = auteurs)) }
        }
    }

    fun addAuteur() {
        val auteurs = _state.value.formState.auteurs.toMutableList()
        auteurs.add(FormAuteur())
        _state.update { it.copy(formState = it.formState.copy(auteurs = auteurs)) }
    }

    fun removeAuteur(index: Int) {
        val auteurs = _state.value.formState.auteurs.toMutableList()
        // Toujours garder au moins un auteur
        if (auteurs.size > 1) {
            auteurs.removeAt(index)
        } else {
            auteurs[0] = FormAuteur()
        }
        _state.update { it.copy(formState = it.formState.copy(auteurs = auteurs)) }
    }

    // ============== VALIDATION ==============

    private fun validateForm(form: JeuFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (form.nom.isBlank()) {
            errors["nom"] = "Le nom est obligatoire"
        }
        if (form.editeurId == null || form.editeurId <= 0) {
            errors["editeurId"] = "Un éditeur doit être sélectionné"
        }
        if (form.auteurs.isEmpty() || form.auteurs.all { it.nom.isBlank() }) {
            errors["auteurs"] = "Au moins un auteur est obligatoire"
        }

        return errors
    }

    // ============== SOUMISSION DU FORMULAIRE ==============

    fun submitForm() {
        val form = _state.value.formState
        val errors = validateForm(form)

        if (errors.isNotEmpty()) {
            _state.update { it.copy(formState = it.formState.copy(errors = errors)) }
            return
        }

        _state.update { it.copy(loadingForm = true, formState = it.formState.copy(errors = emptyMap())) }

        val payload = CreateJeuPayload(
            nom = form.nom,
            editeur_id = form.editeurId ?: 0,
            type_jeu = form.typeJeu.ifBlank { null },
            age_mini = form.ageMini.toIntOrNull(),
            age_maxi = form.ageMaxi.toIntOrNull(),
            joueurs_mini = form.joueursMin.toIntOrNull(),
            joueurs_maxi = form.joueursMax.toIntOrNull(),
            taille_table = form.tailleTable.ifBlank { null },
            duree_moyenne = form.dureeMoyenne.toIntOrNull(),
            auteurs = form.auteurs
                .filter { it.nom.isNotBlank() }
                .map { Auteur(nom = it.nom, prenom = it.prenom.ifBlank { null }) }
        )

        viewModelScope.launch {
            try {
                val isEditing = _state.value.jeuToEdit != null
                val response = if (isEditing) {
                    jeuxApiService.updateJeu(_state.value.jeuToEdit!!.id, payload)
                } else {
                    jeuxApiService.createJeu(payload)
                }

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            jeuToEdit = null,
                            formState = JeuFormState(),
                            successMessage = if (isEditing) "Jeu modifié avec succès" else "Jeu créé avec succès"
                        )
                    }
                    loadJeux()
                    updateModificationTime(_state.value.jeuToEdit?.id)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erreur lors de l'enregistrement du jeu"
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            successMessage = errorMsg
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadingForm = false,
                        successMessage = e.message ?: "Erreur lors de l'enregistrement du jeu"
                    )
                }
            }
        }
    }

    // ============== SUPPRESSION ==============

    fun deleteJeu(jeu: Jeu) {
        _state.update { it.copy(loadingForm = true) }
        viewModelScope.launch {
            try {
                val response = jeuxApiService.deleteJeu(jeu.id)
                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            selectedJeu = null,
                            successMessage = "Jeu supprimé avec succès"
                        )
                    }
                    loadJeux()
                } else {
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            successMessage = "Erreur lors de la suppression du jeu"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadingForm = false,
                        successMessage = e.message ?: "Erreur lors de la suppression du jeu"
                    )
                }
            }
        }
    }

    // ============== UTILITAIRES ==============

    private fun updateModificationTime(jeuId: Int?) {
        if (jeuId != null) {
            val times = _state.value.jeuModificationTimes.toMutableMap()
            times[jeuId] = System.currentTimeMillis()
            _state.update { it.copy(jeuModificationTimes = times) }
        }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
