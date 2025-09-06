package com.example.compose_template.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose_template.features.character_details.CharacterDetailScreen
import com.example.compose_template.features.character_list.presentation.CharacterListScreen
import com.example.compose_template.features.character_list.presentation.model.CharacterUi

@ExperimentalMaterial3Api
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CharacterList
    ) {
        composable(Routes.CharacterList) {
            CharacterListScreen(
                onCharacterClick = { character ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "character",
                        character
                    )
                    navController.navigate(Routes.CharacterDetail)
                }
            )
        }

        composable(Routes.CharacterDetail) {
            val character =
                navController.previousBackStackEntry?.savedStateHandle?.get<CharacterUi>("character")

            if (character != null) {
                CharacterDetailScreen(
                    character = character,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}