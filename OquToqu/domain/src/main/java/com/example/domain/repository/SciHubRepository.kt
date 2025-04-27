package com.example.domain.repository

import com.example.domain.model.SciHubResult

interface SciHubRepository {
    suspend fun fetchPdf(doi: String): SciHubResult?
}
