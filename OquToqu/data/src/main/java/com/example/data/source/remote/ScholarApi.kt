package com.example.data.source.remote

import com.example.data.model.ScholarResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ScholarApi {
    @GET("search.json?engine=google_scholar")
    suspend fun search(
        @Query("q") query: String,
        @Query("start") start: Int = 0,
        @Query("api_key") apiKey: String = "fc924b9a494aa2286742ce1aecffa41f111ce5fb633b54f1a282162d53ff2dbd"
    ): retrofit2.Response<ScholarResponse>
}