package com.example.data.repository

import com.example.data.manager.AuthManager
import com.example.data.model.GoogleLoginRequest
import com.example.data.source.remote.AuthApi
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val authApi: AuthApi,
    private val authManager: AuthManager
) : AuthRepository {

    override suspend fun signInWithGoogle(googleIdToken: String): String? {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val firebaseIdToken = authResult.user?.getIdToken(false)?.await()?.token ?: return null

            val response = authApi.googleLogin(GoogleLoginRequest(idToken = firebaseIdToken))
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (!token.isNullOrBlank()) {
                    authManager.saveToken(token)
                    println("Token backend: $token")
                    token
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getCurrentToken(): String? {
        return authManager.getToken()
    }
}
