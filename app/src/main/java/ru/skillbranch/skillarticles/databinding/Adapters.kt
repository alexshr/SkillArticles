package ru.skillbranch.skillarticles.databinding

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.github.ajalt.timberkt.Timber.d
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.adjustLogo
import ru.skillbranch.skillarticles.extensions.switchActivityMode
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.CheckableImageView


object Adapters {
    @JvmStatic
    @BindingAdapter("android:onClick")
    fun bindClick(btn: CheckableImageView, action: Runnable) {
        btn.setOnClickListener { v: View -> action.run() }
        //d { " " }
    }

    @JvmStatic
    @BindingAdapter("onCheckedChange")
    fun bindSwitch(sw: SwitchMaterial, action: Runnable) {
        sw.setOnCheckedChangeListener() { v: View, isChecked ->
            action.run()
            //sw.switchActivityMode(isChecked)
            //d { "sw.isChecked=${sw.isChecked}, isChecked = $isChecked" }
        }

    }

    @JvmStatic
    @BindingAdapter("app:logo")
    fun bindLogo(toolbar: MaterialToolbar, iconResOld: Any?, iconResNew: Any?) {
        //d { "iconResOld=$iconResOld, iconResNew=$iconResNew" }
        if (iconResOld == iconResNew)
        //d { "new logo is the same" }
        else {
            val res = iconResNew ?: R.drawable.logo_placeholder
            toolbar.logo = ContextCompat.getDrawable(toolbar.context.applicationContext, res as Int)
            d { "logo is changed" }
            if (iconResOld == null) toolbar.adjustLogo()
        }
    }

    @JvmStatic
    @BindingAdapter("checked")
    fun bindChecked(view: CheckableImageView, isChecked: Boolean) {
        if (view.isChecked xor isChecked) {
            view.isChecked = isChecked
            d { "changed isChecked=$isChecked ($view)" }
        }
    }

    @JvmStatic
    @BindingAdapter("checked")
    fun bindSwitchChecked(view: SwitchMaterial, isCheckedOld: Boolean, isCheckedNew: Boolean) {
        //d { "view.isChecked=${view.isChecked}, isCheckedOld=$isCheckedOld, isCheckedNew=$isCheckedNew" }
        if (view.isChecked != isCheckedNew) {
            view.isChecked = isCheckedNew
            d { "changed isChecked=$isCheckedNew ($view)" }
        }
        if (isCheckedOld xor isCheckedNew) {
            view.switchActivityMode(view.isChecked)
        }

    }

    @JvmStatic
    @BindingAdapter("show")
    fun bindShow(view: ArticleSubmenu, isShow: Boolean) {
        if (view.isOpen xor isShow) {
            view.show(isShow)
            d { "changed isShow=$isShow" }
        }
    }

}


