package com.example.layoutfinal.ui.loops

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface FreesoundApiService {

    @GET("sounds/{id}/")
    suspend fun getSoundDetails(
        @Path("id") soundId: Int,
        @Header("Authorization") authHeader: String
    ): Response<SoundDetails>

    @GET("sounds/search/")
    suspend fun searchSounds(
        @Query("query") query: String,
        @Header("Authorization") authHeader: String,
        s: String
    ): Response<FreesoundResponse>
}

data class Sound(
    val name: String,
    val duration: Double,
    val license: String,
    val previewUrl: String
)


data class FreesoundResponse(
    val results: List<Sound>
)

data class SoundDetails(
    val name: String,
    val duration: Double,
    val license: String,
    val previews: Previews? = null, // make previews nullable
    val download: String? = null // also make download nullable
)

data class Previews(
    val `preview-hq-mp3`: String?, // Ensure exact match with JSON key
    val `preview-lq-mp3`: String?  // Ensure exact match with JSON key
)

