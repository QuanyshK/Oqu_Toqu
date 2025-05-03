package com.example.domain.usecase

import com.example.domain.repository.ProfileRepository

class LogoutUseCaseTest(private val repo: ProfileRepository) {
    operator fun invoke() = repo.logout()
}
