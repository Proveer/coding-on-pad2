package dev.codingonpad.news.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import dev.codingonpad.news.data.FeedSource
import dev.codingonpad.news.data.NewsItem
import java.text.DateFormat
import java.util.Date
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    state: NewsUiState,
    onRefresh: () -> Unit,
    onSelectCategory: (FeedSource.Category?) -> Unit,
    onCycleTheme: () -> Unit,
    onOpenArticle: (NewsItem) -> Unit
) {
    val pullState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Coding on Pad",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = onCycleTheme) {
                            val icon = when (state.themePreference) {
                                ThemePreference.AUTO -> Icons.Filled.BrightnessAuto
                                ThemePreference.LIGHT -> Icons.Filled.LightMode
                                ThemePreference.DARK -> Icons.Filled.DarkMode
                            }
                            Icon(icon, contentDescription = "Theme: ${state.themePreference.name}")
                        }
                        IconButton(
                            onClick = onRefresh,
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                CategoryRow(
                    selected = state.selectedCategory,
                    onSelect = onSelectCategory
                )
                StatusBar(
                    lastUpdated = state.lastUpdated,
                    itemCount = state.items.size,
                    failed = state.failedSources,
                    error = state.error
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val visible = state.items.filter {
            state.selectedCategory == null || it.category == state.selectedCategory
        }
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (visible.isEmpty() && !state.isLoading) {
                EmptyState(state.error)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(visible, key = { it.url }) { item ->
                        NewsCard(item) { onOpenArticle(item) }
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
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All") },
            shape = RoundedCornerShape(50)
        )
        FeedSource.Category.entries.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat.label()) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
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
            append("Pull down or tap refresh to load news")
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
            .padding(horizontal = 18.dp, vertical = 4.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            if (!item.imageUrl.isNullOrBlank()) {
                ArticleThumbnail(
                    imageUrl = item.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                )
            }
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryPill(item.category)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.source,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = relativeTime(item.publishedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight
                )
                if (item.summary.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleThumbnail(imageUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        loading = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        error = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    )
}

@Composable
private fun CategoryPill(category: FeedSource.Category) {
    val (bg, fg) = when (category) {
        FeedSource.Category.RESEARCH -> Color(0xFF2D6CDF) to Color.White
        FeedSource.Category.AI_NEWS -> Color(0xFFE26D5C) to Color.White
        FeedSource.Category.CODING -> Color(0xFF2EA043) to Color.White
        FeedSource.Category.HACKER_NEWS -> Color(0xFFFF6600) to Color.White
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = category.label(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun EmptyState(error: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = error ?: "No news yet. Pull to refresh.",
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
