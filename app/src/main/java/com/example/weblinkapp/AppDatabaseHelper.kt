package com.example.weblinkapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "AppsDB"
        private const val DB_VERSION = 2
        private const val TABLE_APPS = "apps"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_URL = "url"
        private const val COL_DESCRIPTION = "description"
        private const val COL_ICON = "icon"
        private const val COL_HTML_PATH = "htmlPath"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_APPS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT,
                $COL_URL TEXT,
                $COL_DESCRIPTION TEXT,
                $COL_ICON TEXT DEFAULT 'ic_app_blue',
                $COL_HTML_PATH TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_APPS ADD COLUMN $COL_ICON TEXT DEFAULT 'ic_app_blue'")
        } else {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_APPS")
            onCreate(db)
        }
    }

    fun addApp(title: String, url: String, description: String, iconRes: String, htmlPath: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, title)
            put(COL_URL, url)
            put(COL_DESCRIPTION, description)
            put(COL_ICON, iconRes)
            put(COL_HTML_PATH, htmlPath)
        }
        val id = db.insert(TABLE_APPS, null, values)
        db.close()
        return id
    }

    fun getAllApps(): List<App> {
        val apps = mutableListOf<App>()
        val db = readableDatabase
        val cursor = db.query(TABLE_APPS, null, null, null, null, null, "$COL_ID DESC")
        while (cursor.moveToNext()) {
            apps.add(
                App(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(COL_URL)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                    iconRes = cursor.getString(cursor.getColumnIndexOrThrow(COL_ICON)) ?: "ic_app_blue",
                    htmlPath = cursor.getString(cursor.getColumnIndexOrThrow(COL_HTML_PATH))
                )
            )
        }
        cursor.close()
        db.close()
        return apps
    }

    fun updateApp(id: Int, title: String, url: String, description: String, iconRes: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, title)
            put(COL_URL, url)
            put(COL_DESCRIPTION, description)
            put(COL_ICON, iconRes)
        }
        db.update(TABLE_APPS, values, "$COL_ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteApp(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_APPS, "$COL_ID=?", arrayOf(id.toString()))
        db.close()
    }
}
