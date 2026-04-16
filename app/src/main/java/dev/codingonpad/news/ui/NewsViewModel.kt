package dev.codingonpad.news.ui

import android.app.Application
import android.content.Context
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

enum class ThemePreference { AUTO, LIGHT, DARK }

data class NewsUiState(
    val items: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L,
    val selectedCategory: FeedSource.Category? = null,
    val failedSources: List<String> = emptyList(),
    val selectedArticle: NewsItem? = null,
    val themePreference: ThemePreference = ThemePreference.AUTO
)

class NewsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as NewsApp).repository
    private val prefs = app.getSharedPreferences("codingonpad", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        NewsUiState(themePreference = loadThemePreference())
    )
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

    fun openArticle(item: NewsItem) {
        _state.update { it.copy(selectedArticle = item) }
    }

    fun closeArticle() {
        _state.update { it.copy(selectedArticle = null) }
    }

    fun cycleTheme() {
        val next = when (_state.value.themePreference) {
            ThemePreference.AUTO -> ThemePreference.LIGHT
            ThemePreference.LIGHT -> ThemePreference.DARK
            ThemePreference.DARK -> ThemePreference.AUTO
        }
        prefs.edit().putString(KEY_THEME, next.name).apply()
        _state.update { it.copy(themePreference = next) }
    }

    private fun loadThemePreference(): ThemePreference {
        val name = prefs.getString(KEY_THEME, null) ?: return ThemePreference.AUTO
        return runCatching { ThemePreference.valueOf(name) }.getOrDefault(ThemePreference.AUTO)
    }

    companion object {
        private const val KEY_THEME = "theme_preference"
    }
}
