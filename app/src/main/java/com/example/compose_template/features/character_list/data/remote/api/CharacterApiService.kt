package com.example.compose_template.features.character_list.data.remote.api

import com.example.compose_template.features.character_list.data.remote.dto.CharacterDto
import com.example.compose_template.features.character_list.data.remote.dto.CharacterResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CharacterApiService {

    @GET("character/")
    suspend fun getCharacters(
        @Query("page") page: Int
    ): CharacterResponseDto

    @GET("character/")
    suspend fun searchCharacters(
        @Query("name") name: String,
        @Query("page") page: Int,
    ): CharacterResponseDto

    @GET("character/{id}")
    suspend fun getCharacter(
        @Path("id") id: Int
    ): CharacterDto
}
