package dev.codingonpad.news.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import dev.codingonpad.news.ui.theme.Spacing
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    state: NewsUiState,
    onRefresh: () -> Unit,
    onSelectCategory: (FeedSource.Category?) -> Unit,
    onCycleTheme: () -> Unit,
    onOpenArticle: (NewsItem) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    val pullState = rememberPullToRefreshState()
    val haptics = LocalHapticFeedback.current

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (spoken.isNotEmpty()) onQueryChange(spoken)
        }
    }

    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = { BrandTitle() },
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
                SearchBar(
                    query = state.searchQuery,
                    isSearching = state.isSearching,
                    onQueryChange = onQueryChange,
                    onClear = onClearQuery,
                    onVoice = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Search news")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        runCatching { voiceLauncher.launch(intent) }
                    }
                )
                CategoryRow(
                    selected = state.selectedCategory,
                    onSelect = onSelectCategory
                )
                StatusBar(
                    lastUpdated = state.lastUpdated,
                    itemCount = state.items.size,
                    failed = state.failedSources,
                    error = state.error,
                    searchActive = state.searchQuery.isNotBlank()
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val visible = remember(
            state.items,
            state.selectedCategory,
            state.searchQuery,
            state.liveResults
        ) {
            val byCategory = state.items.filter {
                state.selectedCategory == null || it.category == state.selectedCategory
            }
            val q = state.searchQuery.trim()
            if (q.isBlank()) {
                byCategory
            } else {
                val needle = q.lowercase()
                val localMatches = byCategory.filter {
                    it.title.lowercase().contains(needle) ||
                        it.summary.lowercase().contains(needle) ||
                        it.source.lowercase().contains(needle)
                }
                (localMatches + state.liveResults)
                    .distinctBy { it.url }
                    .sortedByDescending { it.publishedAt }
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
                EmptyState(
                    error = state.error,
                    searching = state.searchQuery.isNotBlank()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.md,
                        end = Spacing.md,
                        top = Spacing.sm,
                        bottom = Spacing.lg + Spacing.xs
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xs)
                ) {
                    items(visible, key = { it.url }) { item ->
                        NewsCard(
                            item = item,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(240, easing = FastOutSlowInEasing),
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ),
                            onOpen = {
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
private fun BrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = "Coding on Pad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onVoice: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        placeholder = {
            Text(
                "Search papers, AI, code\u2026",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(Spacing.xs + 2.dp))
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
                IconButton(onClick = onVoice) {
                    Icon(Icons.Filled.Mic, contentDescription = "Voice search")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
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
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All", style = MaterialTheme.typography.labelLarge) },
            shape = MaterialTheme.shapes.extraLarge,
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
                shape = MaterialTheme.shapes.extraLarge,
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
    error: String?,
    searchActive: Boolean
) {
    val dotColor by animateColorAsState(
        targetValue = when {
            error != null -> MaterialTheme.colorScheme.error
            searchActive -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        },
        label = "statusDot"
    )
    val text = buildString {
        when {
            searchActive -> append("LIVE SEARCH  \u00b7  arXiv + Hacker News")
            lastUpdated > 0L -> {
                append("UPDATED ")
                append(
                    DateFormat.getTimeInstance(DateFormat.SHORT)
                        .format(Date(lastUpdated)).uppercase()
                )
                append("  \u00b7  ")
                append("$itemCount stories")
            }
            else -> append("Pull to refresh")
        }
        if (failed.isNotEmpty() && !searchActive) {
            append("  \u00b7  ")
            append("${failed.size} source(s) unavailable")
        }
        if (error != null) {
            append("  \u00b7  ")
            append(error)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md + Spacing.xs, vertical = Spacing.xs + 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NewsCard(
    item: NewsItem,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit
) {
    var expanded by rememberSaveable(item.url) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val containerColor by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(220),
        label = "cardContainer"
    )
    val borderAlpha by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        animationSpec = tween(220),
        label = "cardBorder"
    )

    Card(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            expanded = !expanded
        },
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expanded) 6.dp else 0.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderAlpha),
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            Row(Modifier.padding(Spacing.sm + Spacing.xs)) {
                Thumbnail(
                    item = item,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(MaterialTheme.shapes.medium),
                    initialFontSize = 32
                )
                Spacer(Modifier.width(Spacing.sm + Spacing.xs))
                Column(Modifier.fillMaxWidth()) {
                    MetaRow(item)
                    Spacer(Modifier.height(Spacing.xs + 2.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expanded) 5 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.category == FeedSource.Category.HACKER_NEWS) {
                        Spacer(Modifier.height(Spacing.xs + 2.dp))
                        HnStatsRow(item)
                    } else if (item.summary.isNotBlank() && !expanded) {
                        Spacer(Modifier.height(Spacing.xs))
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

            AnimatedVisibility(
                visible = expanded,
                enter = androidx.compose.animation.expandVertically(
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ) + androidx.compose.animation.fadeIn(tween(260)),
                exit = androidx.compose.animation.shrinkVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + androidx.compose.animation.fadeOut(tween(180))
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Spacing.md,
                            end = Spacing.md,
                            bottom = Spacing.md
                        )
                ) {
                    if (!item.imageUrl.isNullOrBlank() ||
                        item.category != FeedSource.Category.HACKER_NEWS
                    ) {
                        Thumbnail(
                            item = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(MaterialTheme.shapes.medium),
                            initialFontSize = 64
                        )
                        Spacer(Modifier.height(Spacing.sm + Spacing.xs))
                    }
                    if (item.summary.isNotBlank()) {
                        Text(
                            text = item.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(Spacing.sm + Spacing.xs))
                    }
                    Button(
                        onClick = onOpen,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.OpenInNew, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "READ ARTICLE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaRow(item: NewsItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CategoryPill(item.category)
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = item.source.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(Spacing.sm))
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
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item.points?.let {
            StatChip(icon = { Icon(Icons.Filled.ArrowUpward, null, Modifier.size(14.dp)) }, text = "$it")
        }
        item.commentCount?.let {
            StatChip(icon = { Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(14.dp)) }, text = "$it")
        }
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
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(horizontal = Spacing.sm + 2.dp, vertical = Spacing.xs)
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
            modifier = Modifier.padding(horizontal = Spacing.sm + 2.dp, vertical = 3.dp)
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
private fun EmptyState(error: String?, searching: Boolean) {
    val message = when {
        error != null -> error
        searching -> "No matches yet. Try a different query."
        else -> "No news yet. Pull to refresh."
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
