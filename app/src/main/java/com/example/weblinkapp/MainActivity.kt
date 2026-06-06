package com.example.weblinkapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var appsList: MutableList<App>
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var titleInput: EditText
    private lateinit var urlInput: EditText
    private lateinit var descInput: EditText
    private lateinit var createBtn: Button
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = AppDatabaseHelper(this)
        appsList = mutableListOf()

        titleInput = findViewById(R.id.titleEditText)
        urlInput = findViewById(R.id.urlEditText)
        descInput = findViewById(R.id.descriptionEditText)
        createBtn = findViewById(R.id.createButton)
        listView = findViewById(R.id.appsListView)

        createBtn.setOnClickListener { createNewApp() }

        loadApps()
        setupListView()
    }

    private fun createNewApp() {
        val title = titleInput.text.toString().trim()
        val url = urlInput.text.toString().trim()
        val description = descInput.text.toString().trim()

        if (title.isEmpty() || url.isEmpty()) {
            Toast.makeText(this, "Įvesk pavadinimą ir URL", Toast.LENGTH_SHORT).show()
            return
        }

        // Generuojame HTML
        val timestamp = System.currentTimeMillis()
        val fileName = "app_$timestamp.html"
        val htmlContent = buildHtmlPage(title, url, description)

        val file = File(filesDir, fileName)
        file.writeText(htmlContent)

        // Įrašome į DB
        dbHelper.addApp(title, url, description, file.absolutePath)

        loadApps()
        adapter.notifyDataSetChanged()

        // Išvalome laukus
        titleInput.text.clear()
        urlInput.text.clear()
        descInput.text.clear()

        Toast.makeText(this, "✅ Appsas sukurtas!", Toast.LENGTH_SHORT).show()
    }

    private fun buildHtmlPage(title: String, url: String, description: String): String {
        val escapedTitle = title.replace("\"", "&quot;")
        val escapedDesc = description.replace("\"", "&quot;")
        return """
<!DOCTYPE html>
<html lang="lt">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$escapedTitle</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        .card {
            background: white;
            border-radius: 20px;
            padding: 40px;
            max-width: 400px;
            width: 100%;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        h1 {
            color: #333;
            font-size: 24px;
            margin-bottom: 12px;
            text-align: center;
        }
        .desc {
            color: #666;
            font-size: 14px;
            text-align: center;
            margin-bottom: 24px;
            line-height: 1.5;
        }
        .link-btn {
            display: block;
            background: #667eea;
            color: white;
            text-decoration: none;
            padding: 16px 24px;
            border-radius: 12px;
            text-align: center;
            font-size: 16px;
            font-weight: 600;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .link-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(102,126,234,0.4);
        }
        .domain {
            text-align: center;
            margin-top: 20px;
            font-size: 12px;
            color: #999;
        }
    </style>
</head>
<body>
    <div class="card">
        <h1>$escapedTitle</h1>
        <p class="desc">$escapedDesc</p>
        <a class="link-btn" href="$url" target="_blank">🌐 Atidaryti svetainę</a>
        <p class="domain">Sugeneruota per "Apps su nuoroda"</p>
    </div>
</body>
</html>
        """.trimIndent()
    }

    private fun loadApps() {
        appsList.clear()
        appsList.addAll(dbHelper.getAllApps())
    }

    private fun setupListView() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appsList.map { it.title })
        listView.adapter = adapter

        // Trumpas paspaudimas → atidaryti per WebView
        listView.setOnItemClickListener { _, _, position, _ ->
            val app = appsList[position]
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", "file://${app.htmlPath}")
                putExtra("title", app.title)
            }
            startActivity(intent)
        }

        // Ilgas paspaudimas → meniu (atidaryti naršykleje / redaguoti / trinti)
        listView.setOnItemLongClickListener { _, _, position, _ ->
            showAppMenu(position)
            true
        }
    }

    private fun showAppMenu(position: Int) {
        val app = appsList[position]
        val options = arrayOf("🌐 Atidaryti naršyklėje", "✏️ Redaguoti", "🗑️ Trinti")

        AlertDialog.Builder(this)
            .setTitle(app.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openInBrowser(app)
                    1 -> showEditDialog(app)
                    2 -> deleteApp(app, position)
                }
            }
            .show()
    }

    private fun openInBrowser(app: App) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("file://${app.htmlPath}"))
        startActivity(intent)
    }

    private fun showEditDialog(app: App) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Redaguoti: ${app.title}")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 24)
        }

        val titleEdit = EditText(this).apply {
            hint = "Pavadinimas"
            setText(app.title)
        }
        val urlEdit = EditText(this).apply {
            hint = "URL"
            setText(app.url)
        }
        val descEdit = EditText(this).apply {
            hint = "Aprašymas"
            setText(app.description)
        }

        layout.addView(titleEdit)
        layout.addView(urlEdit)
        layout.addView(descEdit)

        builder.setView(layout)
        builder.setPositiveButton("Išsaugoti") { _, _ ->
            val newTitle = titleEdit.text.toString().trim()
            val newUrl = urlEdit.text.toString().trim()
            val newDesc = descEdit.text.toString().trim()

            if (newTitle.isEmpty() || newUrl.isEmpty()) {
                Toast.makeText(this, "Pavadinimas ir URL būtini", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Atnaujinti DB
            dbHelper.updateApp(app.id, newTitle, newUrl, newDesc)

            // Perrašyti HTML failą
            val htmlContent = buildHtmlPage(newTitle, newUrl, newDesc)
            File(app.htmlPath).writeText(htmlContent)

            loadApps()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "✅ Atnaujinta!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Atšaukti", null)
        builder.show()
    }

    private fun deleteApp(app: App, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Trinti?")
            .setMessage("Ar tikrai nori ištrinti \"${app.title}\"?")
            .setPositiveButton("Taip, trinti") { _, _ ->
                dbHelper.deleteApp(app.id)
                File(app.htmlPath).delete()
                appsList.removeAt(position)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "🗑️ Ištrinta", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Atšaukti", null)
            .show()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
