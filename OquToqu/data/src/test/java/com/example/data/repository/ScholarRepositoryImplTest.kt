package com.example.data.repository

import com.example.data.source.remote.IScholarRemoteSource
import com.example.data.source.remote.ScholarRemoteSource
import com.example.domain.model.ScholarItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ScholarRepositoryImplTest {

    private lateinit var remoteSource: IScholarRemoteSource
    private lateinit var repository: ScholarRepositoryImpl

    @Before
    fun setup() {
        remoteSource = mock()
        repository = ScholarRepositoryImpl(remoteSource)
    }

    @Test
    fun `searchScholar should return list from remote source`() = runTest {
        val expected = listOf(
            ScholarItem("Title", "Link", "Description", "Author", "DOI")
        )
        whenever(remoteSource.search("query", 0)).thenReturn(expected)

        val result = repository.searchScholar("query", 0)

        assertEquals(expected, result)
    }
}

