package dev.codingonpad.news.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.codingonpad.news.NewsApp
import dev.codingonpad.news.data.FeedSource
import dev.codingonpad.news.data.NewsItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

enum class ThemePreference { AUTO, LIGHT, DARK }

data class NewsUiState(
    val items: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L,
    val selectedCategory: FeedSource.Category? = null,
    val failedSources: List<String> = emptyList(),
    val selectedArticle: NewsItem? = null,
    val themePreference: ThemePreference = ThemePreference.AUTO,
    val searchQuery: String = "",
    val liveResults: List<NewsItem> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class)
class NewsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as NewsApp).repository
    private val prefs = app.getSharedPreferences("codingonpad", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        NewsUiState(themePreference = loadThemePreference())
    )
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var liveSearchJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            queryFlow
                .drop(1)
                .debounce(350)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) {
                        _state.value = _state.value.copy(liveResults = emptyList(), isSearching = false)
                    } else {
                        runLiveSearch(q)
                    }
                }
        }
    }

    fun refresh() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.fetchAll()
                _state.value = _state.value.copy(
                    items = result.items,
                    isLoading = false,
                    lastUpdated = System.currentTimeMillis(),
                    failedSources = result.failedSources
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Fetch failed")
            }
        }
    }

    fun selectCategory(category: FeedSource.Category?) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun openArticle(item: NewsItem) {
        _state.value = _state.value.copy(selectedArticle = item)
    }

    fun closeArticle() {
        _state.value = _state.value.copy(selectedArticle = null)
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        queryFlow.value = query
    }

    fun clearSearch() {
        setSearchQuery("")
    }

    private fun runLiveSearch(q: String) {
        liveSearchJob?.cancel()
        liveSearchJob = viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)
            val results = runCatching { repo.search.search(q) }.getOrDefault(emptyList())
            if (_state.value.searchQuery == q) {
                _state.value = _state.value.copy(liveResults = results, isSearching = false)
            }
        }
    }

    fun cycleTheme() {
        val next = when (_state.value.themePreference) {
            ThemePreference.AUTO -> ThemePreference.LIGHT
            ThemePreference.LIGHT -> ThemePreference.DARK
            ThemePreference.DARK -> ThemePreference.AUTO
        }
        prefs.edit().putString(KEY_THEME, next.name).apply()
        _state.value = _state.value.copy(themePreference = next)
    }

    private fun loadThemePreference(): ThemePreference {
        val name = prefs.getString(KEY_THEME, null) ?: return ThemePreference.AUTO
        return runCatching { ThemePreference.valueOf(name) }.getOrDefault(ThemePreference.AUTO)
    }

    companion object {
        private const val KEY_THEME = "theme_preference"
    }
}
