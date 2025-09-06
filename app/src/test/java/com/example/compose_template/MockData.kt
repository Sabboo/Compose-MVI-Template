package com.example.compose_template

import com.example.compose_template.features.character_list.domain.model.Character
import com.example.compose_template.features.character_list.presentation.model.CharacterUi

fun createMockCharacter(
    id: Int,
    name: String = "Name",
    species: String = "Species",
    url: String = "url",
    status: String = "Alive"
): Character {
    return Character(
        id = id,
        name = name,
        species = species,
        image = url,
        status = status
    )
}

fun createMockCharacterUI(
    id: Int,
    name: String? = "Name",
    species: String? = "Species",
    url: String? = "url",
    status: String? = "Alive"
): CharacterUi {
    return CharacterUi(
        id = id,
        name = name!!,
        species = species!!,
        image = url!!,
        status = status!!
    )
}