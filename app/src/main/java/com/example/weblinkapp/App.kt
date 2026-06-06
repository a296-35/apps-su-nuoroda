package com.example.weblinkapp

data class App(
    val id: Int,
    val title: String,
    val url: String,
    val description: String,
    val iconRes: String,       // pvz "ic_app_red"
    val htmlPath: String
) {
    fun getIconDrawableId(): Int {
        return when (iconRes) {
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
    }
}
