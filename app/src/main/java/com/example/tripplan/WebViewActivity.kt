package com.example.tripplan.board

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.R

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val url = intent.getStringExtra("url")

        val webView = findViewById<WebView>(R.id.webView)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        if (url != null) {
            webView.loadUrl(url)
        }

        closeButton.setOnClickListener {
            finish() // 액티비티 종료
        }
    }
}
