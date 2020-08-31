package ru.skillbranch.skillarticles;

import android.app.Application
import com.github.ajalt.timberkt.Timber
import ru.skillbranch.skillarticles.extensions.init

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode())
        Timber.init()
    }
}
