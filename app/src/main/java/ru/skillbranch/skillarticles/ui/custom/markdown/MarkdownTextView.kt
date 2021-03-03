package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.graphics.withTranslation
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx

@SuppressLint("ViewConstructor", "AppCompatCustomView")
/*
Custom TextView для отображения поискового запроса с поддержкой подсветки (background color)
однострочного и многострочного вхождения
*/
class MarkdownTextView constructor(
    context: Context,
    fontSize: Float,
    mockHelper: SearchBgHelper? = null
) : TextView(context, null, 0), IMarkdownView {

    constructor(context: Context, fontSize: Float) : this(context, fontSize, null)

    override var fontSize: Float = fontSize
        set(value) {
            textSize = value
            field = value
        }
    override val spannableContent: Spannable
        get() = text as Spannable

    private val color = context.attrValue(R.attr.colorOnBackground)
    private val focusRect = Rect()

    private var searchBgHelper = SearchBgHelper(context) { top, bottom ->
        focusRect.set(0, top - context.dpToIntPx(56), width, bottom + context.dpToIntPx(56))
        //show rect on view with animation
        requestRectangleOnScreen(focusRect, false)
    }

    init {
        searchBgHelper = mockHelper ?: SearchBgHelper(context) { top, bottom ->
            //чтобы фокус поиска не прижимался к краю экрана
            focusRect.set(
                0,
                top - context.dpToIntPx(56),
                width,
                bottom + context.dpToIntPx(56)
            )
            //view скроллится (false - с анимацией, не мгновенно) так чтобы rect (фокус поиска) оставался видимым
            requestRectangleOnScreen(
                focusRect,
                false
            )
        }
        setTextColor(color)
        textSize = fontSize
        movementMethod = LinkMovementMethod.getInstance()//прокрутка и кликабельность ссылок
    }

    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            //canvas смещаем рисуем и возвращаем в прежнее положение
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                searchBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }
}