package com.example.oqutoqu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    fun loginWithGoogle(idToken: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repo.signInWithGoogle(idToken)
            callback(result)
        }
    }
}
