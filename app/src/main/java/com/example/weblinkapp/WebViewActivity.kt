package com.example.weblinkapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webView)
        val backBtn = findViewById<Button>(R.id.backButton)
        val url = intent.getStringExtra("url") ?: ""
        val filePath = intent.getStringExtra("filePath") ?: ""
        val title = intent.getStringExtra("title") ?: "Appsas"
        val standalone = intent.getBooleanExtra("standalone", false)

        if (standalone) {
            // Darbalaukio nuoroda - kraunam URL tiesiogiai
            supportActionBar?.hide()
            backBtn.visibility = Button.GONE
            setTitle(title)

            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = true

            if (url.isNotEmpty()) {
                webView.loadUrl(url)
            } else if (filePath.isNotEmpty()) {
                webView.loadDataWithBaseURL(null, File(filePath).readText(), "text/html", "UTF-8", null)
            }
        } else {
            // Peržiūra iš pagrindinio apps'o
            supportActionBar?.title = title
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            backBtn.visibility = Button.VISIBLE
            backBtn.setOnClickListener {
                if (webView.canGoBack()) webView.goBack() else finish()
            }

            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = true
            webView.settings.allowFileAccess = true

            try {
                val file = File(filePath)
                if (file.exists()) {
                    webView.loadDataWithBaseURL(null, file.readText(), "text/html", "UTF-8", null)
                } else {
                    Toast.makeText(this, "Failas nerastas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
