package com.example.domain.usecase
import com.example.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Boolean =
        repo.signInWithGoogle(idToken)
}