package ru.skillbranch.skillarticles.extensions

import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.github.ajalt.timberkt.d
import com.google.android.material.appbar.MaterialToolbar

fun MaterialToolbar.adjustLogo() {
    //val logoView: ImageView? = getChildAt(2) as? ImageView
    logoView?.apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = layoutParams as Toolbar.LayoutParams
        lp.let {
            it.width = context.dpToIntPx(40)
            it.height = context.dpToIntPx(40)
            it.marginEnd = context.dpToIntPx(16)
            layoutParams = it
        }
        d { "logo was adjusted" }
    }



}

val MaterialToolbar.logoView: ImageView?
    get() {
        return children.firstOrNull {
            it is ImageView && it.drawable == logo
        } as? ImageView
    }