package com.example.hardware

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.provider.Settings
import com.example.cache.MetricCacheManager
import com.example.model.ComplianceActionType
import com.example.model.ComplianceCheckItem
import com.example.model.ComplianceSeverity
import com.example.model.DeviceThermals
import com.example.model.MemoryInfoData
import com.example.model.StorageInfoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class SystemResourceManager(
    private val context: Context,
    private val cacheManager: MetricCacheManager
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _memoryFlow = MutableStateFlow(MemoryInfoData())
    val memoryFlow: StateFlow<MemoryInfoData> = _memoryFlow.asStateFlow()

    private val _storageFlow = MutableStateFlow(StorageInfoData())
    val storageFlow: StateFlow<StorageInfoData> = _storageFlow.asStateFlow()

    private val _complianceFlow = MutableStateFlow<List<ComplianceCheckItem>>(emptyList())
    val complianceFlow: StateFlow<List<ComplianceCheckItem>> = _complianceFlow.asStateFlow()

    init {
        refreshMemory(force = true)
        refreshStorage(force = true)
    }

    fun refreshMemory(force: Boolean = false): MemoryInfoData {
        if (!force) {
            val cached = cacheManager.getCachedMemory()
            if (cached != null) {
                _memoryFlow.value = cached
                return cached
            }
        }

        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val total = if (memInfo.totalMem > 0) memInfo.totalMem else 8L * 1024 * 1024 * 1024
        val avail = memInfo.availMem
        val threshold = memInfo.threshold
        val isLow = memInfo.lowMemory

        val data = MemoryInfoData(
            totalBytes = total,
            availableBytes = avail,
            thresholdBytes = threshold,
            isLowMemory = isLow
        )

        cacheManager.putCachedMemory(data)
        _memoryFlow.value = data
        return data
    }

    fun refreshStorage(force: Boolean = false): StorageInfoData {
        if (!force) {
            val cached = cacheManager.getCachedStorage()
            if (cached != null) {
                _storageFlow.value = cached
                return cached
            }
        }

        var total = 128L * 1024 * 1024 * 1024
        var avail = 48L * 1024 * 1024 * 1024

        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            total = stat.totalBytes
            avail = stat.availableBytes
        } catch (ignored: Exception) {}

        var cacheSize = 0L
        try {
            cacheSize = calculateDirSize(context.cacheDir)
        } catch (ignored: Exception) {}

        val data = StorageInfoData(
            totalBytes = total,
            availableBytes = avail,
            appCacheBytes = cacheSize
        )

        cacheManager.putCachedStorage(data)
        _storageFlow.value = data
        return data
    }

    fun auditOfferwallCompliance(currentThermals: DeviceThermals): List<ComplianceCheckItem> {
        val list = mutableListOf<ComplianceCheckItem>()

        // 1. Usage Access Permission (Required by Mistplay/Freecash for tracking playtime)
        val hasUsageAccess = checkUsageAccess()
        list.add(
            ComplianceCheckItem(
                id = "usage_access",
                title = "Usage Access Tracking",
                subtitle = if (hasUsageAccess) "Active - Offerwalls can track game playtime" else "Missing - Mistplay & Freecash cannot record coins!",
                isCompliant = hasUsageAccess,
                requirementLevel = ComplianceSeverity.MANDATORY,
                actionLabel = if (hasUsageAccess) "Verified" else "Enable Usage Access",
                actionType = ComplianceActionType.OPEN_USAGE_ACCESS,
                ruleExplanation = "Offer walls like Mistplay and Freecash legally rely on Android's Usage Stats API to verify that you played games for the required duration. Without this, zero earnings are credited."
            )
        )

        // 2. Battery Optimization (Doze Mode Whitelist)
        val isBatteryExempt = checkBatteryOptimizationExemption()
        list.add(
            ComplianceCheckItem(
                id = "battery_doze",
                title = "Battery Optimization Status",
                subtitle = if (isBatteryExempt) "Unrestricted - Background tracking won't be killed" else "Restricted - Samsung One UI may terminate offer tracking",
                isCompliant = isBatteryExempt,
                requirementLevel = ComplianceSeverity.MANDATORY,
                actionLabel = if (isBatteryExempt) "Compliant" else "Exempt from Saver",
                actionType = ComplianceActionType.OPEN_BATTERY_OPTIMIZATION,
                ruleExplanation = "Aggressive battery savers and One UI sleep modes force-close background tracking services. To prevent losing session progress, ensure offerwall apps are set to 'Unrestricted' battery usage."
            )
        )

        // 3. Display Refresh Rate Check (Galaxy S22+ 120Hz vs 60Hz)
        list.add(
            ComplianceCheckItem(
                id = "refresh_rate",
                title = "Galaxy S22+ 60Hz Tuning",
                subtitle = "Set screen to Standard 60Hz to reduce GPU thermal load by ~35%",
                isCompliant = false, // Informative check to encourage 60Hz switch
                requirementLevel = ComplianceSeverity.RECOMMENDED,
                actionLabel = "Display Settings",
                actionType = ComplianceActionType.OPEN_DISPLAY_SETTINGS,
                ruleExplanation = "The Galaxy S22+ Dynamic AMOLED 2X defaults to 120Hz Adaptive. Because offerwall games only render at 30-60 FPS, 120Hz needlessly overheats the Snapdragon 8 Gen 1 display controller."
            )
        )

        // 4. Fast Charging during Game Sessions
        val isFastCharging = currentThermals.isCharging && currentThermals.chargingType.contains("Fast", ignoreCase = true)
        val isChargingSafe = !isFastCharging || currentThermals.temperatureC < 38.0f
        list.add(
            ComplianceCheckItem(
                id = "charging_heat",
                title = "Charging Thermal Safety",
                subtitle = if (isFastCharging) "Fast charging while gaming spikes temperatures to 43°C+" else "Safe power state for sustained gaming",
                isCompliant = isChargingSafe,
                requirementLevel = if (isFastCharging) ComplianceSeverity.MANDATORY else ComplianceSeverity.INFO,
                actionLabel = if (isFastCharging) "Unplug Charger" else "Safe",
                actionType = ComplianceActionType.SHOW_OFFERWALL_GUIDE,
                ruleExplanation = "Fast charging heats the 4500mAh battery while gaming heats the Snapdragon SoC. This dual heat source triggers heavy thermal throttling and accelerates battery wear."
            )
        )

        // 5. Memory & RAM Plus Hygiene
        val ramHealthy = _memoryFlow.value.usedPercent < 80f
        list.add(
            ComplianceCheckItem(
                id = "ram_hygiene",
                title = "8GB LPDDR5 Memory Headroom",
                subtitle = if (ramHealthy) "Good RAM headroom available for 3D games" else "High RAM load (${_memoryFlow.value.usedPercent.toInt()}%) - Safe trim recommended",
                isCompliant = ramHealthy,
                requirementLevel = ComplianceSeverity.RECOMMENDED,
                actionLabel = "Safe Trim RAM",
                actionType = ComplianceActionType.TRIM_SAFE_MEMORY,
                ruleExplanation = "AeroTune uses gentle memory trimming and JVM garbage collection without terminating your active offerwall background process."
            )
        )

        _complianceFlow.value = list
        return list
    }

    private fun checkUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun checkBatteryOptimizationExemption(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    /**
     * Safe memory trim: triggers GC and clears local buffers without killing any background services.
     */
    fun performSafeMemoryTrim(): Long {
        val beforeAvail = _memoryFlow.value.availableBytes
        
        // 1. Clear internal cache managers
        cacheManager.onTrimMemory(android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        
        // 2. Request VM garbage collection
        System.gc()
        System.runFinalization()

        // 3. Re-read memory
        val updated = refreshMemory(force = true)
        val freed = (updated.availableBytes - beforeAvail).coerceAtLeast(0L)
        return freed
    }

    fun openUsageAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openApplicationDetails()
        }
    }

    fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openApplicationDetails()
        }
    }

    fun openDisplaySettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openApplicationDetails()
        }
    }

    fun openStorageSettings() {
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openApplicationDetails()
        }
    }

    private fun openApplicationDetails() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirSize(f) else f.length()
        }
        return size
    }
}
