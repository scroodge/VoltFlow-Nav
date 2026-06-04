package com.bridge.yandexbyd

import android.app.Application
import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class SetupHelperTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun accessibilityServiceComponent_matchesManifestService() {
        val cn = SetupHelper.accessibilityServiceComponent(app)
        assertEquals(app.packageName, cn.packageName)
        assertEquals("${app.packageName}.YandexA11yService", cn.className)
    }

}
