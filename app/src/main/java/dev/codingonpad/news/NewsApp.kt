package dev.codingonpad.news

import android.app.Application
import dev.codingonpad.news.data.FeedRepository

class NewsApp : Application() {
    lateinit var repository: FeedRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = FeedRepository(this)
    }
}
