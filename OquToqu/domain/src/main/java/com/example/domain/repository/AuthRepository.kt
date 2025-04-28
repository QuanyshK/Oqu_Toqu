package com.example.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): String?
    fun getCurrentToken(): String?

//    suspend fun refreshToken(): String?
}
