package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String) = repository.signInWithGoogle(idToken)
}