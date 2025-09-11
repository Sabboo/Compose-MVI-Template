package com.example.compose_template.repository

import com.example.compose_template.features.character_list.data.remote.api.CharacterApiService
import com.example.compose_template.features.character_list.data.remote.dto.CharacterDto
import com.example.compose_template.features.character_list.data.remote.dto.CharacterResponseDto
import com.example.compose_template.features.character_list.data.remote.dto.ResponseInfoDto
import com.example.compose_template.features.character_list.data.repository.CharacterRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CharacterRepositoryImplTest {

    private val apiService: CharacterApiService = mockk(relaxed = true)

    private lateinit var repository: CharacterRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = CharacterRepositoryImpl(apiService)
    }

    @Test
    fun `getCharacters should return mapped domain models`() = runTest {
        // Given
        val mockDto = CharacterResponseDto(
            info = ResponseInfoDto(count = 1, pages = 1),
            results = listOf(createMockCharacterDto(1))
        )
        coEvery { apiService.getCharacters(1) } returns mockDto

        // When
        val result = repository.getCharacters(1)

        // Then
        assertEquals(1, result.results.size)
        assertEquals("Rick Sanchez", result.results[0].name)
        assertEquals(1, result.info.count)
        coVerify { apiService.getCharacters(1) }
    }

    @Test
    fun `searchCharacters should return mapped domain models`() = runTest {
        // Given
        val query = "Rick"
        val page = 1
        val mockDto = CharacterResponseDto(
            info = ResponseInfoDto(count = 1, pages = 1),
            results = listOf(createMockCharacterDto(1))
        )
        coEvery { apiService.searchCharacters(query, page) } returns mockDto

        // When
        val result = repository.searchCharacters(query, page)

        // Then
        assertEquals(1, result.results.size)
        assertEquals("Rick Sanchez", result.results[0].name)
        coVerify { apiService.searchCharacters(query, page) }
    }

    @Test
    fun `getCharacter should return mapped domain model`() = runTest {
        // Given
        val mockDto = createMockCharacterDto(1)
        coEvery { apiService.getCharacter(1) } returns mockDto

        // When
        val result = repository.getCharacter(1)

        // Then
        assertEquals(1, result.id)
        assertEquals("Rick Sanchez", result.name)
        coVerify { apiService.getCharacter(1) }
    }

    @Test
    fun `should throw proper exception on 404 error`() = runTest {
        val httpException = HttpException(
            Response.error<Any>(404, "Not Found".toResponseBody())
        )
        coEvery { apiService.getCharacter(999) } throws httpException

        val exception = assertThrows<Exception> {
            repository.getCharacter(999)
        }
        assertEquals("Character not found", exception.message)
    }

    @Test
    fun `should throw proper exception on 500 error`() = runTest {
        val httpException = HttpException(
            Response.error<Any>(500, "Server Error".toResponseBody())
        )
        coEvery { apiService.getCharacters(1) } throws httpException

        val exception = assertThrows<Exception> {
            repository.getCharacters(1)
        }
        assertEquals("Server error, please try again later", exception.message)
    }

    @Test
    fun `should handle generic network exceptions`() = runTest {
        val networkException = IOException("No internet connection")

        coEvery { apiService.getCharacters(1) } throws networkException

        val exception = assertThrows<Exception> {
            repository.getCharacters(1)
        }
        assertEquals("Network error: No internet connection", exception.message)
    }

    private fun createMockCharacterDto(id: Int): CharacterDto {
        return CharacterDto(
            id = id,
            name = "Rick Sanchez",
            species = "Human",
            image = "image_url",
            status = "Alive"
        )
    }
}
