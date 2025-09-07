package com.example.compose_template.presentation

import com.example.compose_template.createMockCharacterUI
import com.example.compose_template.features.character_list.presentation.CharacterListUiState
import com.example.compose_template.features.character_list.presentation.model.CharacterUi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharacterListUiStateTest {

    @Test
    fun `initial state should be empty`() {
        val state = CharacterListUiState()
        assertTrue(state.characters.isEmpty())
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingNextPage)
        assertFalse(state.isSearchMode)
        assertFalse(state.isSearching)
        assertNull(state.error)
        assertNull(state.paginationError)
        assertTrue(state.hasNextPage)
        assertEquals(1, state.currentPage)
        assertEquals(0, state.currentSearchPage)
    }

    @Test
    fun `state with characters should not be empty`() {
        val characters = listOf(createMockCharacterUI(1), createMockCharacterUI(2))
        val state = CharacterListUiState(characters = characters)
        assertEquals(2, state.characters.size)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `search mode with results should return searchResults`() {
        val characters = listOf(createMockCharacterUI(1, "Ricky"), createMockCharacterUI(2))
        val searchResults = listOf(createMockCharacterUI(1, "Ricky"))

        val state = CharacterListUiState(
            characters = characters,
            searchResults = searchResults,
            isSearchMode = true
        )

        assertEquals(searchResults.size, state.searchResults.size)
        assertEquals(searchResults.first().name, state.searchResults.first().name)
        assertTrue(state.isSearchMode)
    }

    @Test
    fun `search mode with no results should be empty`() {
        val characters = emptyList<CharacterUi>()
        val state = CharacterListUiState(
            characters = characters,
            searchResults = emptyList(),
            isSearchMode = true,
            isEmpty = true
        )

        assertTrue(state.searchResults.isEmpty())
        assertTrue(state.isSearchMode)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `pagination error should be set`() {
        val state = CharacterListUiState(
            characters = emptyList(),
            paginationError = "Failed to load next page"
        )

        assertNotNull(state.paginationError)
        assertEquals("Failed to load next page", state.paginationError)
    }

    @Test
    fun `loading next page should set isLoadingNextPage true`() {
        val state = CharacterListUiState(isLoadingNextPage = true)
        assertTrue(state.isLoadingNextPage)
    }

    @Test
    fun `error state should mark empty list`() {
        val state = CharacterListUiState(
            characters = emptyList(),
            error = "Network error",
            isEmpty = true
        )

        assertEquals("Network error", state.error)
        assertTrue(state.isEmpty)
    }
}
