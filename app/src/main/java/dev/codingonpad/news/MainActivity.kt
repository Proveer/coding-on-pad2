package dev.codingonpad.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.codingonpad.news.ui.ArticleScreen
import dev.codingonpad.news.ui.NewsScreen
import dev.codingonpad.news.ui.NewsViewModel
import dev.codingonpad.news.ui.theme.CodingOnPadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NewsViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            CodingOnPadTheme(themePreference = state.themePreference) {
                AnimatedContent(
                    targetState = state.selectedArticle,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen"
                ) { article ->
                    if (article != null) {
                        ArticleScreen(
                            item = article,
                            onClose = viewModel::closeArticle
                        )
                    } else {
                        NewsScreen(
                            state = state,
                            onRefresh = viewModel::refresh,
                            onSelectCategory = viewModel::selectCategory,
                            onCycleTheme = viewModel::cycleTheme,
                            onOpenArticle = viewModel::openArticle
                        )
                    }
                }
            }
        }
    }
}
