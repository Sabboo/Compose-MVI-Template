package com.example.compose_template.features.character_list.presentation

import com.example.compose_template.features.character_list.presentation.model.CharacterUi

data class CharacterListUiState(
    val characters: List<CharacterUi> = emptyList(),
    val searchResults: List<CharacterUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val paginationError: String? = null,
    val hasNextPage: Boolean = true,
    val currentPage: Int = 1,
    val isEmpty: Boolean = false,
    val isSearchMode: Boolean = false,
    val currentSearchPage: Int = 0,
    val hasNextSearchPage: Boolean = false,
    val canLoadNextPage: Boolean = true
)