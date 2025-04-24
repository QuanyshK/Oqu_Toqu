package com.example.domain.usecase

import com.example.domain.repository.ProfileRepository

class LogoutUseCase(private val repo: ProfileRepository) {
    operator fun invoke() = repo.logout()
}