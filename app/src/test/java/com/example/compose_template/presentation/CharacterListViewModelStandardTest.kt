package com.example.compose_template.presentation

import app.cash.turbine.test
import com.example.compose_template.createMockCharacter
import com.example.compose_template.createMockCharacterUI
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.model.ResponseInfo
import com.example.compose_template.features.character_list.domain.usecase.GetCharactersUseCase
import com.example.compose_template.features.character_list.domain.usecase.SearchCharactersUseCase
import com.example.compose_template.features.character_list.presentation.CharacterListIntent
import com.example.compose_template.features.character_list.presentation.CharacterListUiState
import com.example.compose_template.features.character_list.presentation.CharacterListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever


@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class CharacterListViewModelStandardTest {

    @Mock
    private lateinit var getCharactersUseCase: GetCharactersUseCase

    @Mock
    private lateinit var searchCharactersUseCase: SearchCharactersUseCase

    private lateinit var viewModel: CharacterListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load should fetch first page of characters`() = runTest {
        val mockCharacters = listOf(createMockCharacter(1, "Rick"))
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 2, pages = 1, next = null, prev = null),
            results = mockCharacters
        )

        whenever(getCharactersUseCase(1)).thenReturn(mockResponse)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)

        viewModel.uiState.test {
            // Initial Default State
            awaitItem()
            // ViewModel state update in loadInitialData() to apply isLoading
            awaitItem()

            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(mockResponse.results.size, state.characters.size)
            assertEquals(mockResponse.results.first().name, state.characters[0].name)
            assertFalse(state.hasNextPage)
            assertNull(state.error)
            assertEquals(1, state.currentPage)
        }
    }

    @Test
    fun `load next page should append characters to existing list`() = runTest {
        val firstPageCharacters = listOf(createMockCharacter(1, "Rick"))
        val secondPageCharacters = listOf(createMockCharacter(2, "Morty"))

        val firstPageResponse = CharacterResponse(
            info = ResponseInfo(count = 2, pages = 2, next = "page2", prev = null),
            results = firstPageCharacters
        )
        val secondPageResponse = CharacterResponse(
            info = ResponseInfo(count = 2, pages = 2, next = null, prev = "page1"),
            results = secondPageCharacters
        )

        whenever(getCharactersUseCase(1)).thenReturn(firstPageResponse)
        whenever(getCharactersUseCase(2)).thenReturn(secondPageResponse)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)

        viewModel.handleIntent(CharacterListIntent.LoadNextPage)

        viewModel.uiState.test {
            // Initial Default State
            awaitItem()
            // ViewModel state update in loadInitialData() to apply isLoading
            awaitItem()

            // Loading first page
            val loadingFirstPageState = awaitItem()
            assertEquals(1, loadingFirstPageState.currentPage)
            assertFalse(loadingFirstPageState.isLoadingNextPage)

            val firstPageState = awaitItem()
            assertTrue(firstPageState.isLoadingNextPage)

            val secondPageState = awaitItem()

            assertEquals(
                firstPageCharacters.size + secondPageCharacters.size,
                secondPageState.characters.size
            )
            assertEquals(firstPageCharacters.first().name, secondPageState.characters[0].name)
            assertEquals(secondPageCharacters.first().name, secondPageState.characters[1].name)
            assertEquals(2, secondPageState.currentPage)
            assertFalse(secondPageState.hasNextPage)
            assertFalse(secondPageState.isLoadingNextPage)
        }
    }

    @Test
    fun `search filters cached characters immediately`() = runTest {
        val mockCharacters = listOf(
            createMockCharacter(1, "Rick Sanchez"),
            createMockCharacter(2, "Morty Smith")
        )
        whenever(getCharactersUseCase(1)).thenReturn(
            CharacterResponse(
                info = ResponseInfo(count = 2, pages = 1, next = null, prev = null),
                results = mockCharacters
            )
        )

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)

        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.Search("Rick"))

        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.searchResults.map { it.name })
                .containsExactly("Rick Sanchez")
        }
    }


    @Test
    fun `search should perform client-side filtering immediately and have cached results to be displayed even when no server results returned`() =
        runTest {
            val mockCharacters = listOf(
                createMockCharacter(1, "Rick Sanchez"),
                createMockCharacter(2, "Morty Smith")
            )
            val mockResponse = CharacterResponse(
                info = ResponseInfo(count = 2, pages = 1, next = null, prev = null),
                results = mockCharacters
            )

            whenever(getCharactersUseCase(1)).thenReturn(mockResponse)
            whenever(searchCharactersUseCase(query = "Rick", page = 1)).thenReturn(
                CharacterResponse(
                    info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
                    results = listOf(
                        createMockCharacter(1, "Rick Sanchez")
                    )
                )
            )

            viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)

            viewModel.handleIntent(CharacterListIntent.Search("Rick"))

            viewModel.uiState.test {
                skipItems(4)
                val state = awaitItem()
                assertTrue { state.searchResults.isNotEmpty() }
                assertThat { state.searchResults.size == 2 }
            }
        }

    @Test
    fun `state with search mode should reflect search properties`() {
        val searchResults = listOf(
            createMockCharacterUI(id = 1, name = "Rick")
        )
        val state = CharacterListUiState(
            searchResults = searchResults,
            searchQuery = "Rick",
            isSearchMode = true,
            currentSearchPage = 2,
            hasNextSearchPage = false
        )

        assertTrue(state.isSearchMode)
        assertEquals("Rick", state.searchQuery)
        assertEquals(1, state.searchResults.size)
        assertEquals(2, state.currentSearchPage)
        assertFalse(state.hasNextSearchPage)
    }

    @Test
    fun `search should perform server-side search after debounce`() = runTest {
        val mockCharacters = listOf(createMockCharacter(1, "Rick Sanchez"))
        val initialResponse = CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = mockCharacters
        )
        val searchResponse = CharacterResponse(
            info = ResponseInfo(count = 5, pages = 1, next = null, prev = null),
            results = listOf(
                createMockCharacter(1, "Rick Sanchez"),
                createMockCharacter(3, "Rick Prime"),
                createMockCharacter(4, "Rick C-137")
            )
        )

        whenever(getCharactersUseCase(1)).thenReturn(initialResponse)
        whenever(searchCharactersUseCase(query = "Rick", page = 1)).thenReturn(searchResponse)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.Search("Rick"))
        advanceTimeBy(400)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.searchResults.size)
            assertFalse(state.isSearching)
            assertTrue(state.isSearchMode)
        }
    }

    @Test
    fun `search pagination should work correctly`() = runTest {
        val initialSearchResponse = CharacterResponse(
            info = ResponseInfo(count = 30, pages = 2, next = "page2", prev = null),
            results = listOf(createMockCharacter(1, "Rick 1"))
        )
        val secondPageResponse = CharacterResponse(
            info = ResponseInfo(count = 30, pages = 2, next = null, prev = "page1"),
            results = listOf(createMockCharacter(2, "Rick 2"))
        )

        whenever(getCharactersUseCase(1)).thenReturn(
            CharacterResponse(
                info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
                results = emptyList()
            )
        )
        whenever(
            searchCharactersUseCase(
                query = "Rick",
                page = 1
            )
        ).thenReturn(initialSearchResponse)
        whenever(searchCharactersUseCase(query = "Rick", page = 2)).thenReturn(secondPageResponse)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.Search("Rick"))
        advanceTimeBy(400)

        viewModel.handleIntent(CharacterListIntent.LoadNextPage)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.searchResults.size)
            assertEquals("Rick 1", state.searchResults[0].name)
            assertEquals("Rick 2", state.searchResults[1].name)
            assertEquals(2, state.currentSearchPage)
            assertFalse(state.hasNextSearchPage)
        }
    }

    @Test
    fun `clear search should restore cached characters`() = runTest {
        val mockCharacters = listOf(createMockCharacter(1), createMockCharacter(2))
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 2, pages = 1, next = null, prev = null),
            results = mockCharacters
        )

        whenever(getCharactersUseCase(1)).thenReturn(mockResponse)
        whenever(searchCharactersUseCase(query = "Rick", page = 1)).thenReturn(
            CharacterResponse(
                info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
                results = listOf(createMockCharacter(1))
            )
        )

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.Search("Rick"))
        advanceTimeBy(400)

        viewModel.handleIntent(CharacterListIntent.ClearSearch)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSearchMode)
            assertEquals("", state.searchQuery)
            assertEquals(2, state.characters.size)
            assertTrue(state.searchResults.isEmpty())
            assertEquals(1, state.currentSearchPage)
            assertTrue(state.hasNextSearchPage)
        }
    }

    @Test
    fun `error during initial load should update error state`() = runTest {
        val errorMessage = "Network error"
        whenever(getCharactersUseCase(1)).thenThrow(RuntimeException(errorMessage))

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)
            assertTrue(state.characters.isEmpty())
            assertTrue(state.isEmpty)
        }
    }

    @Test
    fun `pagination error should be handled`() = runTest {
        val firstPageResponse = CharacterResponse(
            info = ResponseInfo(count = 30, pages = 2, next = "page2", prev = null),
            results = listOf(createMockCharacter(1))
        )

        whenever(getCharactersUseCase(1)).thenReturn(firstPageResponse)
        whenever(getCharactersUseCase(2)).thenThrow(RuntimeException("Network error"))

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.LoadNextPage)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            println(state)
            assertFalse(state.isLoadingNextPage)
            assertNotNull(state.paginationError)
        }
    }

    @Test
    fun `retry should clear pagination error and attempt load again`() = runTest {
        val firstPageResponse = CharacterResponse(
            info = ResponseInfo(count = 30, pages = 2, next = "page2", prev = null),
            results = listOf(createMockCharacter(1))
        )
        val secondPageResponse = CharacterResponse(
            info = ResponseInfo(count = 30, pages = 2, next = null, prev = "page1"),
            results = listOf(createMockCharacter(2))
        )

        whenever(getCharactersUseCase(1)).thenReturn(firstPageResponse)
        whenever(getCharactersUseCase(2))
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(secondPageResponse)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.LoadNextPage)
        advanceUntilIdle()

        viewModel.handleIntent(CharacterListIntent.RetryLastPage)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.paginationError)
            assertTrue(state.canLoadNextPage)
            assertEquals(2, state.characters.size)
        }
    }

    @Test
    fun `empty search query should clear search mode`() = runTest {
        val mockCharacters = listOf(createMockCharacter(1))
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = mockCharacters
        )

        whenever(getCharactersUseCase(1)).thenReturn(mockResponse)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.Search("Rick"))

        viewModel.handleIntent(CharacterListIntent.Search(""))

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSearchMode)
            assertEquals("", state.searchQuery)
            assertEquals(state.searchResults, state.characters)
        }
    }

}