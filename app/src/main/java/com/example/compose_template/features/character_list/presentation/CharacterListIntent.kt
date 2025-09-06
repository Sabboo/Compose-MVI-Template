package com.example.compose_template.features.character_list.presentation

sealed class CharacterListIntent {
    object LoadInitial : CharacterListIntent()
    object LoadNextPage : CharacterListIntent()
    object RetryLastPage : CharacterListIntent()
    data class Search(val query: String) : CharacterListIntent()
    object ClearSearch : CharacterListIntent()
}