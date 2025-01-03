package com.softcraft.rutaxpressapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.cloudinary.android.MediaManager

class RutaXpress : Application() {
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "djcfm4nd2",
            "api_key" to "897657815927312",
            "api_secret" to "6Af5mOu8kiKfn9MT-P3Ag6vXF1s"
        )
        MediaManager.init(this, config)
    }
}