package com.example.data.repository

import com.example.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth

class ProfileRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : ProfileRepository {
    override fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email
    override fun logout() = firebaseAuth.signOut()
}
