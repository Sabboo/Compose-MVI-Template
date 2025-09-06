package com.example.compose_template.repository

import com.example.compose_template.features.character_list.data.remote.api.CharacterApiService
import com.example.compose_template.features.character_list.data.remote.dto.CharacterDto
import com.example.compose_template.features.character_list.data.remote.dto.CharacterResponseDto
import com.example.compose_template.features.character_list.data.remote.dto.ResponseInfoDto
import com.example.compose_template.features.character_list.data.repository.CharacterRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@ExtendWith(MockitoExtension::class)
class CharacterRepositoryImplTest {

    @Mock
    private lateinit var apiService: CharacterApiService

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
        whenever(apiService.getCharacters(1)).thenReturn(mockDto)

        // When
        val result = repository.getCharacters(1)

        // Then
        assertEquals(1, result.results.size)
        assertEquals("Rick Sanchez", result.results[0].name)
        assertEquals(1, result.info.count)
        verify(apiService).getCharacters(1)
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
        whenever(apiService.searchCharacters(query, page)).thenReturn(mockDto)

        // When
        val result = repository.searchCharacters(query, page)

        // Then
        assertEquals(1, result.results.size)
        assertEquals("Rick Sanchez", result.results[0].name)
        verify(apiService).searchCharacters(query, page)
    }

    @Test
    fun `getCharacter should return mapped domain model`() = runTest {
        // Given
        val mockDto = createMockCharacterDto(1)
        whenever(apiService.getCharacter(1)).thenReturn(mockDto)

        // When
        val result = repository.getCharacter(1)

        // Then
        assertEquals(1, result.id)
        assertEquals("Rick Sanchez", result.name)
        verify(apiService).getCharacter(1)
    }

    @Test
    fun `should throw proper exception on 404 error`() = runTest {
        val httpException = HttpException(
            Response.error<Any>(404, "Not Found".toResponseBody())
        )
        whenever(apiService.getCharacter(999)).thenThrow(httpException)

        val exception = assertThrows<Exception> {
            runBlocking { repository.getCharacter(999) }
        }
        assertEquals("Character not found", exception.message)
    }

    @Test
    fun `should throw proper exception on 500 error`() = runTest {
        val httpException = HttpException(
            Response.error<Any>(500, "Server Error".toResponseBody())
        )
        whenever(apiService.getCharacters(1)).thenThrow(httpException)

        val exception = assertThrows<Exception> {
            runBlocking { repository.getCharacters(1) }
        }
        assertEquals("Server error, please try again later", exception.message)
    }

    @Test
    fun `should handle generic network exceptions`() = runTest {
        val networkException = IOException("No internet connection")

        whenever(apiService.getCharacters(1)).thenAnswer {
            throw networkException
        }

        val exception = assertThrows<Exception> {
            runBlocking { repository.getCharacters(1) }
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