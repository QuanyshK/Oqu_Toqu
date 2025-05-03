package com.example.oqutoqu.viewmodel

import com.example.domain.usecase.SignInWithGoogleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var useCase: SignInWithGoogleUseCase
    private lateinit var viewModel: AuthViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        useCase = mock()
        viewModel = AuthViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loginWithGoogle returns true when token is not null`() = runTest {
        whenever(useCase.invoke("id")).thenReturn("token")

        var result = false
        viewModel.loginWithGoogle("id") { result = it }

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
    }
}


