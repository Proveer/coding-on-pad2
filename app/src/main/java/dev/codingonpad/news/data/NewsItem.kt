package dev.codingonpad.news.data

data class NewsItem(
    val title: String,
    val url: String,
    val source: String,
    val category: FeedSource.Category,
    val publishedAt: Long,
    val summary: String
)
