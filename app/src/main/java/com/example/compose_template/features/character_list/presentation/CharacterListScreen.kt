package com.example.compose_template.features.character_list.presentation

import com.example.compose_template.features.character_list.presentation.components.CharacterListItem
import com.example.compose_template.features.character_list.presentation.components.EmptyState
import com.example.compose_template.features.character_list.presentation.components.ErrorState
import com.example.compose_template.features.character_list.presentation.components.PageErrorItem
import com.example.compose_template.features.character_list.presentation.components.SearchBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose_template.features.character_list.presentation.model.CharacterUi
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    onCharacterClick: (CharacterUi) -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val currentList = if (uiState.isSearchMode) uiState.searchResults else uiState.characters
    val hasNextPage = if (uiState.isSearchMode) uiState.hasNextSearchPage else uiState.hasNextPage

    Scaffold(
        topBar = { TopAppBar(title = { Text("Characters") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CharacterSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.handleIntent(CharacterListIntent.Search(it)) },
                onClearClick = { viewModel.handleIntent(CharacterListIntent.ClearSearch) },
                isSearching = uiState.isSearching
            )

            CharacterListContent(
                uiState = uiState,
                characters = currentList,
                hasNextPage = hasNextPage,
                listState = listState,
                onCharacterClick = onCharacterClick,
                onRetryPage = { viewModel.handleIntent(CharacterListIntent.RetryLastPage) },
                onLoadNextPage = { viewModel.handleIntent(CharacterListIntent.LoadNextPage) }
            )
        }
    }

    LaunchedEffect(listState, uiState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems =
                    if (uiState.isSearchMode) uiState.searchResults.size else uiState.characters.size
                val hasNext =
                    if (uiState.isSearchMode) uiState.hasNextSearchPage else uiState.hasNextPage

                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= totalItems - 3 &&
                    hasNext &&
                    !uiState.isLoadingNextPage &&
                    uiState.canLoadNextPage
                ) {
                    viewModel.handleIntent(CharacterListIntent.LoadNextPage)
                }
            }
    }
}

@Composable
private fun CharacterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isSearching: Boolean
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onClearClick = onClearClick,
        isSearching = isSearching,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun CharacterListContent(
    uiState: CharacterListUiState,
    characters: List<CharacterUi>,
    hasNextPage: Boolean,
    listState: LazyListState,
    onCharacterClick: (CharacterUi) -> Unit,
    onRetryPage: () -> Unit,
    onLoadNextPage: () -> Unit
) {
    when {
        uiState.isLoading -> LoadingState()
        uiState.error != null && characters.isEmpty() -> ErrorState(
            message = uiState.error,
            onRetry = { onLoadNextPage() }
        )

        uiState.isEmpty -> EmptyState(
            message = if (uiState.isSearchMode) "No characters found for \"${uiState.searchQuery}\""
            else "No characters available"
        )

        else -> CharacterList(
            characters = characters,
            listState = listState,
            hasNextPage = hasNextPage,
            isLoadingNextPage = uiState.isLoadingNextPage,
            pageError = uiState.paginationError,
            onCharacterClick = onCharacterClick,
            onRetryPage = onRetryPage
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun CharacterList(
    characters: List<CharacterUi>,
    listState: LazyListState,
    hasNextPage: Boolean,
    isLoadingNextPage: Boolean,
    pageError: String?,
    onCharacterClick: (CharacterUi) -> Unit,
    onRetryPage: () -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(characters, key = { it.id }) { character ->
            CharacterListItem(character = character, onClick = { onCharacterClick(character) })
        }

        if (hasNextPage) {
            item {
                when {
                    isLoadingNextPage -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    pageError != null -> PageErrorItem(error = pageError, onRetry = onRetryPage)
                }
            }
        }
    }
}