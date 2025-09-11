package com.example.compose_template

import kotlinx.coroutines.flow.StateFlow

interface BaseViewModel<UiState, UiIntent> {
    val uiState: StateFlow<UiState>
    fun handleIntent(intent: UiIntent)
}