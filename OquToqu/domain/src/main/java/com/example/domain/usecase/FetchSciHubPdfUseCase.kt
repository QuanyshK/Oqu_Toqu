package com.example.domain.usecase

import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository

class FetchSciHubPdfUseCase(private val repository: SciHubRepository) {
    suspend operator fun invoke(doi: String): SciHubResult? {
        return repository.fetchPdf(doi)
    }
}
