package com.shuttlepulse.agility

import android.app.Application
import com.shuttlepulse.agility.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShuttlePulseApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppModule.provideCadenceSoundEngine(this)
        appScope.launch {
            AppModule.provideWorkoutRepository(this@ShuttlePulseApplication).ensureSeeded()
        }
    }
}
