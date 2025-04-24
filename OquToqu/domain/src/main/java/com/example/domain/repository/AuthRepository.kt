package com.example.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Boolean
}
