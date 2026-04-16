package dev.codingonpad.news.data

/**
 * Curated feed list focused on AI, ML research, and coding news.
 * To customize: edit this list and rebuild. Each source provides RSS or Atom.
 */
data class FeedSource(
    val name: String,
    val url: String,
    val category: Category
) {
    enum class Category { RESEARCH, AI_NEWS, CODING, HACKER_NEWS }

    companion object {
        val ALL: List<FeedSource> = listOf(
            FeedSource("arXiv cs.AI", "https://export.arxiv.org/rss/cs.AI", Category.RESEARCH),
            FeedSource("arXiv cs.LG", "https://export.arxiv.org/rss/cs.LG", Category.RESEARCH),
            FeedSource("arXiv cs.CL", "https://export.arxiv.org/rss/cs.CL", Category.RESEARCH),
            FeedSource("Hacker News", "https://hnrss.org/frontpage", Category.HACKER_NEWS),
            FeedSource("HN: AI", "https://hnrss.org/newest?q=AI+OR+LLM+OR+GPT+OR+Claude", Category.HACKER_NEWS),
            FeedSource("Simon Willison", "https://simonwillison.net/atom/everything/", Category.AI_NEWS),
            FeedSource("The Batch", "https://www.deeplearning.ai/the-batch/feed/", Category.AI_NEWS),
            FeedSource("Anthropic", "https://www.anthropic.com/news/rss.xml", Category.AI_NEWS),
            FeedSource("OpenAI", "https://openai.com/news/rss.xml", Category.AI_NEWS),
            FeedSource("Google AI", "https://blog.google/technology/ai/rss/", Category.AI_NEWS),
            FeedSource("GitHub Blog", "https://github.blog/feed/", Category.CODING),
            FeedSource("GitHub Trending", "https://mshibanami.github.io/GitHubTrendingRSS/daily/all.xml", Category.CODING),
            FeedSource("MIT Tech Review AI", "https://www.technologyreview.com/feed/", Category.AI_NEWS),
        )
    }
}
