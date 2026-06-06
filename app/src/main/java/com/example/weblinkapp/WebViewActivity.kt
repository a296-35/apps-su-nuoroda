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
        val filePath = intent.getStringExtra("filePath") ?: ""
        val title = intent.getStringExtra("title") ?: "Appsas"
        val standalone = intent.getBooleanExtra("standalone", false)

        // Jei atidaryta iš darbalaukio nuorodos - slepiam action bar ir mygtuką
        if (standalone) {
            supportActionBar?.hide()
            backBtn.visibility = Button.GONE
            // Darbalaukyje rodom apps pavadinimą
            setTitle(title)
        } else {
            supportActionBar?.title = title
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            backBtn.visibility = Button.VISIBLE
            backBtn.setOnClickListener {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
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
                val htmlContent = file.readText()
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            } else {
                webView.loadDataWithBaseURL(null, "<h2>Failas nerastas</h2>", "text/html", "UTF-8", null)
            }
        } catch (e: Exception) {
            webView.loadDataWithBaseURL(null, "<h2>Klaida: ${e.message}</h2>", "text/html", "UTF-8", null)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
