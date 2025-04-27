package com.example.oqutoqu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SciHubResult
import com.example.domain.repository.SciHubRepository
import kotlinx.coroutines.launch

class ScienceViewModel(private val repository: SciHubRepository) : ViewModel() {

    private val _sciHubResult = MutableLiveData<SciHubResult?>()
    val sciHubResult: LiveData<SciHubResult?> = _sciHubResult

    fun fetchPdf(doi: String) {
        viewModelScope.launch {
            val result = repository.fetchPdf(doi)
            _sciHubResult.value = result
        }
    }
}
