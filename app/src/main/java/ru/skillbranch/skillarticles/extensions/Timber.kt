package ru.skillbranch.skillarticles.extensions

import com.github.ajalt.timberkt.Timber

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