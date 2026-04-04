
package com.example.dev_mobile.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.repository.AuthRepository
import com.example.dev_mobile.repository.AuthResult
import com.example.dev_mobile.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isPending: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /**
     * Vérifie si une session est déjà active sur le téléphone.
     * C'est le coeur du mode "Offline First".
     */
    fun checkSession() {
        val savedLogin = prefs.getString("user_login", null)
        val savedRole  = prefs.getString("user_role", null)

        if (savedLogin != null && savedRole != null) {
            // On restaure la session en mémoire
            UserSession.login = savedLogin
            UserSession.role  = savedRole
            // On informe l'UI qu'on est déjà connecté
            _uiState.value = AuthUiState(isSuccess = true)
        }
    }

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Remplissez tous les champs")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = repository.login(login, password)) {
                is AuthResult.Success -> {
                    // SAUVEGARDE PERSISTANTE pour le Offline First
                    prefs.edit()
                        .putString("user_login", UserSession.login)
                        .putString("user_role", UserSession.role)
                        .apply()
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is AuthResult.PendingValidation -> _uiState.value = AuthUiState(isPending = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
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
                is AuthResult.Success           -> AuthUiState(isSuccess = true)
                is AuthResult.PendingValidation -> AuthUiState(isPending = true)
                is AuthResult.Error             -> AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState(isSuccess = false)
        viewModelScope.launch {
            repository.logout()
            // ON EFFACE TOUT (Mémoire + Disque)
            UserSession.clear()
            prefs.edit().clear().apply()
            _uiState.value = AuthUiState()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}