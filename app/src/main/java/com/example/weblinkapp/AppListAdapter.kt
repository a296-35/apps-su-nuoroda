package com.example.weblinkapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class AppListAdapter(context: Context, private val apps: List<App>) :
    ArrayAdapter<App>(context, 0, apps) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.activity_list_item, parent, false)

        val app = apps[position]

        val icon = view.findViewById<ImageView>(android.R.id.icon)
        val title = view.findViewById<TextView>(android.R.id.text1)
        val desc = view.findViewById<TextView>(android.R.id.text2)

        icon?.setImageResource(app.getIconDrawableId())
        title?.text = app.title
        desc?.text = if (app.description.isNotEmpty()) app.description else app.url

        return view
    }
}
