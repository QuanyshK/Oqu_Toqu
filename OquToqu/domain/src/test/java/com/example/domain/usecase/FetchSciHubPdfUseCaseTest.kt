package com.example.domain.usecase

import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals

class FetchSciHubPdfUseCaseTest {

    private lateinit var repository: SciHubRepository
    private lateinit var useCase: com.example.domain.usecase.FetchSciHubPdfUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = com.example.domain.usecase.FetchSciHubPdfUseCase(repository)
    }

    @Test
    fun `invoke should return result from repository`() = runTest {
        val expected = SciHubResult("url", "title")
        whenever(repository.fetchPdf("doi")).thenReturn(expected)

        val result = useCase("doi")

        assertEquals(expected, result)
    }
}
