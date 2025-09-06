package com.example.compose_template.features.character_list.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CharacterUi(
    val id: Int,
    val name: String,
    val species: String,
    val image: String,
    val status: String
) : Parcelable