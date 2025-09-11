package com.example.compose_template.features.character_list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compose_template.BaseViewModel
import com.example.compose_template.features.character_list.domain.usecase.GetCharactersUseCase
import com.example.compose_template.features.character_list.domain.usecase.SearchCharactersUseCase
import com.example.compose_template.features.character_list.presentation.mapper.toUi
import com.example.compose_template.features.character_list.presentation.model.CharacterUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter

@HiltViewModel
open class CharacterListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val searchCharactersUseCase: SearchCharactersUseCase
) : ViewModel(), BaseViewModel<CharacterListUiState, CharacterListIntent> {

    private val _uiState = MutableStateFlow(CharacterListUiState())
    override val uiState: StateFlow<CharacterListUiState> = _uiState.asStateFlow()

    private val searchDebouncer = MutableSharedFlow<String>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var cachedCharacters: List<CharacterUi> = emptyList()
    private var searchJob: Job? = null

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val RETRY_COOLDOWN_MS = 3000L
    }

    init {
        loadInitialData()
        setupSearchDebouncer()
    }

    override fun handleIntent(intent: CharacterListIntent) {
        when (intent) {
            is CharacterListIntent.LoadInitial -> loadInitialData()
            is CharacterListIntent.LoadNextPage -> loadNextPage()
            is CharacterListIntent.RetryLastPage -> retryLastPage()
            is CharacterListIntent.Search -> handleSearch(intent.query)
            is CharacterListIntent.ClearSearch -> clearSearch()
        }
    }

    private fun loadInitialData() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            try {
                val result = getCharactersUseCase(page = 1)
                val characters = result.results.map { it.toUi() }
                cachedCharacters = characters

                updateState {
                    copy(
                        characters = characters,
                        isLoading = false,
                        hasNextPage = result.info.next != null,
                        currentPage = 1,
                        isEmpty = characters.isEmpty(),
                        error = null,
                        canLoadNextPage = true
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred",
                        isEmpty = true
                    )
                }
            }
        }
    }

    private fun handleSearch(query: String) {
        val trimmedQuery = query.trim()
        updateState { copy(searchQuery = trimmedQuery) }

        val filtered = cachedCharacters.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
        updateState {
            copy(
                searchResults = filtered,
                isSearchMode = trimmedQuery.isNotEmpty(),
                isEmpty = filtered.isEmpty(),
                hasNextSearchPage = false,
                currentSearchPage = 1,
                isSearching = false
            )
        }

        if (trimmedQuery.isNotEmpty()) searchDebouncer.tryEmit(trimmedQuery)
    }

    private fun clearSearch() {
        updateState {
            copy(
                searchQuery = "",
                isSearchMode = false,
                isSearching = false,
                searchResults = emptyList(),
                isEmpty = cachedCharacters.isEmpty(),
                currentSearchPage = 1,
                hasNextSearchPage = true,
                paginationError = null
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebouncer() {
        searchJob = viewModelScope.launch {
            searchDebouncer
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) performServerSearch(query)
                }
        }
    }

    private suspend fun performServerSearch(query: String) {
        updateState { copy(isSearching = true) }

        try {
            val result = searchCharactersUseCase(query, page = 1)
            val searchResults = result.results.map { it.toUi() }

            updateState {
                copy(
                    searchResults = searchResults,
                    isSearchMode = true,
                    hasNextSearchPage = result.info.next != null,
                    isSearching = false,
                    isEmpty = searchResults.isEmpty(),
                    currentSearchPage = result.info.next?.toIntOrNull() ?: 1
                )
            }
        } catch (_: Exception) {
            updateState { copy(isSearching = false) }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingNextPage || !state.canLoadNextPage) return

        val strategy: PaginationStrategy = if (state.isSearchMode) {
            SearchPaginationStrategy(searchCharactersUseCase, state.searchQuery)
        } else {
            NormalPaginationStrategy(getCharactersUseCase)
        }

        if (!strategy.canLoad(state)) return

        viewModelScope.launch {
            updateState { copy(isLoadingNextPage = true, paginationError = null) }

            try {
                val page = strategy.nextPage(state)
                val result = strategy.fetch(page)
                updateState { strategy.updateState(this, page, result) }
            } catch (e: Exception) {
                handlePaginationError(e)
            }
        }
    }

    private fun handlePaginationError(exception: Exception) {
        updateState {
            copy(
                isLoadingNextPage = false,
                paginationError = exception.message ?: "Failed to load more characters"
            )
        }
        viewModelScope.launch {
            updateState { copy(canLoadNextPage = false) }
            delay(RETRY_COOLDOWN_MS)
            updateState { copy(canLoadNextPage = true) }
        }
    }

    private fun retryLastPage() {
        updateState { copy(paginationError = null, canLoadNextPage = true) }
        loadNextPage()
    }

    private fun updateState(update: CharacterListUiState.() -> CharacterListUiState) {
        _uiState.update(update)
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
