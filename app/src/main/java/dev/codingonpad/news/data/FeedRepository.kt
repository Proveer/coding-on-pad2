package dev.codingonpad.news.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class FeedRepository(context: Context) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .cache(Cache(File(context.cacheDir, "http"), 10L * 1024 * 1024))
        .build()

    data class FetchResult(
        val items: List<NewsItem>,
        val failedSources: List<String>
    )

    suspend fun fetchAll(sources: List<FeedSource> = FeedSource.ALL): FetchResult =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val results = sources.map { source ->
                    async { fetchOneSafe(source) }
                }.awaitAll()

                val items = results.flatMap { it.second }
                    .distinctBy { it.url }
                    .sortedByDescending { it.publishedAt }
                    .take(500)

                val failed = results.filter { !it.first }.map { it.third }
                FetchResult(items = items, failedSources = failed)
            }
        }

    private fun fetchOneSafe(source: FeedSource): Triple<Boolean, List<NewsItem>, String> = try {
        val req = Request.Builder()
            .url(source.url)
            .header(
                "User-Agent",
                "CodingOnPad/0.1 (+https://github.com/proveer/coding-on-pad2)"
            )
            .header(
                "Accept",
                "application/rss+xml, application/atom+xml, application/xml;q=0.9, */*;q=0.8"
            )
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                Triple(false, emptyList(), source.name)
            } else {
                val body = resp.body?.string().orEmpty()
                val parsed = RssParser.parse(body, source)
                Triple(true, parsed, source.name)
            }
        }
    } catch (_: Exception) {
        Triple(false, emptyList(), source.name)
    }
}
