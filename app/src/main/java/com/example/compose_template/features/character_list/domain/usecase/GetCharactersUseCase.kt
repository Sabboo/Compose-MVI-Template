package com.example.compose_template.features.character_list.domain.usecase

import com.example.compose_template.features.character_list.domain.repository.CharacterRepository
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(page: Int): CharacterResponse {
        return repository.getCharacters(page)
    }
}