package com.bridge.yandexbyd

import android.app.Application
import android.content.Context

class VoltFlowApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrap(base))
    }
}
