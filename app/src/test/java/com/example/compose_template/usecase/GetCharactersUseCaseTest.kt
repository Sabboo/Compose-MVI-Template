package com.example.compose_template.usecase

import com.example.compose_template.createMockCharacter
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.model.ResponseInfo
import com.example.compose_template.features.character_list.domain.repository.CharacterRepository
import com.example.compose_template.features.character_list.domain.usecase.GetCharactersUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class GetCharactersUseCaseTest {

    @Mock
    private lateinit var repository: CharacterRepository

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
        whenever(repository.getCharacters(1)).thenReturn(expectedResponse)

        val result = useCase(1)

        assertEquals(expectedResponse, result)
        verify(repository).getCharacters(1)
    }

    @Test
    fun `should propagate repository exceptions`() = runTest {
        val expectedException = RuntimeException("Network error")
        whenever(repository.getCharacters(1)).thenThrow(expectedException)

        assertThrows<RuntimeException> {
            runBlocking { useCase(1) }
        }
    }

}