package com.example.dev_mobile.ui.jeuxediteurs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.JeuDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.JeuxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

sealed class JeuxEditeursUiState {
    data object Loading : JeuxEditeursUiState()
    data class Success(
        val jeux: List<JeuDto>,
        val query: String = ""
    ) : JeuxEditeursUiState() {
        val filteredJeux: List<JeuDto>
            get() {
                val trimmed = query.trim().lowercase(Locale.ROOT)
                if (trimmed.isEmpty()) return jeux

                return jeux.filter { jeu ->
                    val nomMatch = jeu.nom.orEmpty().lowercase(Locale.ROOT).contains(trimmed)
                    val publisherMatch = jeu.publisherName.orEmpty().lowercase(Locale.ROOT).contains(trimmed)
                    val auteurMatch = jeu.auteurs.orEmpty().any { auteur ->
                        listOfNotNull(auteur.prenom, auteur.nom)
                            .joinToString(" ")
                            .lowercase(Locale.ROOT)
                            .contains(trimmed)
                    }
                    nomMatch || publisherMatch || auteurMatch
                }
            }
    }
    data class Error(val message: String) : JeuxEditeursUiState()
}

class JeuxEditeursViewModel : ViewModel() {
    private val repository = JeuxRepository()

    private val _uiState = MutableStateFlow<JeuxEditeursUiState>(JeuxEditeursUiState.Loading)
    val uiState: StateFlow<JeuxEditeursUiState> = _uiState.asStateFlow()

    init {
        loadJeux()
    }

    fun loadJeux() {
        viewModelScope.launch {
            _uiState.value = JeuxEditeursUiState.Loading

            when (val result = repository.getAll()) {
                is ApiResult.Success -> {
                    _uiState.value = JeuxEditeursUiState.Success(jeux = result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = JeuxEditeursUiState.Error(result.message)
                }
            }
        }
    }

    fun updateQuery(value: String) {
        _uiState.update { state ->
            if (state is JeuxEditeursUiState.Success) {
                state.copy(query = value)
            } else {
                state
            }
        }
    }
}
