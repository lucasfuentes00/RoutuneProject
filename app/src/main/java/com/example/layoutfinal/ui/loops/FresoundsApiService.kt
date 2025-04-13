package com.example.layoutfinal.ui.loops

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FreesoundApiService {

    @GET("sounds/search/")
    suspend fun searchSounds(
        @Query("query") query: String,
        @Header("Authorization") apiKey: String,
        s: String
    ): FreesoundResponse
}

data class FreesoundResponse(
    val results: List<Sound>
)

data class Sound(
    val name: String,
    val url: String
)
