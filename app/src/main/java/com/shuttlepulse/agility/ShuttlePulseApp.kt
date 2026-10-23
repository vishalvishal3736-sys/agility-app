package com.shuttlepulse.agility

import android.app.Application
import com.shuttlepulse.agility.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

open class ShuttlePulseApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppModule.provideCadenceSoundEngine(this)
        appScope.launch {
            AppModule.provideWorkoutRepository(this@ShuttlePulseApp).ensureSeeded()
        }
    }
}

typealias ShuttlePulseApplication = ShuttlePulseApp
