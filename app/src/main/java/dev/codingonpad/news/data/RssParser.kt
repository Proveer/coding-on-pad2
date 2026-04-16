package dev.codingonpad.news.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Minimal RSS 2.0 + Atom 1.0 parser. Extracts title, link, date, summary, and a
 * best-effort thumbnail URL. Uses Android's XmlPullParser, which does not resolve
 * external entities by default.
 */
object RssParser {

    fun parse(xml: String, source: FeedSource): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        val current = mutableMapOf<String, String>()
        var image: String? = null
        var inEntry = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name
                    if (tag == "item" || tag == "entry") {
                        inEntry = true
                        current.clear()
                        image = null
                    } else if (inEntry) {
                        when (tag) {
                            "title", "description", "summary",
                            "content", "content:encoded",
                            "pubDate", "published", "updated", "dc:date" ->
                                current[tag] = safeText(parser)
                            "link" -> {
                                val href = parser.getAttributeValue(null, "href")
                                val rel = parser.getAttributeValue(null, "rel")
                                if (href != null) {
                                    if (rel == null || rel == "alternate") current["link"] = href
                                } else {
                                    current["link"] = safeText(parser)
                                }
                            }
                            "media:thumbnail", "media:content" -> {
                                val url = parser.getAttributeValue(null, "url")
                                val type = parser.getAttributeValue(null, "type")
                                if (url != null && image == null &&
                                    (type == null || type.startsWith("image/"))
                                ) image = url
                            }
                            "enclosure" -> {
                                val url = parser.getAttributeValue(null, "url")
                                val type = parser.getAttributeValue(null, "type")
                                if (url != null && image == null &&
                                    type != null && type.startsWith("image/")
                                ) image = url
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name
                    if ((tag == "item" || tag == "entry") && inEntry) {
                        val title = current["title"].orEmpty()
                        val url = current["link"].orEmpty()
                        val rawSummary = current["description"]
                            ?: current["summary"]
                            ?: current["content:encoded"]
                            ?: current["content"]
                            ?: ""
                        val date = current["pubDate"]
                            ?: current["published"]
                            ?: current["updated"]
                            ?: current["dc:date"]
                            ?: ""
                        if (image == null) image = extractFirstImg(rawSummary)
                        if (title.isNotBlank() && url.isNotBlank()) {
                            items += NewsItem(
                                title = cleanHtml(title),
                                url = url.trim(),
                                source = source.name,
                                category = source.category,
                                publishedAt = parseDate(date),
                                summary = cleanHtml(rawSummary).take(500),
                                imageUrl = image
                            )
                        }
                        inEntry = false
                        current.clear()
                        image = null
                    }
                }
            }
            event = parser.next()
        }
        return items
    }

    private fun safeText(parser: XmlPullParser): String = try {
        parser.nextText().trim()
    } catch (_: Exception) {
        ""
    }

    private val imgSrcRegex = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val htmlTag = Regex("<[^>]+>")
    private val whitespace = Regex("\\s+")

    private fun extractFirstImg(html: String): String? =
        imgSrcRegex.find(html)?.groupValues?.getOrNull(1)

    private fun cleanHtml(s: String): String = s
        .replace(htmlTag, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace(whitespace, " ")
        .trim()

    private val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd"
    )

    private fun parseDate(input: String): Long {
        if (input.isBlank()) return 0L
        for (fmt in dateFormats) {
            try {
                val parsed = SimpleDateFormat(fmt, Locale.ENGLISH).parse(input)
                if (parsed != null) return parsed.time
            } catch (_: Exception) { /* try next */ }
        }
        return 0L
    }
}
