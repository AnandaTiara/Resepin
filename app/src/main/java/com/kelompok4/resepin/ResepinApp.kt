package com.kelompok4.resepin

import android.app.Application
import com.kelompok4.resepin.data.AppContainer

/** Titik awal aplikasi: menyiapkan AppContainer (Firebase dibuat otomatis oleh plugin google-services). */
class ResepinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
