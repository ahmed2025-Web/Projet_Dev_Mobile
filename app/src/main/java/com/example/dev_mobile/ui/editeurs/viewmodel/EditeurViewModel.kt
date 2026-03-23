package com.example.dev_mobile.ui.editeurs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.Contact
import com.example.dev_mobile.network.CreateEditeurPayload
import com.example.dev_mobile.network.Editeur
import com.example.dev_mobile.network.EditeurApiService
import com.example.dev_mobile.network.Jeu
import com.example.dev_mobile.network.RetrofitClient
import com.example.dev_mobile.ui.editeurs.models.EditeurFormState
import com.example.dev_mobile.ui.editeurs.models.FormContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditeurListState(
    val editeurs: List<Editeur> = emptyList(),
    val selectedEditeur: Editeur? = null,
    val editeurToEdit: Editeur? = null,
    val formState: EditeurFormState = EditeurFormState(),
    val jeuxSelected: List<Jeu> = emptyList(),
    val contactsSelected: List<Contact> = emptyList(),
    val loadingList: Boolean = false,
    val loadingForm: Boolean = false,
    val loadingDetails: Boolean = false,
    val errorList: String? = null,
    val sortBy: SortBy = SortBy.NOM,
    val successMessage: String? = null,
    val editeurModificationTimes: Map<Int, Long> = emptyMap()
)

enum class SortBy {
    NOM, NOM_DESC, RECENT
}

class EditeurViewModel : ViewModel() {
    private val editeurApiService = RetrofitClient.retrofit.create(EditeurApiService::class.java)

    private val _state = MutableStateFlow(EditeurListState())
    val state: StateFlow<EditeurListState> = _state.asStateFlow()

    val editeursTries: StateFlow<List<Editeur>> = MutableStateFlow(emptyList())
    private val _editeursTries = editeursTries as MutableStateFlow<List<Editeur>>

    init {
        loadEditeurs()
    }

    // ============== CHARGEMENT DES DONNÉES ==============

    private fun loadEditeurs() {
        _state.update { it.copy(loadingList = true, errorList = null) }
        viewModelScope.launch {
            try {
                val response = editeurApiService.getEditeurs()
                if (response.isSuccessful && response.body() != null) {
                    _state.update { it.copy(editeurs = response.body()!!, loadingList = false) }
                    updateTriedEditeurs()
                } else {
                    _state.update { it.copy(loadingList = false, errorList = "Erreur lors du chargement des éditeurs") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingList = false, errorList = "Erreur lors du chargement des éditeurs") }
            }
        }
    }

    // ============== TRI DES ÉDITEURS ==============

    private fun updateTriedEditeurs() {
        val sorted = when (_state.value.sortBy) {
            SortBy.NOM -> _state.value.editeurs.sortedBy { it.nom }
            SortBy.NOM_DESC -> _state.value.editeurs.sortedByDescending { it.nom }
            SortBy.RECENT -> _state.value.editeurs.sortedByDescending { editeur ->
                _state.value.editeurModificationTimes[editeur.id] ?: 0L
            }
        }
        _editeursTries.value = sorted
    }

    fun setSortBy(sortBy: SortBy) {
        _state.update { it.copy(sortBy = sortBy) }
        updateTriedEditeurs()
    }

    // ============== SÉLECTION ET ÉDITION ==============

    fun selectEditeur(editeur: Editeur) {
        _state.update { it.copy(selectedEditeur = editeur, loadingDetails = true) }
        loadDetailsEditeur(editeur.id)
    }

    fun startCreateEditeur() {
        _state.update { it.copy(editeurToEdit = null, formState = EditeurFormState()) }
    }

    fun startEditEditeur(editeur: Editeur) {
        val formState = EditeurFormState(
            nom = editeur.nom,
            contacts = listOf(FormContact())
        )
        _state.update { it.copy(editeurToEdit = editeur, formState = formState, selectedEditeur = null) }
    }

    fun cancelEdit() {
        _state.update { it.copy(editeurToEdit = null, formState = EditeurFormState()) }
    }

    private fun loadDetailsEditeur(editeurId: Int) {
        viewModelScope.launch {
            try {
                val jeuxResponse = editeurApiService.getJeuxEditeur(editeurId)
                val contactsResponse = editeurApiService.getContactsEditeur(editeurId)

                val jeux = if (jeuxResponse.isSuccessful) jeuxResponse.body() ?: emptyList() else emptyList()
                val contacts = if (contactsResponse.isSuccessful) contactsResponse.body() ?: emptyList() else emptyList()

                _state.update {
                    it.copy(
                        jeuxSelected = jeux,
                        contactsSelected = contacts,
                        loadingDetails = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingDetails = false, errorList = "Erreur lors du chargement des détails") }
            }
        }
    }

    // ============== GESTION DU FORMULAIRE ==============

    fun updateFormField(field: String, value: String) {
        val currentForm = _state.value.formState
        val newForm = when (field) {
            "nom" -> currentForm.copy(nom = value)
            else -> currentForm
        }
        _state.update { it.copy(formState = newForm) }
    }

    fun updateContact(index: Int, field: String, value: String) {
        val contacts = _state.value.formState.contacts.toMutableList()
        if (index < contacts.size) {
            val current = contacts[index]
            contacts[index] = when (field) {
                "nom" -> current.copy(nom = value)
                "email" -> current.copy(email = value)
                "telephone" -> current.copy(telephone = value)
                "roleProfession" -> current.copy(roleProfession = value)
                else -> current
            }
            _state.update { it.copy(formState = it.formState.copy(contacts = contacts)) }
        }
    }

    fun addContact() {
        val contacts = _state.value.formState.contacts.toMutableList()
        contacts.add(FormContact())
        _state.update { it.copy(formState = it.formState.copy(contacts = contacts)) }
    }

    fun removeContact(index: Int) {
        val contacts = _state.value.formState.contacts.toMutableList()
        if (contacts.size > 1) {
            contacts.removeAt(index)
        } else {
            contacts[0] = FormContact()
        }
        _state.update { it.copy(formState = it.formState.copy(contacts = contacts)) }
    }

    // ============== VALIDATION ==============

    private fun validateForm(form: EditeurFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (form.nom.isBlank()) {
            errors["nom"] = "Le nom est obligatoire"
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

        val payload = CreateEditeurPayload(
            nom = form.nom,
            contacts = form.contacts
                .filter { it.nom.isNotBlank() }
                .map {
                    Contact(
                        nom = it.nom,
                        email = it.email.ifBlank { null },
                        telephone = it.telephone.ifBlank { null },
                        role_profession = it.roleProfession.ifBlank { null }
                    )
                }
        )

        viewModelScope.launch {
            try {
                val isEditing = _state.value.editeurToEdit != null
                val response = if (isEditing) {
                    editeurApiService.updateEditeur(_state.value.editeurToEdit!!.id, payload)
                } else {
                    editeurApiService.createEditeur(payload)
                }

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            editeurToEdit = null,
                            formState = EditeurFormState(),
                            successMessage = if (isEditing) "Éditeur modifié avec succès" else "Éditeur créé avec succès"
                        )
                    }
                    loadEditeurs()
                    updateModificationTime(_state.value.editeurToEdit?.id)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erreur lors de l'enregistrement de l'éditeur"
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
                        successMessage = e.message ?: "Erreur lors de l'enregistrement de l'éditeur"
                    )
                }
            }
        }
    }

    // ============== SUPPRESSION ==============

    fun deleteEditeur(editeur: Editeur) {
        _state.update { it.copy(loadingForm = true) }
        viewModelScope.launch {
            try {
                val response = editeurApiService.deleteEditeur(editeur.id)
                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            selectedEditeur = null,
                            successMessage = "Éditeur supprimé avec succès"
                        )
                    }
                    loadEditeurs()
                } else {
                    _state.update {
                        it.copy(
                            loadingForm = false,
                            successMessage = "Erreur lors de la suppression de l'éditeur"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadingForm = false,
                        successMessage = e.message ?: "Erreur lors de la suppression de l'éditeur"
                    )
                }
            }
        }
    }

    // ============== UTILITAIRES ==============

    private fun updateModificationTime(editeurId: Int?) {
        if (editeurId != null) {
            val times = _state.value.editeurModificationTimes.toMutableMap()
            times[editeurId] = System.currentTimeMillis()
            _state.update { it.copy(editeurModificationTimes = times) }
        }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
