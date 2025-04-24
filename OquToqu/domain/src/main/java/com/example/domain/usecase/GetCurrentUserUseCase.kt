package com.example.domain.usecase

import com.example.domain.repository.ProfileRepository

class GetCurrentUserUseCase(private val repo: ProfileRepository) {
    operator fun invoke(): String? = repo.getCurrentUserEmail()
}