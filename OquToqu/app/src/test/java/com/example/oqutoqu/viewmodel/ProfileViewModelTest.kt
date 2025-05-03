package com.example.oqutoqu.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.LogoutUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var getEmail: GetCurrentUserUseCase
    private lateinit var logout: LogoutUseCase
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        getEmail = mock()
        logout = mock()
        viewModel = ProfileViewModel(getEmail, logout)
    }

    @Test
    fun `loadUser should update email`() {
        whenever(getEmail()).thenReturn("test@example.com")
        viewModel.loadUser()
        assertEquals("test@example.com", viewModel.email.value)
    }
}

