package com.example.layoutfinal.ui.loops

import com.google.gson.annotations.SerializedName
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

    @GET("search/text/")
    suspend fun searchSounds(
        @Query("query") query: String,
        @Header("Authorization") authHeader: String
    ): Response<SearchResponse>
}

data class Sound(
    val name: String,
    val duration: Double,
    val username: String,
    val previewUrl: String
)


data class SoundDetails(
    val name: String,
    val duration: Double,
    val username: String,
    val previews: Previews? = null,
)

data class Previews(
    val `preview-hq-mp3`: String?,
    val `preview-lq-mp3`: String?
)

data class SearchResponse(
    val results: List<SoundResult>
)

data class SoundResult(
    val id: Int,
    val name: String,
    val duration: Double,
    val username: String,
    val previews: Previews? = null
)

data class PreviewUrls(
    @SerializedName("preview-hq-mp3") val previewHqMp3: String
)

