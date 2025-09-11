package com.example.compose_template

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.compose_template.features.character_list.presentation.CharacterListScreen
import com.example.compose_template.features.character_list.presentation.CharacterListUiState
import com.example.compose_template.features.character_list.presentation.TestTags
import com.example.compose_template.features.character_list.presentation.TestTags.CHARACTERS_LIST
import com.example.compose_template.features.character_list.presentation.TestTags.LOADING_INDICATOR
import com.example.compose_template.features.character_list.presentation.TestTags.characterListItem
import com.example.compose_template.features.character_list.presentation.model.CharacterUi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

@SuppressLint("ViewModelConstructorInComposable")
@HiltAndroidTest
class CharacterListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private val sampleCharacters = listOf(
        CharacterUi(1, "Rick Sanchez", "Alive", "Human", "image_url"),
        CharacterUi(2, "Morty Smith", "Alive", "Human", "image_url")
    )

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun initialLoadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            val viewModel = FakeCharacterListViewModel(
                CharacterListUiState(isLoading = true)
            )

            CharacterListScreen(
                onCharacterClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(LOADING_INDICATOR).assertIsDisplayed()
    }

    @Test
    fun errorOccurredShowsErrorWidget() {
        composeTestRule.setContent {
            val viewModel = FakeCharacterListViewModel(
                CharacterListUiState()
            )

            viewModel.setUiState(CharacterListUiState(error = "Failed to load initial data"))

            CharacterListScreen(
                onCharacterClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(TestTags.ERROR_WIDGET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ERROR_WIDGET_TEXT)
            .assertTextEquals("Failed to load initial data")
    }

    @Test
    fun emptyResultsShowsEmptyWidget() {
        composeTestRule.setContent {
            val viewModel = FakeCharacterListViewModel(
                CharacterListUiState()
            )

            viewModel.setUiState(CharacterListUiState(isEmpty = true))

            CharacterListScreen(
                onCharacterClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(TestTags.EMPTY_WIDGET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.EMPTY_WIDGET_TEXT)
            .assertTextEquals("No characters available")
    }

    @Test
    fun resultsShowsInList() {
        composeTestRule.setContent {
            val viewModel = FakeCharacterListViewModel(
                CharacterListUiState()
            )

            viewModel.setUiState(CharacterListUiState(characters = sampleCharacters))

            CharacterListScreen(
                onCharacterClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(CHARACTERS_LIST).assertIsDisplayed()
    }

    @Test
    fun charactersAppearProperlyInListWhenLoaded() {
        composeTestRule.setContent {
            val viewModel = FakeCharacterListViewModel(
                CharacterListUiState()
            )

            viewModel.setUiState(CharacterListUiState(characters = sampleCharacters))

            CharacterListScreen(
                onCharacterClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(characterListItem(1)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(characterListItem(2)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(characterListItem(3)).assertIsNotDisplayed()
    }

}
