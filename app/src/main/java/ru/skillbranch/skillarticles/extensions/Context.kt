package ru.skillbranch.skillarticles.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.github.ajalt.timberkt.Timber
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.e
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.NaterialToolbarExtensions.adjustLogo
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.CheckableImageView

fun Context.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics

    )
}

fun Context.dpToIntPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics
    ).toInt()
}

val Context.isNetworkAvailable: Boolean
    get() {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork?.run {
                val nc = cm.getNetworkCapabilities(this)
                nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            } ?: false
        } else {
            cm.activeNetworkInfo?.run { isConnectedOrConnecting } ?: false
        }
    }

tailrec fun Context.activity(): Activity? =
    when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> this.baseContext.activity()
        else -> {
            e(IllegalArgumentException("can't get activity from context: $this"))
            null
        }
    }


fun View.switchActivityMode(isDarkMode: Boolean) {
    val activity = context.activity()
    if (activity != null && activity is AppCompatActivity)

        activity.apply {
            val oldMode = delegate.localNightMode
            val newMode =
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (oldMode != newMode) {
                delegate.localNightMode = newMode

                d { "activity mode changed: isDarkMode=$isDarkMode" }
            }
        }
    else e(IllegalArgumentException("there is no AppCompatActivity; current activity=$activity"))
}

object NaterialToolbarExtensions {
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
}

object TimberExtensions {
    fun Timber.init() {
        plant(object : timber.log.Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return String.format(
                    "Timber %s: %s:%d (%s)",
                    super.createStackElementTag(element),
                    element.methodName,
                    element.lineNumber,
                    Thread.currentThread().name
                )
            }
        })
    }
}
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
            Timber.d { "logo is changed" }
            if (iconResOld == null) toolbar.adjustLogo()
        }
    }

    @JvmStatic
    @BindingAdapter("checked")
    fun bindChecked(view: CheckableImageView, isChecked: Boolean) {
        if (view.isChecked xor isChecked) {
            view.isChecked = isChecked
            Timber.d { "changed isChecked=$isChecked ($view)" }
        }
    }

    @JvmStatic
    @BindingAdapter("checked")
    fun bindSwitchChecked(view: SwitchMaterial, isCheckedOld: Boolean, isCheckedNew: Boolean) {
        //d { "view.isChecked=${view.isChecked}, isCheckedOld=$isCheckedOld, isCheckedNew=$isCheckedNew" }
        if (view.isChecked != isCheckedNew) {
            view.isChecked = isCheckedNew
            Timber.d { "changed isChecked=$isCheckedNew ($view)" }
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
            Timber.d { "changed isShow=$isShow" }
        }
    }

}


