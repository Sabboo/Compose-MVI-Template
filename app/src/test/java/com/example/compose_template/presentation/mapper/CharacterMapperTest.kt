package com.example.compose_template.presentation.mapper

import com.example.compose_template.createMockCharacter
import com.example.compose_template.features.character_list.presentation.mapper.toUi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CharacterMapperTest {

    @Test
    fun `should map Character to CharacterUi correctly`() {
        // Given
        val character = createMockCharacter(
            1,
            "Rick Sanchez",
            "Human",
            "image_url",
            "Alive"
        )

        // When
        val characterUi = character.toUi()

        // Then
        assertEquals(1, characterUi.id)
        assertEquals("Rick Sanchez", characterUi.name)
        assertEquals("Human", characterUi.species)
        assertEquals("image_url", characterUi.image)
        assertEquals("Alive", characterUi.status)
    }
}