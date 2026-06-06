package com.example.weblinkapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class AppListAdapter(private val context: Context, private val apps: List<App>) : BaseAdapter() {

    override fun getCount() = apps.size
    override fun getItem(pos: Int) = apps[pos]
    override fun getItemId(pos: Int) = apps[pos].id.toLong()

    override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
        val ll: LinearLayout
        val icon: ImageView
        val title: TextView
        val desc: TextView

        if (v == null) {
            ll = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 12, 16, 12)
            }
            icon = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(56, 56)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            val tvLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(16, 0, 0, 0)
            }
            title = TextView(context).apply {
                textSize = 16f
                setTextColor(0xFF333333.toInt())
            }
            desc = TextView(context).apply {
                textSize = 13f
                setTextColor(0xFF999999.toInt())
            }
            tvLayout.addView(title)
            tvLayout.addView(desc)
            ll.addView(icon)
            ll.addView(tvLayout)
            ll.tag = Pair(icon, Pair(title, desc))
        } else {
            ll = v as LinearLayout
            val tag = ll.tag as Pair<ImageView, Pair<TextView, TextView>>
            icon = tag.first
            title = tag.second.first
            desc = tag.second.second
        }

        val app = apps[pos]
        icon.setImageResource(app.getIconDrawableId())
        title.text = app.title
        desc.text = if (app.description.isNotEmpty()) app.description else app.url

        return ll
    }
}
