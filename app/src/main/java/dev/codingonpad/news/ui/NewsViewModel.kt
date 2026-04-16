package dev.codingonpad.news.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.codingonpad.news.NewsApp
import dev.codingonpad.news.data.FeedSource
import dev.codingonpad.news.data.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val items: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L,
    val selectedCategory: FeedSource.Category? = null,
    val failedSources: List<String> = emptyList()
)

class NewsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as NewsApp).repository

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val result = repo.fetchAll()
                _state.update {
                    it.copy(
                        items = result.items,
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis(),
                        failedSources = result.failedSources
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Fetch failed") }
            }
        }
    }

    fun selectCategory(category: FeedSource.Category?) {
        _state.update { it.copy(selectedCategory = category) }
    }
}
