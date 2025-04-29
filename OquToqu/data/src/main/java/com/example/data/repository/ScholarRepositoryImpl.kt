package com.example.data.repository

import com.example.data.source.remote.ScholarApi
import com.example.data.source.remote.ScholarRemoteSource
import com.example.domain.model.ScholarItem
import com.example.domain.repository.ScholarRepository


class ScholarRepositoryImpl(
    private val remoteSource: ScholarRemoteSource
) : ScholarRepository {

    override suspend fun searchScholar(query: String, page: Int): List<ScholarItem> {
        return remoteSource.search(query, page = page)
    }
}
