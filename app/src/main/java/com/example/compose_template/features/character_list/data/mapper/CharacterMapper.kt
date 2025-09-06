package com.example.compose_template.features.character_list.data.mapper

import com.example.compose_template.features.character_list.data.remote.dto.CharacterDto
import com.example.compose_template.features.character_list.data.remote.dto.CharacterResponseDto
import com.example.compose_template.features.character_list.data.remote.dto.ResponseInfoDto
import com.example.compose_template.features.character_list.domain.model.Character
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.model.ResponseInfo

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    species = species,
    image = image,
    status = status
)

fun CharacterResponseDto.toDomain(): CharacterResponse = CharacterResponse(
    info = info.toDomain(),
    results = results.map { it.toDomain() }
)

fun ResponseInfoDto.toDomain(): ResponseInfo = ResponseInfo(
    count = count,
    pages = pages,
    next = next,
    prev = prev
)
