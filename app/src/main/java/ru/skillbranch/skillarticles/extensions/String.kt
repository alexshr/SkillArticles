package ru.skillbranch.skillarticles.extensions

import java.util.*

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    return if (this == null || substr.isEmpty()) listOf()
    else Regex(if (ignoreCase) substr.toLowerCase(Locale.getDefault()) else substr)
        .findAll(if (ignoreCase) this.toLowerCase(Locale.getDefault()) else this)
        .map { it.range.first }
        .toList()
}

/* как вариант
inline fun <reified T> Spannable.removeSpans() {
    this
        .getSpans<SearchSpan>()
        .forEach { this.removeSpan(it) }
}*/
