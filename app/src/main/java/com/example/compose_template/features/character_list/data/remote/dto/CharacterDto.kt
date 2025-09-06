package com.example.compose_template.features.character_list.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val species: String,
    val image: String
)