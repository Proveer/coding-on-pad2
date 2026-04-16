package dev.codingonpad.news.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Live search across arXiv (research papers) and Hacker News (via Algolia).
 * Both APIs are free and require no authentication.
 */
class SearchService(private val client: OkHttpClient) {

    suspend fun search(query: String, limit: Int = 25): List<NewsItem> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        return withContext(Dispatchers.IO) {
            coroutineScope {
                val arxiv = async { runCatching { searchArxiv(trimmed, limit) }.getOrDefault(emptyList()) }
                val hn = async { runCatching { searchHackerNews(trimmed, limit) }.getOrDefault(emptyList()) }
                (arxiv.await() + hn.await())
                    .distinctBy { it.url }
                    .sortedByDescending { it.publishedAt }
            }
        }
    }

    private fun searchArxiv(query: String, limit: Int): List<NewsItem> {
        val encoded = URLEncoder.encode("all:$query", "UTF-8")
        val url = "https://export.arxiv.org/api/query?search_query=$encoded" +
            "&sortBy=submittedDate&sortOrder=descending&max_results=$limit"
        val body = get(url) ?: return emptyList()
        val pseudoSource = FeedSource(
            name = "arXiv search",
            url = url,
            category = FeedSource.Category.RESEARCH
        )
        return RssParser.parse(body, pseudoSource)
    }

    private fun searchHackerNews(query: String, limit: Int): List<NewsItem> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://hn.algolia.com/api/v1/search?query=$encoded&tags=story&hitsPerPage=$limit"
        val body = get(url) ?: return emptyList()
        return parseHnAlgolia(body)
    }

    private fun parseHnAlgolia(body: String): List<NewsItem> {
        val json = runCatching { JSONObject(body) }.getOrNull() ?: return emptyList()
        val hits = json.optJSONArray("hits") ?: return emptyList()
        val out = mutableListOf<NewsItem>()
        for (i in 0 until hits.length()) {
            val h = hits.optJSONObject(i) ?: continue
            val title = h.optString("title").takeIf { it.isNotBlank() } ?: continue
            val storyUrl = h.optString("url").takeIf { it.isNotBlank() }
            val objectId = h.optString("objectID")
            val commentsUrl = "https://news.ycombinator.com/item?id=$objectId"
            val articleUrl = storyUrl ?: commentsUrl
            val points = h.optInt("points", 0).takeIf { it > 0 }
            val commentCount = h.optInt("num_comments", 0).takeIf { it > 0 }
            val createdAt = h.optLong("created_at_i", 0L) * 1000L
            out += NewsItem(
                title = title,
                url = articleUrl,
                source = "HN search",
                category = FeedSource.Category.HACKER_NEWS,
                publishedAt = createdAt,
                summary = "",
                points = points,
                commentCount = commentCount,
                commentsUrl = commentsUrl
            )
        }
        return out
    }

    private fun get(url: String): String? {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "CodingOnPad/0.1")
            .header("Accept", "application/xml, application/json;q=0.9, */*;q=0.8")
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.string() else null
            }
        }.getOrNull()
    }
}
