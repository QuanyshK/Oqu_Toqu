package com.example.domain.repository

interface ProfileRepository {
    fun getCurrentUserEmail(): String?
    fun logout()
}