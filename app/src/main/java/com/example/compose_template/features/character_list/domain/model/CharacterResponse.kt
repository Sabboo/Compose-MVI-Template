package com.example.compose_template.features.character_list.domain.model

data class CharacterResponse(
    val info: ResponseInfo,
    val results: List<Character>
)

data class ResponseInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)