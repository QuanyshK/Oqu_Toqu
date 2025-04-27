package com.example.data.repository

import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository
import com.example.scihubparser.SciHubParser

class SciHubRepositoryImpl : SciHubRepository {

    private val parser = SciHubParser()

    override suspend fun fetchPdf(doi: String): SciHubResult? {
        val result = parser.parse(doi)
        return result?.let {
            SciHubResult(
                pdfLink = it.pdfUrl,
                title = it.title
            )
        }
    }
}

