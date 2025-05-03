package com.example.data.repository

import com.example.domain.model.SciHubResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SciHubRepositoryImplTest {

    private lateinit var repository: SciHubRepositoryImpl

    @Test
    fun `fetchPdf should return non-null result for dummy DOI`() = runTest {
        repository = SciHubRepositoryImpl()

        val result = repository.fetchPdf("10.1038/nphys1170")

        assertNotNull(result)
        result!!.pdfLink?.let { assertTrue(it.contains("pdf")) }
    }
}
