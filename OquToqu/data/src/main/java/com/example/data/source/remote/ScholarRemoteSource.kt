package com.example.data.source.remote

import android.util.Log
import com.example.domain.model.ScholarItem

class ScholarRemoteSource(
    private val api: ScholarApi
) {
    suspend fun search(query: String, page: Int = 0, pageSize: Int = 10): List<ScholarItem> {
        val start = page * pageSize
        val response = api.search(query = query, start = start)

        if (!response.isSuccessful) {
            throw Exception("Failed: ${response.code()}")
        }

        val results = response.body()?.organic_results ?: return emptyList()

        return results.map {
            ScholarItem(
                title = it.title ?: "No title",
                link = it.link,
                snippet = it.snippet,
                authors = it.publication_info?.summary,
                doi = extractDoi(it.link)
            )
        }
    }


    private fun extractDoi(link: String?): String? {
        if (link.isNullOrBlank()) return null
        val regex = Regex("""10\.\d{4,9}/[\w.()\-;:/]+""", RegexOption.IGNORE_CASE)
        val match = regex.find(link)?.value
        return match
    }
}
