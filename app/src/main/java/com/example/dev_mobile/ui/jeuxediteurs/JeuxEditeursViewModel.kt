package com.example.dev_mobile.ui.jeuxediteurs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.ContactEditeurDto
import com.example.dev_mobile.network.CreateEditeurRequest
import com.example.dev_mobile.network.CreateJeuRequest
import com.example.dev_mobile.network.EditeurSummaryDto
import com.example.dev_mobile.network.JeuEditeurDto
import com.example.dev_mobile.network.JeuSummaryDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.EditeurRepository
import com.example.dev_mobile.repository.JeuRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class JeuxEditeursTab { EDITEURS, JEUX }

data class JeuxEditeursUiState(
    val isLoading: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val isSubmitting: Boolean = false,
    val activeTab: JeuxEditeursTab = JeuxEditeursTab.EDITEURS,
    val editeurs: List<EditeurSummaryDto> = emptyList(),
    val jeux: List<JeuSummaryDto> = emptyList(),
    val selectedEditeur: EditeurSummaryDto? = null,
    val selectedEditeurContacts: List<ContactEditeurDto> = emptyList(),
    val selectedEditeurJeux: List<JeuEditeurDto> = emptyList(),
    val showCreateEditeurDialog: Boolean = false,
    val showEditEditeurDialog: Boolean = false,
    val showCreateJeuDialog: Boolean = false,
    val showEditJeuDialog: Boolean = false,
    val editeurToEdit: EditeurSummaryDto? = null,
    val jeuToEdit: JeuSummaryDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class JeuxEditeursViewModel : ViewModel() {
    private val editeurRepository = EditeurRepository()
    private val jeuRepository = JeuRepository()

    private val _uiState = MutableStateFlow(JeuxEditeursUiState())
    val uiState: StateFlow<JeuxEditeursUiState> = _uiState

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val editeursDeferred = async { editeurRepository.getAll() }
            val jeuxDeferred = async { jeuRepository.getAll() }

            val editeursResult = editeursDeferred.await()
            val jeuxResult = jeuxDeferred.await()

            val editeurs = when (editeursResult) {
                is ApiResult.Success -> editeursResult.data
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = editeursResult.message)
                    emptyList()
                }
            }

            val jeux = when (jeuxResult) {
                is ApiResult.Success -> jeuxResult.data
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = jeuxResult.message)
                    emptyList()
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                editeurs = editeurs.sortedBy { it.nom.lowercase() },
                jeux = jeux.sortedBy { it.nom.lowercase() }
            )
        }
    }

    fun setActiveTab(tab: JeuxEditeursTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun openEditeurDetails(editeur: EditeurSummaryDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedEditeur = editeur,
                isLoadingDetails = true,
                errorMessage = null
            )

            val contactsDeferred = async { editeurRepository.getContacts(editeur.id) }
            val jeuxDeferred = async { editeurRepository.getJeux(editeur.id) }

            val contactsResult = contactsDeferred.await()
            val jeuxResult = jeuxDeferred.await()

            val contacts = when (contactsResult) {
                is ApiResult.Success -> contactsResult.data
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = contactsResult.message)
                    emptyList()
                }
            }

            val jeux = when (jeuxResult) {
                is ApiResult.Success -> jeuxResult.data
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = jeuxResult.message)
                    emptyList()
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoadingDetails = false,
                selectedEditeurContacts = contacts,
                selectedEditeurJeux = jeux
            )
        }
    }

    fun closeEditeurDetails() {
        _uiState.value = _uiState.value.copy(
            selectedEditeur = null,
            selectedEditeurContacts = emptyList(),
            selectedEditeurJeux = emptyList()
        )
    }

    fun openCreateEditeurDialog() {
        _uiState.value = _uiState.value.copy(showCreateEditeurDialog = true)
    }

    fun closeCreateEditeurDialog() {
        _uiState.value = _uiState.value.copy(showCreateEditeurDialog = false)
    }

    fun openEditEditeurDialog(editeur: EditeurSummaryDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                errorMessage = null,
                editeurToEdit = editeur,
                selectedEditeurContacts = emptyList()
            )

            when (val r = editeurRepository.getContacts(editeur.id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showEditEditeurDialog = true,
                        selectedEditeurContacts = r.data
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showEditEditeurDialog = false,
                        editeurToEdit = null,
                        errorMessage = r.message
                    )
                }
            }
        }
    }

    fun closeEditEditeurDialog() {
        _uiState.value = _uiState.value.copy(showEditEditeurDialog = false, editeurToEdit = null)
    }

    fun createEditeur(request: CreateEditeurRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = editeurRepository.create(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showCreateEditeurDialog = false,
                        successMessage = "Éditeur créé avec succès"
                    )
                    loadAll()
                }

                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = r.message
                )
            }
        }
    }

    fun updateEditeur(id: Int, request: CreateEditeurRequest) {
        viewModelScope.launch {
            val selectedBeforeUpdate = _uiState.value.selectedEditeur
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = editeurRepository.update(id, request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showEditEditeurDialog = false,
                        editeurToEdit = null,
                        successMessage = "Éditeur modifié"
                    )
                    loadAll()

                    // If user had this editor details open, reload them to avoid stale contacts.
                    if (selectedBeforeUpdate?.id == id) {
                        openEditeurDetails(selectedBeforeUpdate.copy(nom = r.data.nom))
                    }
                }

                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = r.message
                )
            }
        }
    }

    fun deleteEditeur(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = editeurRepository.delete(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        selectedEditeur = if (_uiState.value.selectedEditeur?.id == id) null else _uiState.value.selectedEditeur,
                        selectedEditeurContacts = if (_uiState.value.selectedEditeur?.id == id) emptyList() else _uiState.value.selectedEditeurContacts,
                        selectedEditeurJeux = if (_uiState.value.selectedEditeur?.id == id) emptyList() else _uiState.value.selectedEditeurJeux,
                        successMessage = "Éditeur supprimé"
                    )
                    loadAll()
                }

                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = r.message
                )
            }
        }
    }

    fun openCreateJeuDialog() {
        _uiState.value = _uiState.value.copy(showCreateJeuDialog = true)
    }

    fun closeCreateJeuDialog() {
        _uiState.value = _uiState.value.copy(showCreateJeuDialog = false)
    }

    fun openEditJeuDialog(jeu: JeuSummaryDto) {
        _uiState.value = _uiState.value.copy(showEditJeuDialog = true, jeuToEdit = jeu)
    }

    fun closeEditJeuDialog() {
        _uiState.value = _uiState.value.copy(showEditJeuDialog = false, jeuToEdit = null)
    }

    fun createJeu(request: CreateJeuRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = jeuRepository.create(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showCreateJeuDialog = false,
                        successMessage = "Jeu créé avec succès"
                    )
                    loadAll()
                }

                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = r.message
                )
            }
        }
    }

    fun updateJeu(id: Int, request: CreateJeuRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = jeuRepository.update(id, request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showEditJeuDialog = false,
                        jeuToEdit = null,
                        successMessage = "Jeu modifié"
                    )
                    loadAll()
                }

                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = r.message
                )
            }
        }
    }

    fun deleteJeu(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = jeuRepository.delete(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Jeu supprimé"
                    )
                    loadAll()
                }

                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = r.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
