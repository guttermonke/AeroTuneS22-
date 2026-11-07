package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ThermalCool
import com.example.ui.theme.ThermalCritical
import com.example.ui.theme.ThermalHot
import com.example.ui.theme.ThermalWarm

data class DeviceThermals(
    val temperatureC: Float = 31.5f,
    val thermalStatus: Int = 0, // PowerManager.THERMAL_STATUS_NONE
    val thermalHeadroom: Float = 0.45f, // 0.0 - 1.5, 1.0 is throttling threshold
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val chargingType: String = "Discharging",
    val voltageMv: Int = 4150,
    val health: String = "Good",
    val timestamp: Long = System.currentTimeMillis()
) {
    val temperatureF: Float
        get() = (temperatureC * 9f / 5f) + 32f

    val thermalStatusLabel: String
        get() = when {
            temperatureC >= 43.0f || thermalStatus >= 4 -> "CRITICAL HEAT"
            temperatureC >= 40.5f || thermalStatus >= 3 -> "THROTTLING RISK"
            temperatureC >= 36.5f || thermalStatus >= 2 -> "WARM GAMING"
            else -> "OPTIMAL COOL"
        }

    val thermalStatusColor: Color
        get() = when {
            temperatureC >= 43.0f || thermalStatus >= 4 -> ThermalCritical
            temperatureC >= 40.5f || thermalStatus >= 3 -> ThermalHot
            temperatureC >= 36.5f || thermalStatus >= 2 -> ThermalWarm
            else -> ThermalCool
        }

    val s22ThermalAdvice: String
        get() = when {
            temperatureC >= 43.0f -> "Snapdragon 8 Gen 1 is thermal throttling! Take a 3-min cool down break to prevent frame drops in your offer game."
            temperatureC >= 40.5f -> "Galaxy S22+ is reaching high temps. Lower screen brightness or switch to 60Hz display to shed heat safely."
            temperatureC >= 36.5f -> "Normal gaming temperature. Do not plug into 45W fast charger while playing to avoid compounding heat."
            else -> "Perfect thermal profile! Safe for long offerwall gaming sessions on Freecash/Mistplay."
        }
}

data class MemoryInfoData(
    val totalBytes: Long = 8L * 1024 * 1024 * 1024, // 8GB on Galaxy S22+
    val availableBytes: Long = 3200L * 1024 * 1024,
    val thresholdBytes: Long = 512L * 1024 * 1024,
    val isLowMemory: Boolean = false
) {
    val totalGb: Float
        get() = totalBytes.toFloat() / (1024f * 1024f * 1024f)

    val availableGb: Float
        get() = availableBytes.toFloat() / (1024f * 1024f * 1024f)

    val usedBytes: Long
        get() = (totalBytes - availableBytes).coerceAtLeast(0)

    val usedGb: Float
        get() = usedBytes.toFloat() / (1024f * 1024f * 1024f)

    val usedPercent: Float
        get() = if (totalBytes > 0) ((usedBytes.toFloat() / totalBytes) * 100f).coerceIn(0f, 100f) else 0f
}

data class StorageInfoData(
    val totalBytes: Long = 128L * 1024 * 1024 * 1024,
    val availableBytes: Long = 48L * 1024 * 1024 * 1024,
    val appCacheBytes: Long = 184000000L // internal cached bytes
) {
    val totalGb: Float
        get() = totalBytes.toFloat() / (1024f * 1024f * 1024f)

    val availableGb: Float
        get() = availableBytes.toFloat() / (1024f * 1024f * 1024f)

    val usedBytes: Long
        get() = (totalBytes - availableBytes).coerceAtLeast(0)

    val usedGb: Float
        get() = usedBytes.toFloat() / (1024f * 1024f * 1024f)

    val usedPercent: Float
        get() = if (totalBytes > 0) ((usedBytes.toFloat() / totalBytes) * 100f).coerceIn(0f, 100f) else 0f
}

data class ComplianceCheckItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isCompliant: Boolean,
    val requirementLevel: ComplianceSeverity,
    val actionLabel: String,
    val actionType: ComplianceActionType,
    val ruleExplanation: String
)

enum class ComplianceSeverity {
    MANDATORY,
    RECOMMENDED,
    INFO
}

enum class ComplianceActionType {
    OPEN_USAGE_ACCESS,
    OPEN_BATTERY_OPTIMIZATION,
    OPEN_DISPLAY_SETTINGS,
    OPEN_STORAGE_SETTINGS,
    TRIM_SAFE_MEMORY,
    SHOW_OFFERWALL_GUIDE
}

data class GameSession(
    val id: String = System.currentTimeMillis().toString(),
    val gameName: String = "Active Offer Game",
    val offerWall: String = "Freecash",
    val startTimeMs: Long = System.currentTimeMillis(),
    val elapsedSeconds: Long = 0L,
    val startTempC: Float = 32.0f,
    val currentTempC: Float = 32.0f,
    val peakTempC: Float = 32.0f,
    val isTracking: Boolean = false
) {
    val tempDeltaC: Float
        get() = currentTempC - startTempC

    val formattedDuration: String
        get() {
            val mins = elapsedSeconds / 60
            val secs = elapsedSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
}
