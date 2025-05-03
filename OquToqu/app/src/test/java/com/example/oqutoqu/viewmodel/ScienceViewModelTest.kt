package com.example.oqutoqu.viewmodel

import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ScienceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: SciHubRepository
    private lateinit var viewModel: ScienceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mock()
        viewModel = ScienceViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchPdf should update sciHubResult StateFlow on success`() = runTest {
        val result = SciHubResult("url", "pdf")
        whenever(repo.fetchPdf("10.1234/test")).thenReturn(result)

        viewModel.fetchPdf("10.1234/test")
        testScheduler.advanceUntilIdle()

        assertEquals(result, viewModel.sciHubResult.value)
    }

    @Test
    fun `clearResult should reset sciHubResult and cached values`() = runTest {
        val result = SciHubResult("url", "pdf")
        whenever(repo.fetchPdf("10.1234/test")).thenReturn(result)

        viewModel.fetchPdf("10.1234/test")
        testScheduler.advanceUntilIdle()

        viewModel.clearResult()

        assertEquals(null, viewModel.sciHubResult.value)
        assertEquals(null, viewModel.lastDoi)
        assertEquals(null, viewModel.lastPdfUrl)
    }
}
