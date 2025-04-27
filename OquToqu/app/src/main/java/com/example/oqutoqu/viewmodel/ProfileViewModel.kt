package com.example.oqutoqu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.LogoutUseCase

class ProfileViewModel(
    private val getEmailUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    fun loadUser() {
        _email.value = getEmailUseCase()
    }
    fun logout() {
        logoutUseCase()
    }
}