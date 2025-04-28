package com.example.data.source.remote

import com.example.data.manager.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authManager: AuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authManager.getToken()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
