package com.example.compose_template.features.character_list.data.repository

import com.example.compose_template.features.character_list.data.remote.api.CharacterApiService
import com.example.compose_template.features.character_list.domain.repository.CharacterRepository
import com.example.compose_template.features.character_list.data.mapper.toDomain
import com.example.compose_template.features.character_list.domain.model.Character
import com.example.compose_template.features.character_list.domain.model.CharacterResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val apiService: CharacterApiService
) : CharacterRepository {

    override suspend fun getCharacters(page: Int): CharacterResponse {
        return try {
            val response = apiService.getCharacters(page)
            response.toDomain()
        } catch (e: HttpException) {
            throw Exception(getErrorMessage(e))
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    override suspend fun searchCharacters(name: String, page: Int): CharacterResponse {
        return try {
            val response = apiService.searchCharacters(name,page)
            response.toDomain()
        } catch (e: HttpException) {
            throw Exception(getErrorMessage(e))
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    override suspend fun getCharacter(id: Int): Character {
        return try {
            val response = apiService.getCharacter(id)
            response.toDomain()
        } catch (e: HttpException) {
            throw Exception(getErrorMessage(e))
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    private fun getErrorMessage(exception: HttpException): String {
        return when (exception.code()) {
            404 -> "Character not found"
            500 -> "Server error, please try again later"
            else -> "Something went wrong"
        }
    }
}