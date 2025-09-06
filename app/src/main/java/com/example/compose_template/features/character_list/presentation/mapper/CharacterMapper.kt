package com.example.compose_template.features.character_list.presentation.mapper

import com.example.compose_template.features.character_list.presentation.model.CharacterUi
import com.example.compose_template.features.character_list.domain.model.Character

fun Character.toUi(): CharacterUi = CharacterUi(
    id = id,
    name = name,
    species = species,
    image = image,
    status = status
)