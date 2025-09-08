package com.example.compose_template.features.character_list.presentation

import com.example.compose_template.features.character_list.domain.model.CharacterResponse

interface PaginationStrategy {
    fun canLoad(state: CharacterListUiState): Boolean
    fun nextPage(state: CharacterListUiState): Int
    suspend fun fetch(page: Int): CharacterResponse
    fun updateState(
        state: CharacterListUiState,
        page: Int,
        result: CharacterResponse
    ): CharacterListUiState
}