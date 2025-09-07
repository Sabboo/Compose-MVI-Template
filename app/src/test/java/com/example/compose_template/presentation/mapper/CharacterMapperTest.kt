package com.example.compose_template.presentation.mapper

import com.example.compose_template.createMockCharacter
import com.example.compose_template.features.character_list.presentation.mapper.toUi
import com.example.compose_template.features.character_list.presentation.model.CharacterUi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CharacterMapperTest {

    @Test
    fun `should map Character to CharacterUi correctly`() {
        // Given
        val character = createMockCharacter(
            id = 1,
            name = "Rick Sanchez",
            species = "Human",
            url = "image_url",
            status = "Alive"
        )

        // When
        val characterUi = character.toUi()

        // Then
        assertThat(characterUi).isEqualTo(
            CharacterUi(
                id = 1,
                name = "Rick Sanchez",
                species = "Human",
                image = "image_url",
                status = "Alive"
            )
        )
    }

    @Test
    fun `should map Character with missing species`() {
        // Given
        val character = createMockCharacter(
            id = 2,
            name = "Morty Smith",
            species = "",
            url = "image_url_2",
            status = "Alive"
        )

        // When
        val characterUi = character.toUi()

        // Then
        assertThat(characterUi.species).isEmpty()
    }
}