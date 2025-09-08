package com.example.compose_template.features.character_list.presentation

import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.usecase.SearchCharactersUseCase
import com.example.compose_template.features.character_list.presentation.mapper.toUi

class SearchPaginationStrategy(
    private val searchCharactersUseCase: SearchCharactersUseCase,
    private val query: String
) : PaginationStrategy {

    override fun canLoad(state: CharacterListUiState): Boolean =
        state.hasNextSearchPage

    override fun nextPage(state: CharacterListUiState): Int =
        state.currentSearchPage + 1

    override suspend fun fetch(page: Int): CharacterResponse =
        searchCharactersUseCase(query, page)

    override fun updateState(
        state: CharacterListUiState,
        page: Int,
        result: CharacterResponse
    ): CharacterListUiState {
        val newChars = result.results.map { it.toUi() }
        val updated = state.searchResults + newChars

        return state.copy(
            searchResults = updated,
            currentSearchPage = page,
            hasNextSearchPage = result.info.next != null,
            isLoadingNextPage = false,
            paginationError = null
        )
    }
}
