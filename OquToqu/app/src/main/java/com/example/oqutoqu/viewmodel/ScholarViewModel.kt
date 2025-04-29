package com.example.oqutoqu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ScholarItem
import com.example.domain.repository.ScholarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScholarViewModel(
    private val repository: ScholarRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ScholarItem>>(emptyList())
    val items: StateFlow<List<ScholarItem>> get() = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var currentPage = 0
    private var currentQuery: String = ""

    fun search(query: String, page: Int = 0) {
        currentQuery = query
        currentPage = page

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.searchScholar(query, page)
                if (page == 0) {
                    _items.value = result
                } else {
                    _items.value = _items.value + result
                }
            } catch (e: Exception) {
                if (page == 0) _items.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value) return
        search(currentQuery, currentPage + 1)
    }
}

