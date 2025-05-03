package com.example.data.source.remote

import com.example.domain.model.ScholarItem

interface IScholarRemoteSource {
    suspend fun search(query: String, page: Int): List<ScholarItem>
}
