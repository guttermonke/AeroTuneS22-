package com.example.hardware

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.example.cache.MetricCacheManager
import com.example.model.DeviceThermals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThermalMonitor(
    private val context: Context,
    private val cacheManager: MetricCacheManager
) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val _thermals = MutableStateFlow(DeviceThermals())
    val thermals: StateFlow<DeviceThermals> = _thermals.asStateFlow()

    init {
        refreshThermals(force = true)
    }

    fun refreshThermals(force: Boolean = false): DeviceThermals {
        if (!force) {
            val cached = cacheManager.getCachedThermals()
            if (cached != null) {
                _thermals.value = cached
                return cached
            }
        }

        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        var tempC = 31.5f
        var level = 85
        var isCharging = false
        var plugType = "Discharging"
        var voltage = 4100
        var healthStr = "Good"

        if (batteryIntent != null) {
            val rawTemp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 315)
            tempC = rawTemp / 10f

            val rawLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (rawLevel >= 0 && scale > 0) {
                level = (rawLevel * 100) / scale
            }

            val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            plugType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Pad"
                else -> if (isCharging) "Charging" else "On Battery"
            }

            voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4100)
            val rawHealth = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            healthStr = when (rawHealth) {
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                else -> "Normal"
            }
        }

        var thermalStatus = 0
        var thermalHeadroom = 0.45f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            thermalStatus = powerManager.currentThermalStatus
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && powerManager != null) {
            try {
                thermalHeadroom = powerManager.getThermalHeadroom(30).coerceIn(0f, 1.5f)
            } catch (e: Exception) {
                // If thermal headroom calculation is unavailable on specific vendor kernel
                thermalHeadroom = (tempC / 42.0f).coerceIn(0f, 1.5f)
            }
        } else {
            thermalHeadroom = (tempC / 42.0f).coerceIn(0f, 1.5f)
        }

        val updated = DeviceThermals(
            temperatureC = tempC,
            thermalStatus = thermalStatus,
            thermalHeadroom = thermalHeadroom,
            batteryPercent = level,
            isCharging = isCharging,
            chargingType = plugType,
            voltageMv = voltage,
            health = healthStr,
            timestamp = System.currentTimeMillis()
        )

        cacheManager.putCachedThermals(updated)
        _thermals.value = updated
        return updated
    }
}
