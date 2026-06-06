package com.example.weblinkapp

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
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

    private var pendingTitle = ""
    private var pendingUrl = ""
    private var pendingDesc = ""

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
            refreshList()
        } catch (e: Exception) {
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun onSukurtiClicked() {
        val title = titleInput.text.toString().trim()
        val url = urlInput.text.toString().trim()
        val desc = descInput.text.toString().trim()

        if (title.isEmpty()) { Toast.makeText(this, "Įvesk pavadinimą", Toast.LENGTH_SHORT).show(); return }
        if (url.isEmpty()) { Toast.makeText(this, "Įvesk URL", Toast.LENGTH_SHORT).show(); return }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "URL turi prasidėti http:// arba https://", Toast.LENGTH_SHORT).show(); return
        }

        pendingTitle = title
        pendingUrl = url
        pendingDesc = desc
        showIconPicker()
    }

    private fun showIconPicker() {
        try {
            val d = AlertDialog.Builder(this).setTitle("Pasirink ikoną").create()
            val grid = GridView(this).apply {
                numColumns = 4
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350)
                verticalSpacing = 12; horizontalSpacing = 12
                setPadding(30, 16, 30, 16)
                adapter = object : BaseAdapter() {
                    override fun getCount() = iconKeys.size
                    override fun getItem(p: Int) = iconKeys[p]
                    override fun getItemId(p: Int) = p.toLong()
                    override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                        val ll = if (v == null) LinearLayout(this@MainActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = AbsListView.LayoutParams(130, 150)
                            gravity = android.view.Gravity.CENTER
                        } else v as LinearLayout

                        var iv = ll.getChildAt(0) as? ImageView
                        var tv = ll.getChildAt(1) as? TextView
                        if (iv == null) {
                            iv = ImageView(this@MainActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(80, 80)
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                            tv = TextView(this@MainActivity).apply {
                                textSize = 11f
                                gravity = android.view.Gravity.CENTER
                            }
                            ll.addView(iv); ll.addView(tv)
                        }
                        iv.setImageResource(iconDrawables[p])
                        tv.text = iconNames[p]
                        return ll
                    }
                }
            }
            d.setView(grid)
            d.setButton(AlertDialog.BUTTON_NEGATIVE, "Atšaukti") { _, _ -> d.dismiss() }
            d.show()
            grid.setOnItemClickListener { _, _, pos, _ ->
                d.dismiss()
                saveNewApp(pendingTitle, pendingUrl, pendingDesc, iconKeys[pos])
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveNewApp(title: String, url: String, desc: String, iconKey: String) {
        try {
            val ts = System.currentTimeMillis()
            val fileName = "app_$ts.html"
            val html = buildHtmlPage(title, url, desc, iconKey)
            val file = File(filesDir, fileName)
            file.writeText(html)

            val id = dbHelper.addApp(title, url, desc, iconKey, file.absolutePath)
            if (id == -1L) {
                Toast.makeText(this, "Klaida išsaugant DB", Toast.LENGTH_SHORT).show()
                return
            }

            // Sukuriam darbalaukio nuorodą (shortcut)
            createDesktopShortcut(title, file.absolutePath, iconKey)

            loadApps()
            refreshList()
            titleInput.text.clear()
            urlInput.text.clear()
            descInput.text.clear()

            Toast.makeText(this, "✅ Sukurtas: $title", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Klaida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createDesktopShortcut(title: String, filePath: String, iconKey: String) {
        try {
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("filePath", filePath)
                putExtra("title", title)
                putExtra("standalone", true)    // atskiras "appsas", ne pagrindinis
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val iconResId = when (iconKey) {
                "ic_app_red" -> R.drawable.ic_app_red
                "ic_app_green" -> R.drawable.ic_app_green
                "ic_app_blue" -> R.drawable.ic_app_blue
                "ic_app_orange" -> R.drawable.ic_app_orange
                "ic_app_purple" -> R.drawable.ic_app_purple
                "ic_app_cyan" -> R.drawable.ic_app_cyan
                "ic_app_pink" -> R.drawable.ic_app_pink
                "ic_app_grey" -> R.drawable.ic_app_grey
                "ic_app_brown" -> R.drawable.ic_app_brown
                "ic_app_indigo" -> R.drawable.ic_app_indigo
                else -> R.drawable.ic_app_blue
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8+ - ShortcutManager
                val shortcutManager = getSystemService(ShortcutManager::class.java)
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                    val shortcut = ShortcutInfo.Builder(this, "app_$title")
                        .setShortLabel(title)
                        .setLongLabel(title)
                        .setIcon(Icon.createWithResource(this, iconResId))
                        .setIntent(intent)
                        .build()
                    shortcutManager.requestPinShortcut(shortcut, null)
                    return
                }
            }

            // Android 7.1+ - Pinned shortcut arba senesnis būdas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                val shortcutManager = getSystemService(ShortcutManager::class.java)
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                    val shortcut = ShortcutInfo.Builder(this, "app_$title")
                        .setShortLabel(title)
                        .setLongLabel(title)
                        .setIcon(Icon.createWithResource(this, iconResId))
                        .setIntent(intent)
                        .build()
                    shortcutManager.requestPinShortcut(shortcut, null)
                    return
                }
            }

            // Senesnis būdas (Android 4-7) - broadcast
            val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
                putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this@MainActivity, iconResId))
                putExtra("duplicate", false)
            }
            sendBroadcast(shortcutIntent)

        } catch (e: Exception) {
            // Jei nepavyko sukurti shortcut - pranešam bet neblokuojam
            e.printStackTrace()
            Toast.makeText(this, "Nuoroda nesukurta: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildHtmlPage(title: String, url: String, desc: String, iconKey: String): String {
        val bg = when (iconKey) {
            "ic_app_red" -> "#FF6B6B"; "ic_app_green" -> "#4CAF50"
            "ic_app_blue" -> "#2196F3"; "ic_app_orange" -> "#FF9800"
            "ic_app_purple" -> "#9C27B0"; "ic_app_cyan" -> "#00BCD4"
            "ic_app_pink" -> "#E91E63"; "ic_app_grey" -> "#607D8B"
            "ic_app_brown" -> "#795548"; "ic_app_indigo" -> "#3F51B5"
            else -> "#667EEA"
        }
        val t = title.replace("\"", "&quot;")
        val d = desc.replace("\"", "&quot;")
        return """
<!DOCTYPE html><html lang="lt"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>$t</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;
background:linear-gradient(135deg,$bg 0%,#333 100%);min-height:100vh;
display:flex;justify-content:center;align-items:center;padding:20px}
.card{background:white;border-radius:20px;padding:40px;max-width:400px;width:100%;
box-shadow:0 20px 60px rgba(0,0,0,.3)}
h1{color:#333;font-size:24px;margin-bottom:12px;text-align:center}
.desc{color:#666;font-size:14px;text-align:center;margin-bottom:24px}
.link-btn{display:block;background:$bg;color:white;text-decoration:none;
padding:16px 24px;border-radius:12px;text-align:center;font-size:16px;font-weight:600}
.link-btn:hover{transform:translateY(-2px);box-shadow:0 8px 24px ${bg}66}
.domain{text-align:center;margin-top:20px;font-size:12px;color:#999}
</style></head><body>
<div class="card"><h1>$t</h1><p class="desc">$d</p>
<a class="link-btn" href="$url" target="_blank">Atidaryti svetainę</a>
<p class="domain">Sugeneruota per "Apps su nuoroda"</p></div></body></html>"""
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
                putExtra("filePath", app.htmlPath)
                putExtra("title", app.title)
                putExtra("standalone", false)
            })
        }
        listView.setOnItemLongClickListener { _, _, pos, _ ->
            showMenu(pos); true
        }
    }

    private fun showMenu(pos: Int) {
        val app = appsList[pos]
        AlertDialog.Builder(this)
            .setTitle(app.title)
            .setIcon(app.getIconDrawableId())
            .setItems(arrayOf("Redaguoti", "Trinti")) { _, w ->
                when (w) {
                    0 -> editApp(app, pos)
                    1 -> deleteApp(app, pos)
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
        val d = AlertDialog.Builder(this).setTitle("Pasirink ikoną").create()
        val grid = GridView(this).apply {
            numColumns = 4
            verticalSpacing = 12; horizontalSpacing = 12
            setPadding(30, 16, 30, 16)
            adapter = object : BaseAdapter() {
                override fun getCount() = iconKeys.size
                override fun getItem(p: Int) = iconKeys[p]
                override fun getItemId(p: Int) = p.toLong()
                override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                    val ll = if (v == null) LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = AbsListView.LayoutParams(130, 150)
                        gravity = android.view.Gravity.CENTER
                    } else v as LinearLayout
                    var iv = ll.getChildAt(0) as? ImageView
                    var tv = ll.getChildAt(1) as? TextView
                    if (iv == null) {
                        iv = ImageView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(80, 80)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        tv = TextView(this@MainActivity).apply { textSize = 11f; gravity = android.view.Gravity.CENTER }
                        ll.addView(iv); ll.addView(tv)
                    }
                    iv.setImageResource(iconDrawables[p])
                    tv.text = iconNames[p]
                    return ll
                }
            }
        }
        d.setView(grid)
        d.setButton(AlertDialog.BUTTON_NEGATIVE, "Atšaukti") { _, _ -> d.dismiss() }
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
