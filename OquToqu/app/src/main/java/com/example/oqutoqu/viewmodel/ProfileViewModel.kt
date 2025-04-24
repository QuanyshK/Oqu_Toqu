package com.example.oqutoqu.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.LogoutUseCase

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    val userEmail = MutableLiveData<String?>()
    fun loadUser(){
        userEmail.value = getCurrentUserUseCase()
    }
    fun logout(){
        logoutUseCase
    }
}