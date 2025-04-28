package com.example.data.source.remote

import com.example.data.model.ChatResponse
import com.example.data.model.GoogleLoginRequest
import com.example.data.model.GoogleLoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface AuthApi {
    @POST("api/google-login/")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<GoogleLoginResponse>
    @Multipart
    @POST("api/chat/send/")
    suspend fun sendAndReceive(
        @Part("message") message: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): ChatResponse
}