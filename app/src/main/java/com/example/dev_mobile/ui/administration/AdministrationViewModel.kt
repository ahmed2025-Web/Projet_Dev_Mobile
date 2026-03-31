package com.example.dev_mobile.ui.administration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.CreateUserRequest
import com.example.dev_mobile.network.PendingUserDto
import com.example.dev_mobile.network.FullUserDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoadingUsers: Boolean = false,
    val isLoadingPending: Boolean = false,
    val users: List<FullUserDto> = emptyList(),
    val pendingUsers: List<PendingUserDto> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedTab: Int = 0  // 0 = Utilisateurs, 1 = En attente
)

class AdministrationViewModel : ViewModel() {
    private val repository = UsersRepository()
    private val _uiState = MutableStateFlow(AdminUiState())
    //
    val uiState: StateFlow<AdminUiState> = _uiState

    init {
        loadAll()
    }

    fun loadAll() {
        loadUsers()
        loadPendingUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUsers = true)
            when (val r = repository.getAllUsers()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoadingUsers = false, users = r.data
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoadingUsers = false, errorMessage = r.message
                )
            }
        }
    }

    fun loadPendingUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPending = true)
            when (val r = repository.getPendingUsers()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoadingPending = false, pendingUsers = r.data
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoadingPending = false, errorMessage = r.message
                )
            }
        }
    }

    fun changeRole(userId: Int, newRole: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = repository.changeRole(userId, newRole)) {
                is ApiResult.Success -> {
                    loadUsers()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Rôle mis à jour avec succès ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun validateAccount(userId: Int, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val r = repository.validateAccount(userId, role)) {
                is ApiResult.Success -> {
                    loadPendingUsers()
                    loadUsers()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Compte validé avec succès ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val r = repository.deleteUser(userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        users = _uiState.value.users.filter { it.id != userId },
                        successMessage = "Utilisateur supprimé 🗑️"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun createUser(nom: String, prenom: String, email: String, login: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val req = CreateUserRequest(nom, prenom, email, login, password, role)
            when (val r = repository.createUser(req)) {
                is ApiResult.Success -> {
                    loadUsers()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Utilisateur créé avec succès ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun setTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
