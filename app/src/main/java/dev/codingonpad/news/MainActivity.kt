package dev.codingonpad.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.codingonpad.news.ui.NewsScreen
import dev.codingonpad.news.ui.theme.CodingOnPadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodingOnPadTheme {
                NewsScreen()
            }
        }
    }
}
