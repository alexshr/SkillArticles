package ru.skillbranch.skillarticles.ui.custom

import android.text.TextPaint

/*Ничего не добавляет к SearchSpan
Однако отдельный класс нужен для content.getSpans<SearchFocusSpan> */
class SearchFocusSpan(
    private val bgColor: Int,
    private val fgColor: Int
) : SearchSpan(bgColor, fgColor) {

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.bgColor = bgColor
        textPaint.color = fgColor
    }
}