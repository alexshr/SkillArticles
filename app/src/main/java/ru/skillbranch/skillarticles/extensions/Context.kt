package ru.skillbranch.skillarticles.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.e

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
            if(oldMode!=newMode) {
                delegate.localNightMode=newMode

                d { "activity mode changed: isDarkMode=$isDarkMode" }
            }
        }
    else e(IllegalArgumentException("there is no AppCompatActivity; current activity=$activity"))
}


