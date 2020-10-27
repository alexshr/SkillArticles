package ru.skillbranch.skillarticles.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.extensions.logd
import ru.skillbranch.skillarticles.markdown.spans.*

class MarkdownBuilder(context: Context) {

    private val colorPrimary: Int = context.attrValue(R.attr.colorPrimary)
    private val colorSurface: Int = context.attrValue(R.attr.colorSurface)
    private val colorDivider: Int = context.getColor(R.color.color_divider)
    private val colorSecondary: Int = context.attrValue(R.attr.colorSecondary)
    private val colorOnSurface: Int = context.attrValue(R.attr.colorOnSurface)

    private val gap: Float = context.dpToPx(8)
    private val ruleWidth: Float = context.dpToPx(2)
    private val strikeWidth: Float = context.dpToPx(4)
    private val bulletRadius: Float = context.dpToPx(4)
    private val cornerRadius: Float = context.dpToPx(8)

    private val linkIcon = ContextCompat.getDrawable(context, R.drawable.ic_link_black_24dp)!!

    private val headerMarginTop: Float = context.dpToPx(12)
    private val headerMarginBottom: Float = context.dpToPx(8)

    // alexshr 5
    // перевод markdown string -> spannedString
    fun markdownToSpan(string: String): SpannedString {
        logd("markdown string:\n$string\n-------------------------------------")
        val elements = MarkdownParser.parse(string).elements
        logd("markdown elements:\n$elements\n----------------------------------")
        val spannedString= buildSpannedString {
            elements.forEach {
                buildElement(it, this)
            }
        }
        logd("spannedString:\n$spannedString\n---------------------------------")
        return spannedString
    }

    private fun buildElement(element: Element, builder: SpannableStringBuilder): CharSequence {
        return builder.apply {
            when (element) {
                is Element.Text -> {
                    append(element.text)
                }
                is Element.UnorderedListItem -> {
                    inSpans(UnorderedListSpan(gap, bulletRadius, colorSecondary)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Quote -> {
                    inSpans(
                        BlockquotesSpan(gap, strikeWidth, colorSecondary),
                        StyleSpan(Typeface.ITALIC)
                    ) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Header -> {
                    inSpans(
                        HeaderSpan(
                            element.level,
                            colorPrimary,
                            colorDivider,
                            headerMarginTop,
                            headerMarginBottom
                        )
                    ) {
                        append(element.text)
                    }
                }
                is Element.Italic -> {
                    inSpans(StyleSpan(Typeface.ITALIC)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Bold -> {
                    inSpans(StyleSpan(Typeface.BOLD)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Strike -> {
                    inSpans(StrikethroughSpan()) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.Rule -> {
                    inSpans(HorizontalRuleSpan(ruleWidth, colorDivider)) {
                        append(element.text)
                    }
                }
                is Element.InlineCode -> {
                    inSpans(InlineCodeSpan(colorOnSurface, colorSurface, cornerRadius, gap)) {
                        append(element.text)
                    }
                }
                is Element.Link -> {
                    inSpans(
                        IconLinkSpan(linkIcon, colorSecondary, gap, colorPrimary, strikeWidth),
                        URLSpan(element.link)
                    ) {
                        append(element.text)
                    }
                }
                is Element.OrderedListItem -> {
                    inSpans(OrderedListSpan(gap, element.order, colorSecondary)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                is Element.BlockCode -> {
                    inSpans(
                        BlockCodeSpan(
                            colorOnSurface,
                            colorSurface,
                            cornerRadius,
                            gap,
                            element.type
                        )
                    ) {
                        append(element.text)
                    }
                }

                //comment this because of sealed classes
                /*else -> {
                    append(element.text)
                }*/
            }
        }
    }
}