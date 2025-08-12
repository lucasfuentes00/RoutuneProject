package com.example.layoutfinal.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class FreesoundResponse(val results: List<Sound>)
data class Sound(val name: String, val previews: Previews)
data class Previews(val preview_lq_mp3: String)

interface FreesoundApi {
    @GET("search/text/")
    fun searchSounds(
        @Query("query") query: String,
        @Query("token") token: String,
        @Query("page_size") pageSize: Int = 1,
        @Query("page") page: Int = (1..20).random()
    ): Call<FreesoundResponse>
}


