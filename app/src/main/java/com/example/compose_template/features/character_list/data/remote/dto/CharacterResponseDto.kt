package com.example.compose_template.features.character_list.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharacterResponseDto(
    val info: ResponseInfoDto,
    val results: List<CharacterDto>
)

@Serializable
data class ResponseInfoDto(
    val count: Int,
    val pages: Int,
    val next: String? = null,
    val prev: String? = null
)