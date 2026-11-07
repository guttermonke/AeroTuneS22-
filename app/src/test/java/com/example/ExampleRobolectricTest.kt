package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.cache.MetricCacheManager
import com.example.hardware.GalaxyS22Profile
import com.example.model.DeviceThermals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AeroTune S22+", appName)
    }

    @Test
    fun `test metric cache manager TTL and ring buffer`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheManager = MetricCacheManager(context)

        val thermals = DeviceThermals(temperatureC = 34.2f)
        cacheManager.putCachedThermals(thermals)

        val retrieved = cacheManager.getCachedThermals()
        assertNotNull(retrieved)
        assertEquals(34.2f, retrieved?.temperatureC ?: 0f, 0.01f)

        // Verify history flow has recorded point
        assertEquals(1, cacheManager.historyFlow.value.size)
        assertEquals(34.2f, cacheManager.historyFlow.value[0], 0.01f)

        cacheManager.destroy()
    }

    @Test
    fun `test Galaxy S22 profile constants`() {
        assertEquals("Galaxy S22+ (Plus)", GalaxyS22Profile.DEVICE_NAME)
        assertEquals(8, GalaxyS22Profile.RAM_TOTAL_GB)
        assertEquals(4500, GalaxyS22Profile.BATTERY_CAPACITY_MAH)
        assertTrue(GalaxyS22Profile.OPTIMIZATION_TIPS.isNotEmpty())
    }
}
