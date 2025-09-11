package com.example.compose_template

import androidx.lifecycle.ViewModel
import com.example.compose_template.features.character_list.presentation.CharacterListIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.compose_template.features.character_list.presentation.CharacterListUiState


class FakeCharacterListViewModel(
    initialUiState: CharacterListUiState
) : ViewModel(), BaseViewModel<CharacterListUiState, CharacterListIntent> {

    private val _uiState = MutableStateFlow(initialUiState)
    override val uiState: StateFlow<CharacterListUiState> = _uiState

    override fun handleIntent(intent: CharacterListIntent) {
        // Do Nothing, Just for testing with States
    }

    fun setUiState(newState: CharacterListUiState) {
        _uiState.value = newState
    }
}
