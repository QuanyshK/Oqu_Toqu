package com.example.data.repository

import com.example.data.model.GoogleLoginRequest
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.source.remote.AuthApi
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val authApi: AuthApi
) : AuthRepository {

    /**
     * Принимает Google ID Token из GoogleSignInAccount.getIdToken(),
     * логинит в Firebase, затем получает от Firebase свой ID Token
     * и шлёт его на backend (/api/google-login/).
     */
    override suspend fun signInWithGoogle(googleIdToken: String): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)

            val authResult = firebaseAuth
                .signInWithCredential(credential)
                .await()

            val firebaseIdToken = authResult.user
                ?.getIdToken(false)
                ?.await()
                ?.token
                ?: return false

            val response = authApi.googleLogin(
                GoogleLoginRequest(idToken = firebaseIdToken)
            )

            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
