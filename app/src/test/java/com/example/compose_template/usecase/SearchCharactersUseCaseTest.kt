package com.example.compose_template.usecase

import com.example.compose_template.createMockCharacter
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import com.example.compose_template.features.character_list.domain.model.ResponseInfo
import com.example.compose_template.features.character_list.domain.repository.CharacterRepository
import com.example.compose_template.features.character_list.domain.usecase.SearchCharactersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SearchCharactersUseCaseTest {

    private val repository: CharacterRepository = mockk(relaxed = true)

    private lateinit var useCase: SearchCharactersUseCase

    @BeforeEach
    fun setup() {
        useCase = SearchCharactersUseCase(repository)
    }

    @Test
    fun `should return search results from repository`() = runTest {
        val query = "Rick"
        val page = 1
        val expectedResponse = CharacterResponse(
            info = ResponseInfo(count = 1, pages = 1, next = null, prev = null),
            results = listOf(createMockCharacter(1, "Rick Sanchez"))
        )
        coEvery { repository.searchCharacters(query, page) } returns expectedResponse

        val result = useCase(query, page)

        assertEquals(expectedResponse, result)
        coVerify { repository.searchCharacters(query, page) }
    }

    @Test
    fun `should handle empty search results`() = runTest {
        val query = "NonExistent"
        val expectedResponse = CharacterResponse(
            info = ResponseInfo(count = 0, pages = 0, next = null, prev = null),
            results = emptyList()
        )
        coEvery { repository.searchCharacters(query, 1) } returns expectedResponse

        val result = useCase(query, 1)

        assertEquals(expectedResponse, result)
        assertTrue(result.results.isEmpty())
    }
}
