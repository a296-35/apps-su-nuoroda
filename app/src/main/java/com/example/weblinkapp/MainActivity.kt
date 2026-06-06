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

    private var pendingTitle: String = ""
    private var pendingUrl: String = ""
    private var pendingDesc: String = ""

    private val iconNames = arrayOf(
        "Raudona", "Žalia", "Mėlyna", "Oranžinė", "Violetinė",
        "Cian", "Rožinė", "Pilka", "Ruda", "Indigo"
    )
    private val iconKeys = arrayOf(
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

            createBtn.setOnClickListener { onSukurtiClicked() }
            loadApps()
            setupListView()
        } catch (e: Exception) {
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun onSukurtiClicked() {
        val title = titleInput.text.toString().trim()
        val url = urlInput.text.toString().trim()
        val desc = descInput.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Įvesk pavadinimą", Toast.LENGTH_SHORT).show()
            return
        }
        if (url.isEmpty()) {
            Toast.makeText(this, "Įvesk URL", Toast.LENGTH_SHORT).show()
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "URL turi prasidėti http:// arba https://", Toast.LENGTH_SHORT).show()
            return
        }

        // Išsaugom duomenis ir rodom ikonų pasirinkimą
        pendingTitle = title
        pendingUrl = url
        pendingDesc = desc
        showIconPicker()
    }

    private fun showIconPicker() {
        try {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Pasirink ikoną")

            // Sukuriam paprastą LinearLayout su TextView + GridView
            val ll = LinearLayout(this)
            ll.orientation = LinearLayout.VERTICAL
            ll.setPadding(20, 16, 20, 16)

            val hint = TextView(this)
            hint.text = "Skirta: ${pendingTitle}"
            hint.textSize = 14f
            hint.setTextColor(0xFF666666.toInt())
            ll.addView(hint)

            val grid = GridView(this)
            grid.numColumns = 4
            grid.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300
            )
            grid.verticalSpacing = 8
            grid.horizontalSpacing = 8
            grid.setPadding(0, 12, 0, 0)

            // Paprastas adapteris be inflate - naudojam ImageView tiesiogiai
            grid.adapter = object : BaseAdapter() {
                override fun getCount() = iconKeys.size
                override fun getItem(pos: Int) = iconKeys[pos]
                override fun getItemId(pos: Int) = pos.toLong()
                override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
                    val iv = if (v == null) {
                        val img = ImageView(this@MainActivity)
                        img.layoutParams = GridView.LayoutParams(120, 120)
                        img.setPadding(4, 4, 4, 4)
                        img.scaleType = ImageView.ScaleType.FIT_CENTER
                        img
                    } else v as ImageView
                    iv.setImageResource(iconDrawables[pos])
                    return iv
                }
            }

            ll.addView(grid)
            dialog.setView(ll)
            dialog.setNegativeButton("Atšaukti", null)

            val d = dialog.create()
            d.show()

            grid.setOnItemClickListener { _, _, pos, _ ->
                d.dismiss()
                Toast.makeText(this, "✅ Kuriama...", Toast.LENGTH_SHORT).show()
                saveNewApp(pendingTitle, pendingUrl, pendingDesc, iconKeys[pos])
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveNewApp(title: String, url: String, desc: String, iconKey: String) {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "app_$timestamp.html"
            val html = buildHtmlPage(title, url, desc, iconKey)
            File(filesDir, fileName).writeText(html)

            val id = dbHelper.addApp(title, url, desc, iconKey, File(filesDir, fileName).absolutePath)
            if (id == -1L) {
                Toast.makeText(this, "Klaida išsaugant DB", Toast.LENGTH_SHORT).show()
                return
            }

            loadApps()
            refreshList()
            titleInput.text.clear()
            urlInput.text.clear()
            descInput.text.clear()

            Toast.makeText(this, "✅ Sukurtas: $title", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun buildHtmlPage(title: String, url: String, desc: String, iconKey: String): String {
        val bg = when (iconKey) {
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
        val safeTitle = title.replace("\"", "&quot;")
        val safeDesc = desc.replace("\"", "&quot;")
        return """
<!DOCTYPE html>
<html lang="lt"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>$safeTitle</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:linear-gradient(135deg,$bg 0%,#333 100%);min-height:100vh;display:flex;justify-content:center;align-items:center;padding:20px}
.card{background:white;border-radius:20px;padding:40px;max-width:400px;width:100%;box-shadow:0 20px 60px rgba(0,0,0,.3)}
h1{color:#333;font-size:24px;margin-bottom:12px;text-align:center}
.desc{color:#666;font-size:14px;text-align:center;margin-bottom:24px;line-height:1.5}
.link-btn{display:block;background:$bg;color:white;text-decoration:none;padding:16px 24px;border-radius:12px;text-align:center;font-size:16px;font-weight:600}
.link-btn:hover{transform:translateY(-2px);box-shadow:0 8px 24px ${bg}66}
.domain{text-align:center;margin-top:20px;font-size:12px;color:#999}
</style></head><body>
<div class="card">
<h1>$safeTitle</h1>
<p class="desc">$safeDesc</p>
<a class="link-btn" href="$url" target="_blank">Atidaryti svetainę</a>
<p class="domain">Sugeneruota per "Apps su nuoroda"</p>
</div></body></html>"""
    }

    private fun loadApps() {
        appsList.clear()
        appsList.addAll(dbHelper.getAllApps())
    }

    private fun refreshList() {
        adapter = AppListAdapter(this, appsList)
        listView.adapter = adapter
        val empty = appsList.isEmpty()
        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        listView.visibility = if (empty) View.GONE else View.VISIBLE

        listView.setOnItemClickListener { _, _, pos, _ ->
            val app = appsList[pos]
            startActivity(Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", "file://${app.htmlPath}")
                putExtra("title", app.title)
            })
        }
        listView.setOnItemLongClickListener { _, _, pos, _ ->
            showMenu(pos); true
        }
    }

    private fun setupListView() = refreshList()

    private fun showMenu(pos: Int) {
        val app = appsList[pos]
        AlertDialog.Builder(this)
            .setTitle(app.title)
            .setIcon(app.getIconDrawableId())
            .setItems(arrayOf("Atidaryti naršyklėje", "Redaguoti", "Trinti")) { _, w ->
                when (w) {
                    0 -> try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("file://${app.htmlPath}")))
                    } catch (_: Exception) {
                        Toast.makeText(this, "Nepavyko atidaryti", Toast.LENGTH_SHORT).show()
                    }
                    1 -> editApp(app, pos)
                    2 -> deleteApp(app, pos)
                }
            }.show()
    }

    private fun editApp(app: App, pos: Int) {
        val ll = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 24)
        }
        val etTitle = EditText(this).apply { hint = "Pavadinimas"; setText(app.title) }
        val etUrl = EditText(this).apply { hint = "URL"; setText(app.url) }
        val etDesc = EditText(this).apply { hint = "Aprašymas"; setText(app.description) }
        ll.addView(etTitle); ll.addView(etUrl); ll.addView(etDesc)

        AlertDialog.Builder(this)
            .setTitle("Redaguoti")
            .setView(ll)
            .setNeutralButton("Keisti ikoną") { _, _ ->
                pickEditIcon(app, pos, etTitle.text.toString(), etUrl.text.toString(), etDesc.text.toString())
            }
            .setPositiveButton("Išsaugoti") { _, _ ->
                val t = etTitle.text.toString().trim()
                val u = etUrl.text.toString().trim()
                val d = etDesc.text.toString().trim()
                if (t.isEmpty() || u.isEmpty()) {
                    Toast.makeText(this, "Pavadinimas ir URL būtini", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                try {
                    dbHelper.updateApp(app.id, t, u, d, app.iconRes)
                    File(app.htmlPath).writeText(buildHtmlPage(t, u, d, app.iconRes))
                    loadApps(); refreshList()
                    Toast.makeText(this, "Atnaujinta", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Atšaukti", null).show()
    }

    private fun pickEditIcon(app: App, pos: Int, title: String, url: String, desc: String) {
        val grid = GridView(this).apply {
            numColumns = 4; layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300)
            verticalSpacing = 8; horizontalSpacing = 8; setPadding(20, 16, 20, 16)
            adapter = object : BaseAdapter() {
                override fun getCount() = iconKeys.size
                override fun getItem(p: Int) = iconKeys[p]
                override fun getItemId(p: Int) = p.toLong()
                override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                    val iv = if (v == null) ImageView(this@MainActivity).apply {
                        layoutParams = GridView.LayoutParams(120, 120)
                        setPadding(4, 4, 4, 4); scaleType = ImageView.ScaleType.FIT_CENTER
                    } else v as ImageView
                    iv.setImageResource(iconDrawables[p])
                    return iv
                }
            }
        }
        val d = AlertDialog.Builder(this).setTitle("Pasirink ikoną").setView(grid)
            .setNegativeButton("Atšaukti", null).create()
        d.show()
        grid.setOnItemClickListener { _, _, p, _ ->
            d.dismiss()
            val newIcon = iconKeys[p]
            try {
                dbHelper.updateApp(app.id, title, url, desc, newIcon)
                File(app.htmlPath).writeText(buildHtmlPage(title, url, desc, newIcon))
                loadApps(); refreshList()
                Toast.makeText(this, "Ikona pakeista", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteApp(app: App, pos: Int) {
        AlertDialog.Builder(this)
            .setTitle("Trinti?")
            .setMessage("Ištrinti \"${app.title}\"?")
            .setPositiveButton("Taip") { _, _ ->
                try {
                    dbHelper.deleteApp(app.id); File(app.htmlPath).delete()
                    appsList.removeAt(pos); refreshList()
                    Toast.makeText(this, "Ištrinta", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Ne", null).show()
    }

    override fun onDestroy() {
        try { dbHelper.close() } catch (_: Exception) {}
        super.onDestroy()
    }
}
