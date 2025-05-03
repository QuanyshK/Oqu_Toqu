package com.example.data.repository

import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository
import com.example.scihubparser.SciHubParser

class SciHubRepositoryImpl(    private val parser: SciHubParser = SciHubParser() ) : SciHubRepository {



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

