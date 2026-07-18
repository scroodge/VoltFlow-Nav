package com.bridge.yandexbyd

import android.app.Application
import android.content.Context

class VoltFlowApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        InstallEventLogger.append(
            this,
            "app_onCreate package=$packageName version=${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
        )
        InstallDiagnostics.dump(this, "application_onCreate")
    }
}
