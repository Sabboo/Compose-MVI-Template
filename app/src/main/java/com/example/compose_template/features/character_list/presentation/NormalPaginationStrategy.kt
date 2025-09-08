package com.example.compose_template.features.character_list.presentation

import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.usecase.GetCharactersUseCase
import com.example.compose_template.features.character_list.presentation.mapper.toUi

class NormalPaginationStrategy(
    private val getCharactersUseCase: GetCharactersUseCase
) : PaginationStrategy {

    override fun canLoad(state: CharacterListUiState): Boolean =
        state.hasNextPage

    override fun nextPage(state: CharacterListUiState): Int =
        state.currentPage + 1

    override suspend fun fetch(page: Int): CharacterResponse =
        getCharactersUseCase(page)

    override fun updateState(
        state: CharacterListUiState,
        page: Int,
        result: CharacterResponse
    ): CharacterListUiState {
        val newChars = result.results.map { it.toUi() }
        val updated = state.characters + newChars

        return state.copy(
            characters = updated,
            currentPage = page,
            hasNextPage = result.info.next != null,
            isLoadingNextPage = false,
            paginationError = null
        )
    }
}
