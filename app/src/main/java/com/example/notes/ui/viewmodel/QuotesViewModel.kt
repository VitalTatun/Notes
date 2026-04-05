package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.local.entities.Quote
import com.example.notes.data.repository.QuotesRepository
import com.example.notes.util.isSameDay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuotesViewModel(private val repository: QuotesRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val quotes: StateFlow<List<Quote>> = combine(
        searchQuery
            .debounce(300L)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.allQuotes
                } else {
                    repository.searchQuotes(query)
                }
            },
        selectedDateMillis
    ) { quotes, selectedDateMillis ->
        if (selectedDateMillis == null) {
            quotes
        } else {
            quotes.filter { it.createdAt.isSameDay(selectedDateMillis) }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun setSelectedDate(dateMillis: Long?) {
        _selectedDateMillis.value = dateMillis
    }

    fun addQuote(text: String, author: String) {
        viewModelScope.launch {
            repository.insertQuote(Quote(text = text, author = author))
        }
    }

    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            repository.updateQuote(quote)
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            repository.deleteQuote(quote)
        }
    }

    suspend fun getQuoteById(id: Long): Quote? {
        return repository.getQuoteById(id)
    }

    class Factory(private val repository: QuotesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuotesViewModel::class.java)) {
                return QuotesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
