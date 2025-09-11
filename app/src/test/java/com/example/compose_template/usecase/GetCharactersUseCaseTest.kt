package com.example.compose_template.usecase

import com.example.compose_template.createMockCharacter
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.model.ResponseInfo
import com.example.compose_template.features.character_list.domain.repository.CharacterRepository
import com.example.compose_template.features.character_list.domain.usecase.GetCharactersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetCharactersUseCaseTest {

    private val repository: CharacterRepository = mockk(relaxed = true)

    private lateinit var useCase: GetCharactersUseCase

    @BeforeEach
    fun setup() {
        useCase = GetCharactersUseCase(repository)
    }

    @Test
    fun `should return character response from repository`() = runTest {
        val expectedResponse = CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = listOf(createMockCharacter(1))
        )
        coEvery { repository.getCharacters(1) } returns expectedResponse

        val result = useCase(1)

        assertThat(result).isEqualTo(expectedResponse)
        coVerify { repository.getCharacters(1) }
    }

    @Test
    fun `should propagate repository exceptions`() = runTest {
        val expectedException = RuntimeException("Network error")
        coEvery { repository.getCharacters(1) } throws expectedException

        assertThrows<RuntimeException> {
            useCase(1)
        }
    }
}
