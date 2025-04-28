package com.example.oqutoqu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInWithGoogleUseCase,
) : ViewModel() {

    fun loginWithGoogle(idToken: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = signInUseCase(idToken)
            callback(token != null)
        }
    }
}
