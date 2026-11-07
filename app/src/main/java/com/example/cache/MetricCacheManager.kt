package com.example.cache

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.SystemClock
import com.example.model.DeviceThermals
import com.example.model.MemoryInfoData
import com.example.model.StorageInfoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

/**
 * High-performance, lightweight caching manager specifically tuned for Galaxy S22 Plus.
 * 
 * Features:
 * 1. Time-to-Live (TTL) cache: avoids continuous polling of heavy system APIs (ActivityManager, StatFs)
 * 2. Ring buffer in-memory cache: fixed capacity historical temperature samples with zero GC churn
 * 3. Proactive onTrimMemory listener: immediately flushes memory buffers when games need RAM
 * 4. Offerwall Safe: strictly avoids spoofing network, clock, or tracking tokens
 */
class MetricCacheManager(private val context: Context) : ComponentCallbacks2 {

    private val _historyFlow = MutableStateFlow<List<Float>>(emptyList())
    val historyFlow: StateFlow<List<Float>> = _historyFlow.asStateFlow()

    // Pre-allocated ring buffer for temperature history (max 40 points)
    private val maxHistorySize = 40
    private val tempHistory = ArrayDeque<Float>(maxHistorySize)

    // Cached snapshots with TTL (milliseconds)
    private var cachedThermals: DeviceThermals? = null
    private var lastThermalsFetchTime: Long = 0L
    private val thermalsTtlMs: Long = 2500L // 2.5s TTL to balance real-time awareness and 0 CPU load

    private var cachedMemory: MemoryInfoData? = null
    private var lastMemoryFetchTime: Long = 0L
    private val memoryTtlMs: Long = 4000L // 4s TTL for RAM checks

    private var cachedStorage: StorageInfoData? = null
    private var lastStorageFetchTime: Long = 0L
    private val storageTtlMs: Long = 30000L // 30s TTL for disk stats (infrequently changing)

    private val sessionCacheFile by lazy {
        File(context.cacheDir, "aerotune_session_cache.log")
    }

    init {
        context.registerComponentCallbacks(this)
    }

    fun getCachedThermals(): DeviceThermals? {
        val now = SystemClock.elapsedRealtime()
        return if (now - lastThermalsFetchTime < thermalsTtlMs) {
            cachedThermals
        } else {
            null
        }
    }

    fun putCachedThermals(thermals: DeviceThermals) {
        cachedThermals = thermals
        lastThermalsFetchTime = SystemClock.elapsedRealtime()

        synchronized(tempHistory) {
            if (tempHistory.size >= maxHistorySize) {
                tempHistory.removeFirst()
            }
            tempHistory.addLast(thermals.temperatureC)
            _historyFlow.value = tempHistory.toList()
        }
    }

    fun getCachedMemory(): MemoryInfoData? {
        val now = SystemClock.elapsedRealtime()
        return if (now - lastMemoryFetchTime < memoryTtlMs) {
            cachedMemory
        } else {
            null
        }
    }

    fun putCachedMemory(memory: MemoryInfoData) {
        cachedMemory = memory
        lastMemoryFetchTime = SystemClock.elapsedRealtime()
    }

    fun getCachedStorage(): StorageInfoData? {
        val now = SystemClock.elapsedRealtime()
        return if (now - lastStorageFetchTime < storageTtlMs) {
            cachedStorage
        } else {
            null
        }
    }

    fun putCachedStorage(storage: StorageInfoData) {
        cachedStorage = storage
        lastStorageFetchTime = SystemClock.elapsedRealtime()
    }

    /**
     * Persist session telemetry locally to cache directory without hitting external network.
     */
    fun saveSessionLog(gameName: String, durationSecs: Long, startTemp: Float, peakTemp: Float) {
        try {
            val entry = "[${System.currentTimeMillis()}] $gameName | ${durationSecs}s | Start: ${startTemp}C | Peak: ${peakTemp}C\n"
            FileOutputStream(sessionCacheFile, true).use { out ->
                out.write(entry.toByteArray(Charsets.UTF_8))
            }
        } catch (ignored: Exception) {
            // Non-critical cache write
        }
    }

    fun readSessionLogs(): List<String> {
        return try {
            if (sessionCacheFile.exists()) {
                sessionCacheFile.readLines().takeLast(10).reversed()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearSessionLogs() {
        try {
            if (sessionCacheFile.exists()) {
                sessionCacheFile.delete()
            }
        } catch (ignored: Exception) {}
    }

    /**
     * When the S22+ OS alerts for low memory, immediately flush all in-memory history
     * and cached snapshots to ensure offerwall games never suffer OOM (Out Of Memory) crashes.
     */
    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            synchronized(tempHistory) {
                tempHistory.clear()
                _historyFlow.value = emptyList()
            }
            cachedThermals = null
            cachedMemory = null
            cachedStorage = null
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onLowMemory() {
        onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    fun destroy() {
        try {
            context.unregisterComponentCallbacks(this)
        } catch (ignored: Exception) {}
    }
}
