package dev.codingonpad.news.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.codingonpad.news.data.FeedSource
import dev.codingonpad.news.data.NewsItem
import java.text.DateFormat
import java.util.Date
import kotlin.math.absoluteValue
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
    val haptics = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Coding on Pad",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCycleTheme()
                        }) {
                            val icon = when (state.themePreference) {
                                ThemePreference.AUTO -> Icons.Filled.BrightnessAuto
                                ThemePreference.LIGHT -> Icons.Filled.LightMode
                                ThemePreference.DARK -> Icons.Filled.DarkMode
                            }
                            Icon(icon, contentDescription = "Theme: ${state.themePreference.name}")
                        }
                        IconButton(onClick = onRefresh, enabled = !state.isLoading) {
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
        val visible = remember(state.items, state.selectedCategory) {
            state.items.filter {
                state.selectedCategory == null || it.category == state.selectedCategory
            }
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
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(visible, key = { _, it -> it.url }) { index, item ->
                        NewsCard(
                            item = item,
                            featured = index == 0,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(240),
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ),
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenArticle(item)
                            }
                        )
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All", style = MaterialTheme.typography.labelLarge) },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        FeedSource.Category.entries.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat.label(), style = MaterialTheme.typography.labelLarge) },
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
            append("UPDATED ")
            append(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(lastUpdated)).uppercase())
            append("  \u00b7  ")
            append("$itemCount stories")
        } else {
            append("Pull to refresh")
        }
        if (failed.isNotEmpty()) {
            append("  \u00b7  ")
            append("${failed.size} source(s) unavailable")
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
            .padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun NewsCard(
    item: NewsItem,
    featured: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(if (featured) 24.dp else 18.dp)
    ) {
        if (featured) FeaturedLayout(item) else CompactLayout(item)
    }
}

@Composable
private fun FeaturedLayout(item: NewsItem) {
    Column {
        Thumbnail(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            initialFontSize = 72
        )
        Column(Modifier.padding(18.dp)) {
            MetaRow(item)
            Spacer(Modifier.height(10.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (item.category == FeedSource.Category.HACKER_NEWS) {
                Spacer(Modifier.height(10.dp))
                HnStatsRow(item)
            } else if (item.summary.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
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
private fun CompactLayout(item: NewsItem) {
    Row(Modifier.padding(12.dp)) {
        Thumbnail(
            item = item,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(14.dp)),
            initialFontSize = 32
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.fillMaxWidth()) {
            MetaRow(item)
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (item.category == FeedSource.Category.HACKER_NEWS) {
                Spacer(Modifier.height(6.dp))
                HnStatsRow(item)
            } else if (item.summary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MetaRow(item: NewsItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CategoryPill(item.category)
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.source.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = relativeTime(item.publishedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HnStatsRow(item: NewsItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item.points?.let { StatChip(icon = { Icon(Icons.Filled.ArrowUpward, null, Modifier.size(14.dp)) }, text = "$it") }
        item.commentCount?.let { StatChip(icon = { Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(14.dp)) }, text = "$it") }
    }
}

@Composable
private fun StatChip(icon: @Composable () -> Unit, text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            icon()
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoryPill(category: FeedSource.Category) {
    val tint = category.tint()
    val bg = tint.copy(alpha = 0.14f)
    Surface(
        shape = CircleShape,
        color = bg,
        contentColor = tint
    ) {
        Text(
            text = category.label().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun Thumbnail(
    item: NewsItem,
    modifier: Modifier = Modifier,
    initialFontSize: Int
) {
    val context = LocalContext.current
    if (!item.imageUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            loading = { GradientInitialTile(item.source, initialFontSize, Modifier.fillMaxSize()) },
            error = { GradientInitialTile(item.source, initialFontSize, Modifier.fillMaxSize()) }
        )
    } else {
        GradientInitialTile(item.source, initialFontSize, modifier)
    }
}

@Composable
private fun GradientInitialTile(seed: String, fontSize: Int, modifier: Modifier = Modifier) {
    val brush = remember(seed) {
        val hue = (seed.hashCode().absoluteValue % 360).toFloat()
        val c1 = Color.hsv(hue, 0.55f, 0.55f)
        val c2 = Color.hsv((hue + 42f) % 360f, 0.70f, 0.38f)
        Brush.linearGradient(listOf(c1, c2))
    }
    val initial = seed.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(modifier.background(brush)) {
        Text(
            text = initial,
            fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = Color.White.copy(alpha = 0.88f),
            modifier = Modifier.align(Alignment.Center)
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

@Composable
private fun FeedSource.Category.tint(): Color = when (this) {
    FeedSource.Category.RESEARCH -> Color(0xFF3B5BDB)
    FeedSource.Category.AI_NEWS -> Color(0xFFE26D5C)
    FeedSource.Category.CODING -> Color(0xFF2EA043)
    FeedSource.Category.HACKER_NEWS -> Color(0xFFFF6600)
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
