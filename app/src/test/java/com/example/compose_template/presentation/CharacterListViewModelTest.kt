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
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

@ExperimentalCoroutinesApi
class CharacterListViewModelTest {

    private lateinit var getCharactersUseCase: GetCharactersUseCase
    private lateinit var searchCharactersUseCase: SearchCharactersUseCase
    private lateinit var viewModel: CharacterListViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCharactersUseCase = mockk(relaxed = true)
        searchCharactersUseCase = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial load should fetch first page of characters`() = runTest {
        val mockCharacters = listOf(createMockCharacter(1, "Rick"))
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 2, pages = 1, next = null, prev = null),
            results = mockCharacters
        )

        coEvery { getCharactersUseCase(1) } returns mockResponse

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(mockResponse.results.size, state.characters.size)
            assertEquals("Rick", state.characters[0].name)
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

        coEvery { getCharactersUseCase(1) } returns firstPageResponse
        coEvery { getCharactersUseCase(2) } returns secondPageResponse

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.LoadNextPage)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.characters.size)
            assertEquals("Rick", state.characters[0].name)
            assertEquals("Morty", state.characters[1].name)
            assertEquals(2, state.currentPage)
            assertFalse(state.hasNextPage)
            assertFalse(state.isLoadingNextPage)
        }
    }

    @Test
    fun `search should perform client-side filtering immediately`() = runTest {
        val mockCharacters = listOf(
            createMockCharacter(1, "Rick Sanchez"),
            createMockCharacter(2, "Morty Smith")
        )
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 2, pages = 1, next = null, prev = null),
            results = mockCharacters
        )

        coEvery { getCharactersUseCase(1) } returns mockResponse
        coEvery { searchCharactersUseCase("Rick", 1) } returns CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = emptyList()
        )

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.Search("Rick"))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.searchResults.isNotEmpty())
            assertThat(state.searchResults.size == 2)
        }
    }

    @Test
    fun `state with search mode should reflect search properties`() {
        val searchResults = listOf(createMockCharacterUI(id = 1, name = "Rick"))
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

        coEvery { getCharactersUseCase(1) } returns initialResponse
        coEvery { searchCharactersUseCase("Rick", 1) } returns searchResponse

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
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

        coEvery { getCharactersUseCase(1) } returns CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = emptyList()
        )
        coEvery { searchCharactersUseCase("Rick", 1) } returns initialSearchResponse
        coEvery { searchCharactersUseCase("Rick", 2) } returns secondPageResponse

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.Search("Rick"))
        advanceTimeBy(400)

        viewModel.handleIntent(CharacterListIntent.LoadNextPage)

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

        coEvery { getCharactersUseCase(1) } returns mockResponse
        coEvery { searchCharactersUseCase("Rick", 1) } returns CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = listOf(createMockCharacter(1))
        )

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.Search("Rick"))
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
        coEvery { getCharactersUseCase(1) } throws RuntimeException(errorMessage)

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)

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

        coEvery { getCharactersUseCase(1) } returns firstPageResponse
        coEvery { getCharactersUseCase(2) } throws RuntimeException("Network error")

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.LoadNextPage)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoadingNextPage)
            assertNotNull(state.paginationError)
            assertFalse(state.canLoadNextPage)
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

        coEvery { getCharactersUseCase(1) } returns firstPageResponse
        coEvery { getCharactersUseCase(2) } throws RuntimeException("Network error") andThen secondPageResponse

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.LoadNextPage)
        viewModel.handleIntent(CharacterListIntent.RetryLastPage)

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.paginationError)
            assertTrue(state.canLoadNextPage)
            assertEquals(2, state.characters.size)
        }
    }

    @Test
    fun `should not load next page when already loading`() = runTest {
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 30, pages = 2, next = "page2", prev = null),
            results = listOf(createMockCharacter(1))
        )

        coEvery { getCharactersUseCase(1) } returns mockResponse
        coEvery { getCharactersUseCase(2) } coAnswers {
            delay(1000)
            CharacterResponse(
                info = ResponseInfo(count = 30, pages = 2, next = null, prev = "page1"),
                results = listOf(createMockCharacter(2))
            )
        }

        viewModel = CharacterListViewModel(getCharactersUseCase, searchCharactersUseCase)
        viewModel.handleIntent(CharacterListIntent.LoadNextPage)
        viewModel.handleIntent(CharacterListIntent.LoadNextPage)

        coVerify(exactly = 1) { getCharactersUseCase(2) }
    }

    @Test
    fun `empty search query should clear search mode`() = runTest {
        val mockCharacters = listOf(createMockCharacter(1))
        val mockResponse = CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = mockCharacters
        )

        coEvery { getCharactersUseCase(1) } returns mockResponse

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
