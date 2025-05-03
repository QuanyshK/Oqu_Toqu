package com.example.oqutoqu.viewmodel

import com.example.domain.model.ScholarItem
import com.example.domain.repository.ScholarRepository
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
class ScholarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: ScholarRepository
    private lateinit var viewModel: ScholarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mock()
        viewModel = ScholarViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search should populate items`() = runTest {
        val result = listOf(ScholarItem("Test", "link", "desc", "author", "doi"))
        whenever(repo.searchScholar("query", 0)).thenReturn(result)

        viewModel.search("query")
        testScheduler.advanceUntilIdle()

        assertEquals(result, viewModel.items.value)
    }
}
