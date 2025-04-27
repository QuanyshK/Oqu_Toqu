package com.example.data.source.remote

import com.example.data.model.GoogleLoginRequest
import com.example.data.model.GoogleLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApi {
    @POST("api/google-login/")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<GoogleLoginResponse>
}