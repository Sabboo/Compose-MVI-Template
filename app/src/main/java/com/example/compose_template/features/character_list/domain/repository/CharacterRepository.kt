package com.example.compose_template.features.character_list.domain.repository

import com.example.compose_template.features.character_list.domain.model.Character
import com.example.compose_template.features.character_list.domain.model.CharacterResponse

interface CharacterRepository {
    suspend fun getCharacters(page: Int): CharacterResponse
    suspend fun searchCharacters(name: String, page: Int): CharacterResponse
    suspend fun getCharacter(id: Int): Character
}