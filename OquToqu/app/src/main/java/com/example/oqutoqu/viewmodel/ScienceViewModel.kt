package com.example.oqutoqu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScienceViewModel(private val repository: SciHubRepository) : ViewModel() {

    private val _sciHubResult = MutableStateFlow<SciHubResult?>(null)
    val sciHubResult: StateFlow<SciHubResult?> = _sciHubResult

    var lastDoi: String? = null
        private set
    var lastPdfUrl: String? = null
        private set

    fun fetchPdf(doi: String) {
        if (doi == lastDoi && !lastPdfUrl.isNullOrBlank()) return
        viewModelScope.launch {
            val result = repository.fetchPdf(doi)
            if (result?.pdfLink != null) {
                lastDoi = doi
                lastPdfUrl = "https:${result.pdfLink}"
            }
            _sciHubResult.value = result
        }
    }
}

