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

    private val color: Int = context.attrValue(R.attr.colorOnBackground)
    private val focusRect = Rect()

    private val searchBgHelper: SearchBgHelper//для отрисовки фона

    override var fontSize: Float = fontSize
        set(value) {
            textSize = value
            field = value
        }

    override val spannableContent: Spannable
        get() = text as Spannable

    init {
        searchBgHelper = mockHelper ?: SearchBgHelper(context) { top, bottom ->
            focusRect.set(0, top - context.dpToIntPx(56), width, bottom + context.dpToIntPx(56))//чтобы фокус поиска не прижимался к краю экрана
            requestRectangleOnScreen(focusRect, false)  //view скроллится (false - с анимацией, не мгновенно) так чтобы rect (фокус поиска) оставался видимым
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