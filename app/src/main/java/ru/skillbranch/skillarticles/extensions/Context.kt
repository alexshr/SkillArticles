package ru.skillbranch.skillarticles.extensions

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import ru.skillbranch.skillarticles.extensions.LogMode.isTest

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

fun Context.attrValue(@AttrRes attr: Int, typedValue: TypedValue = TypedValue()): Int {
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

//alexshr мое логгирование
inline val <reified T> T.TAG: String
    get() = T::class.java.simpleName + " " + Thread.currentThread().stackTrace[2].methodName//+" "+Thread.currentThread().stackTrace[2].lineNumber

inline fun <reified T> T.logv(message: String) =
    if (!isTest) Log.v(TAG, message) else println("$TAG $message")

inline fun <reified T> T.logi(message: String) =
    if (!isTest) Log.i(TAG, message) else println("$TAG $message")

inline fun <reified T> T.logw(message: String) =
    if (!isTest) Log.w(TAG, message) else println("$TAG $message")

inline fun <reified T> T.logd(message: String) =
    if (!isTest) Log.d(TAG, message) else println("$TAG $message")

inline fun <reified T> T.loge(message: String) =
    if (!isTest) Log.e(TAG, message) else println("$TAG $message")

inline fun <reified T> T.loge(message: String, err: Throwable) =
    if (!isTest) Log.e(TAG, message, err) else {
        println("$TAG $message")
        err.printStackTrace()
    }

inline fun <reified T> T.logd() = logd(" ")

object LogMode {
    var isTest = false
}