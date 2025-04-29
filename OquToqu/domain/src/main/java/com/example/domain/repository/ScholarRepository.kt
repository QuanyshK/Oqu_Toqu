package com.example.domain.repository

import com.example.domain.model.ScholarItem

interface ScholarRepository {
    suspend fun searchScholar(query: String, page: Int = 0): List<ScholarItem>
}
