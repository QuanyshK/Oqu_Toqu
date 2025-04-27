package com.example.oqutoqu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.SignInWithGoogleUseCase
import com.example.domain.usecase.LogoutUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInWithGoogleUseCase,
) : ViewModel() {

    fun loginWithGoogle(idToken: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = signInUseCase(idToken)
            callback(ok)
        }
    }
}