package fr.vferries.cuisine.data

import android.content.Intent

fun buildShareIntent(title: String, url: String): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n$url")
    }
