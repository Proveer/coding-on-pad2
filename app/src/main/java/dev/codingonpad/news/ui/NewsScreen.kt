package dev.codingonpad.news.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.codingonpad.news.data.FeedSource
import dev.codingonpad.news.data.NewsItem
import java.text.DateFormat
import java.util.Date
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Coding on Pad") },
                    actions = {
                        IconButton(
                            onClick = viewModel::refresh,
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
                if (state.isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                CategoryRow(
                    selected = state.selectedCategory,
                    onSelect = viewModel::selectCategory
                )
                StatusBar(
                    lastUpdated = state.lastUpdated,
                    itemCount = state.items.size,
                    failed = state.failedSources,
                    error = state.error
                )
            }
        }
    ) { padding ->
        val visible = state.items.filter {
            state.selectedCategory == null || it.category == state.selectedCategory
        }
        if (visible.isEmpty() && !state.isLoading) {
            EmptyState(state.error)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visible, key = { it.url }) { item ->
                    NewsCard(item) {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    selected: FeedSource.Category?,
    onSelect: (FeedSource.Category?) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All") }
        )
        FeedSource.Category.entries.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat.label()) }
            )
        }
    }
}

@Composable
private fun StatusBar(
    lastUpdated: Long,
    itemCount: Int,
    failed: List<String>,
    error: String?
) {
    val text = buildString {
        if (lastUpdated > 0L) {
            append("Updated ")
            append(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(lastUpdated)))
            append("  \u00b7  ")
            append("$itemCount items")
        } else {
            append("Tap refresh to load news")
        }
        if (failed.isNotEmpty()) {
            append("  \u00b7  ")
            append("${failed.size} source(s) failed")
        }
        if (error != null) {
            append("  \u00b7  ")
            append(error)
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = relativeTime(item.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (item.summary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyState(error: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = error ?: "No news yet. Tap refresh.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun FeedSource.Category.label(): String = when (this) {
    FeedSource.Category.RESEARCH -> "Research"
    FeedSource.Category.AI_NEWS -> "AI News"
    FeedSource.Category.CODING -> "Coding"
    FeedSource.Category.HACKER_NEWS -> "HN"
}

private fun relativeTime(publishedAt: Long): String {
    if (publishedAt <= 0L) return ""
    val now = System.currentTimeMillis()
    val diff = max(0L, now - publishedAt)
    val minutes = diff / 60_000L
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        minutes < 1440 * 7 -> "${minutes / 1440}d ago"
        else -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(publishedAt))
    }
}
