
package com.example.dev_mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.repository.AuthRepository
import com.example.dev_mobile.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Remplissez tous les champs")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            _uiState.value = when (val result = repository.login(login, password)) {
                is AuthResult.Success -> AuthUiState(isSuccess = true)
                is AuthResult.Error   -> AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun register(nom: String, prenom: String, email: String, login: String, password: String) {
        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || login.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Remplissez tous les champs")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            _uiState.value = when (val result = repository.register(nom, prenom, email, login, password)) {
                is AuthResult.Success -> AuthUiState(isSuccess = true)
                is AuthResult.Error   -> AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}