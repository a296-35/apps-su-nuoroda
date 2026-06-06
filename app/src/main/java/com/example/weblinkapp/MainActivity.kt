package com.example.weblinkapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var appsList: MutableList<App>
    private lateinit var adapter: AppListAdapter

    private lateinit var titleInput: EditText
    private lateinit var urlInput: EditText
    private lateinit var descInput: EditText
    private lateinit var createBtn: Button
    private lateinit var listView: ListView
    private lateinit var emptyView: TextView

    private var selectedIcon: String = "ic_app_blue"

    // Ikonų sąrašas dialogui
    private val iconNames = arrayOf(
        "Raudona", "Žalia", "Mėlyna", "Oranžinė", "Violetinė",
        "Cian", "Rožinė", "Pilka", "Ruda", "Indigo"
    )
    private val iconResArray = arrayOf(
        "ic_app_red", "ic_app_green", "ic_app_blue", "ic_app_orange", "ic_app_purple",
        "ic_app_cyan", "ic_app_pink", "ic_app_grey", "ic_app_brown", "ic_app_indigo"
    )
    private val iconDrawables = intArrayOf(
        R.drawable.ic_app_red, R.drawable.ic_app_green, R.drawable.ic_app_blue,
        R.drawable.ic_app_orange, R.drawable.ic_app_purple, R.drawable.ic_app_cyan,
        R.drawable.ic_app_pink, R.drawable.ic_app_grey, R.drawable.ic_app_brown,
        R.drawable.ic_app_indigo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            dbHelper = AppDatabaseHelper(this)
            appsList = mutableListOf()

            titleInput = findViewById(R.id.titleEditText)
            urlInput = findViewById(R.id.urlEditText)
            descInput = findViewById(R.id.descriptionEditText)
            createBtn = findViewById(R.id.createButton)
            listView = findViewById(R.id.appsListView)
            emptyView = findViewById(R.id.emptyView)

            createBtn.setOnClickListener { createNewApp() }

            loadApps()
            setupListView()
        } catch (e: Exception) {
            Toast.makeText(this, "Klaida paleidžiant: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNewApp() {
        try {
            val title = titleInput.text.toString().trim()
            val url = urlInput.text.toString().trim()
            val description = descInput.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "❌ Įvesk pavadinimą", Toast.LENGTH_SHORT).show()
                return
            }
            if (url.isEmpty()) {
                Toast.makeText(this, "❌ Įvesk URL", Toast.LENGTH_SHORT).show()
                return
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Toast.makeText(this, "❌ URL turi prasidėti http:// arba https://", Toast.LENGTH_SHORT).show()
                return
            }

            // Rodyti ikonų pasirinkimo dialogą
            showIconPickerDialog(title, url, description)
        } catch (e: Exception) {
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showIconPickerDialog(title: String, url: String, description: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pasirink ikoną")

        // Grid su ikonomis
        val grid = GridView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            numColumns = 4
            setPadding(40, 20, 40, 20)
            adapter = object : ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, iconNames) {
                override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
                    val view = v ?: LayoutInflater.from(context)
                        .inflate(android.R.layout.activity_list_item, parent, false)
                    val img = view.findViewById<ImageView>(android.R.id.icon)
                    val txt = view.findViewById<TextView>(android.R.id.text1)
                    img?.setImageResource(iconDrawables[pos])
                    img?.layoutParams = ViewGroup.LayoutParams(96, 96)
                    txt?.text = iconNames[pos]
                    txt?.textSize = 11f
                    return view
                }
            }
        }

        builder.setView(grid)
        builder.setNegativeButton("Atšaukti", null)

        val dialog = builder.create()
        dialog.show()

        grid.setOnItemClickListener { _, _, pos, _ ->
            selectedIcon = iconResArray[pos]
            dialog.dismiss()
            // Sukurti appsą pasirinkus ikoną
            saveNewApp(title, url, description, selectedIcon)
        }
    }

    private fun saveNewApp(title: String, url: String, description: String, iconRes: String) {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "app_$timestamp.html"
            val htmlContent = buildHtmlPage(title, url, description, iconRes)

            val file = File(filesDir, fileName)
            file.writeText(htmlContent)

            val id = dbHelper.addApp(title, url, description, iconRes, file.absolutePath)
            if (id == -1L) {
                Toast.makeText(this, "❌ Klaida įrašant į duomenų bazę", Toast.LENGTH_SHORT).show()
                return
            }

            loadApps()
            refreshList()

            // Išvalom laukus
            titleInput.text.clear()
            urlInput.text.clear()
            descInput.text.clear()
            selectedIcon = "ic_app_blue"

            Toast.makeText(this, "✅ \"$title\" sukurtas!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun buildHtmlPage(title: String, url: String, description: String, iconRes: String): String {
        val safeTitle = title.replace("\"", "&quot;")
        val safeDesc = description.replace("\"", "&quot;")
        val iconColor = when (iconRes) {
            "ic_app_red" -> "#FF6B6B"
            "ic_app_green" -> "#4CAF50"
            "ic_app_blue" -> "#2196F3"
            "ic_app_orange" -> "#FF9800"
            "ic_app_purple" -> "#9C27B0"
            "ic_app_cyan" -> "#00BCD4"
            "ic_app_pink" -> "#E91E63"
            "ic_app_grey" -> "#607D8B"
            "ic_app_brown" -> "#795548"
            "ic_app_indigo" -> "#3F51B5"
            else -> "#667EEA"
        }
        return """
<!DOCTYPE html>
<html lang="lt">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$safeTitle</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, $iconColor 0%, #333 100%);
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
        .icon-circle {
            width: 80px; height: 80px;
            background: $iconColor;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 36px;
            color: white;
        }
        h1 { color: #333; font-size: 24px; margin-bottom: 12px; text-align: center; }
        .desc { color: #666; font-size: 14px; text-align: center; margin-bottom: 24px; line-height: 1.5; }
        .link-btn {
            display: block;
            background: $iconColor;
            color: white; text-decoration: none;
            padding: 16px 24px; border-radius: 12px;
            text-align: center; font-size: 16px; font-weight: 600;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .link-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px ${iconColor}66; }
        .domain { text-align: center; margin-top: 20px; font-size: 12px; color: #999; }
    </style>
</head>
<body>
    <div class="card">
        <div class="icon-circle">🔗</div>
        <h1>$safeTitle</h1>
        <p class="desc">$safeDesc</p>
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

    private fun refreshList() {
        adapter = AppListAdapter(this, appsList)
        listView.adapter = adapter
        emptyView.visibility = if (appsList.isEmpty()) View.VISIBLE else View.GONE
        listView.visibility = if (appsList.isEmpty()) View.GONE else View.VISIBLE

        listView.setOnItemClickListener { _, _, position, _ ->
            val app = appsList[position]
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", "file://${app.htmlPath}")
                putExtra("title", app.title)
            }
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            showAppMenu(position)
            true
        }
    }

    private fun setupListView() {
        refreshList()
    }

    private fun showAppMenu(position: Int) {
        val app = appsList[position]
        val options = arrayOf("🌐 Atidaryti naršyklėje", "✏️ Redaguoti", "🗑️ Trinti")

        AlertDialog.Builder(this)
            .setTitle(app.title)
            .setIcon(app.getIconDrawableId())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openInBrowser(app)
                    1 -> showEditDialog(app, position)
                    2 -> deleteApp(app, position)
                }
            }
            .show()
    }

    private fun openInBrowser(app: App) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("file://${app.htmlPath}"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Nepavyko atidaryti naršyklėje", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(app: App, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Redaguoti")

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

        // Ikonų keitimo mygtukas
        builder.setNeutralButton("🎨 Keisti ikoną") { _, _ ->
            showEditIconPicker(app, position, titleEdit.text.toString(), urlEdit.text.toString(), descEdit.text.toString())
        }
        builder.setPositiveButton("Išsaugoti") { _, _ ->
            val newTitle = titleEdit.text.toString().trim()
            val newUrl = urlEdit.text.toString().trim()
            val newDesc = descEdit.text.toString().trim()

            if (newTitle.isEmpty() || newUrl.isEmpty()) {
                Toast.makeText(this, "Pavadinimas ir URL būtini", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            try {
                dbHelper.updateApp(app.id, newTitle, newUrl, newDesc, app.iconRes)
                val htmlContent = buildHtmlPage(newTitle, newUrl, newDesc, app.iconRes)
                File(app.htmlPath).writeText(htmlContent)

                loadApps()
                refreshList()
                Toast.makeText(this, "✅ Atnaujinta!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Atšaukti", null)
        builder.show()
    }

    private fun showEditIconPicker(app: App, position: Int, title: String, url: String, desc: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pasirink ikoną")

        val grid = GridView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            numColumns = 4
            setPadding(40, 20, 40, 20)
            adapter = object : ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, iconNames) {
                override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
                    val view = v ?: LayoutInflater.from(context)
                        .inflate(android.R.layout.activity_list_item, parent, false)
                    val img = view.findViewById<ImageView>(android.R.id.icon)
                    val txt = view.findViewById<TextView>(android.R.id.text1)
                    img?.setImageResource(iconDrawables[pos])
                    img?.layoutParams = ViewGroup.LayoutParams(96, 96)
                    txt?.text = iconNames[pos]
                    txt?.textSize = 11f
                    return view
                }
            }
        }

        builder.setView(grid)
        builder.setNegativeButton("Atšaukti", null)
        val dialog = builder.create()
        dialog.show()

        grid.setOnItemClickListener { _, _, pos, _ ->
            val newIcon = iconResArray[pos]
            dialog.dismiss()
            try {
                dbHelper.updateApp(app.id, title, url, desc, newIcon)
                val htmlContent = buildHtmlPage(title, url, desc, newIcon)
                File(app.htmlPath).writeText(htmlContent)
                loadApps()
                refreshList()
                Toast.makeText(this, "✅ Ikona pakeista!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteApp(app: App, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Trinti?")
            .setIcon(app.getIconDrawableId())
            .setMessage("Ar tikrai nori ištrinti \"${app.title}\"?")
            .setPositiveButton("Taip, trinti") { _, _ ->
                try {
                    dbHelper.deleteApp(app.id)
                    File(app.htmlPath).delete()
                    appsList.removeAt(position)
                    refreshList()
                    Toast.makeText(this, "🗑️ Ištrinta", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Atšaukti", null)
            .show()
    }

    override fun onDestroy() {
        try { dbHelper.close() } catch (_: Exception) {}
        super.onDestroy()
    }
}
