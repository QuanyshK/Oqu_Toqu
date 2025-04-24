package com.example.oqutoqu.di

import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.ProfileRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.ProfileRepository
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.oqutoqu.viewmodel.AuthViewModel
import com.example.oqutoqu.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { FirebaseAuth.getInstance() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get()) }
}

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    viewModel { ProfileViewModel(get(), get()) }
}
