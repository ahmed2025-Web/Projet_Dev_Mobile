
package com.example.dev_mobile.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val festivalCourantNom: String = "Chargement...",
    val festivalCourantId: Int? = null
)

class MainViewModel : ViewModel() {
    private val festivalRepository = FestivalRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadFestivalCourant()
    }

    fun loadFestivalCourant() {
        viewModelScope.launch {
            val festival = festivalRepository.getFestivalCourant()
            if (festival != null) {
                _uiState.value = MainUiState(
                    festivalCourantNom = festival.nom,
                    festivalCourantId  = festival.id
                )
            } else {
                _uiState.value = MainUiState(festivalCourantNom = "Aucun festival")
            }
        }
    }
}