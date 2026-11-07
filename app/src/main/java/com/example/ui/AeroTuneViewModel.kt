package com.example.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cache.MetricCacheManager
import com.example.hardware.SystemResourceManager
import com.example.hardware.ThermalMonitor
import com.example.model.ComplianceActionType
import com.example.model.ComplianceCheckItem
import com.example.model.DeviceThermals
import com.example.model.GameSession
import com.example.model.MemoryInfoData
import com.example.model.StorageInfoData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AeroTuneViewModel(application: Application) : AndroidViewModel(application) {

    private val cacheManager = MetricCacheManager(application.applicationContext)
    private val thermalMonitor = ThermalMonitor(application.applicationContext, cacheManager)
    private val resourceManager = SystemResourceManager(application.applicationContext, cacheManager)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    val thermals: StateFlow<DeviceThermals> = thermalMonitor.thermals
    val memory: StateFlow<MemoryInfoData> = resourceManager.memoryFlow
    val storage: StateFlow<StorageInfoData> = resourceManager.storageFlow
    val complianceItems: StateFlow<List<ComplianceCheckItem>> = resourceManager.complianceFlow
    val tempHistory: StateFlow<List<Float>> = cacheManager.historyFlow

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _useFahrenheit = MutableStateFlow(false)
    val useFahrenheit: StateFlow<Boolean> = _useFahrenheit.asStateFlow()

    private val _activeSession = MutableStateFlow(GameSession())
    val activeSession: StateFlow<GameSession> = _activeSession.asStateFlow()

    private val _cooldownSecondsRemaining = MutableStateFlow(180) // 3 mins default
    val cooldownSecondsRemaining: StateFlow<Int> = _cooldownSecondsRemaining.asStateFlow()

    private val _isCooldownActive = MutableStateFlow(false)
    val isCooldownActive: StateFlow<Boolean> = _isCooldownActive.asStateFlow()

    private val _freedMemoryMessage = MutableStateFlow<String?>(null)
    val freedMemoryMessage: StateFlow<String?> = _freedMemoryMessage.asStateFlow()

    private val _sessionLogs = MutableStateFlow<List<String>>(emptyList())
    val sessionLogs: StateFlow<List<String>> = _sessionLogs.asStateFlow()

    private var pollingJob: Job? = null
    private var sessionTimerJob: Job? = null
    private var cooldownTimerJob: Job? = null

    init {
        loadSessionLogs()
        refreshAll()
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                // Adaptive poll rate: 3s when gaming session is active, 6s when idle to save battery
                val delayTime = if (_activeSession.value.isTracking) 3000L else 6000L
                delay(delayTime)
                val current = thermalMonitor.refreshThermals(force = false)
                resourceManager.refreshMemory(force = false)
                resourceManager.auditOfferwallCompliance(current)

                // Update active game session thermals
                if (_activeSession.value.isTracking) {
                    val currentPeak = maxOf(_activeSession.value.peakTempC, current.temperatureC)
                    _activeSession.value = _activeSession.value.copy(
                        currentTempC = current.temperatureC,
                        peakTempC = currentPeak
                    )

                    // Alert user if Galaxy S22+ hits severe throttling point (41.8°C)
                    if (current.temperatureC >= 41.8f && !_activeSession.value.isTracking) {
                        vibrateDevice(shortPattern = false)
                    }
                }
            }
        }
    }

    fun refreshAll() {
        val t = thermalMonitor.refreshThermals(force = true)
        resourceManager.refreshMemory(force = true)
        resourceManager.refreshStorage(force = true)
        resourceManager.auditOfferwallCompliance(t)
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleTempUnit() {
        _useFahrenheit.value = !_useFahrenheit.value
    }

    fun startSession(gameName: String, offerwall: String) {
        val currentTemp = thermals.value.temperatureC
        _activeSession.value = GameSession(
            gameName = gameName.ifBlank { "Offer Game" },
            offerWall = offerwall,
            startTimeMs = System.currentTimeMillis(),
            elapsedSeconds = 0L,
            startTempC = currentTemp,
            currentTempC = currentTemp,
            peakTempC = currentTemp,
            isTracking = true
        )

        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (isActive && _activeSession.value.isTracking) {
                delay(1000L)
                _activeSession.value = _activeSession.value.copy(
                    elapsedSeconds = _activeSession.value.elapsedSeconds + 1
                )
            }
        }
        vibrateDevice(shortPattern = true)
    }

    fun stopSession() {
        if (_activeSession.value.isTracking) {
            val session = _activeSession.value
            cacheManager.saveSessionLog(
                gameName = session.gameName,
                durationSecs = session.elapsedSeconds,
                startTemp = session.startTempC,
                peakTemp = session.peakTempC
            )
            loadSessionLogs()
        }
        sessionTimerJob?.cancel()
        _activeSession.value = _activeSession.value.copy(isTracking = false)
        vibrateDevice(shortPattern = true)
    }

    fun triggerSafeMemoryTrim() {
        viewModelScope.launch {
            val freedBytes = resourceManager.performSafeMemoryTrim()
            val freedMb = freedBytes / (1024 * 1024)
            _freedMemoryMessage.value = if (freedMb > 0) {
                "Safe Trim freed $freedMb MB (No offerwall services killed)"
            } else {
                "Memory already optimized for 8GB LPDDR5"
            }
            vibrateDevice(shortPattern = true)
            delay(3500L)
            _freedMemoryMessage.value = null
        }
    }

    fun startCooldownTimer(minutes: Int = 3) {
        cooldownTimerJob?.cancel()
        _cooldownSecondsRemaining.value = minutes * 60
        _isCooldownActive.value = true

        cooldownTimerJob = viewModelScope.launch {
            while (isActive && _cooldownSecondsRemaining.value > 0) {
                delay(1000L)
                _cooldownSecondsRemaining.value -= 1
            }
            _isCooldownActive.value = false
            vibrateDevice(shortPattern = false)
        }
    }

    fun stopCooldownTimer() {
        cooldownTimerJob?.cancel()
        _isCooldownActive.value = false
        _cooldownSecondsRemaining.value = 180
    }

    fun handleComplianceAction(actionType: ComplianceActionType) {
        when (actionType) {
            ComplianceActionType.OPEN_USAGE_ACCESS -> resourceManager.openUsageAccessSettings()
            ComplianceActionType.OPEN_BATTERY_OPTIMIZATION -> resourceManager.openBatteryOptimizationSettings()
            ComplianceActionType.OPEN_DISPLAY_SETTINGS -> resourceManager.openDisplaySettings()
            ComplianceActionType.OPEN_STORAGE_SETTINGS -> resourceManager.openStorageSettings()
            ComplianceActionType.TRIM_SAFE_MEMORY -> triggerSafeMemoryTrim()
            ComplianceActionType.SHOW_OFFERWALL_GUIDE -> {
                // switch to tips or show advice
                _selectedTab.value = 1
            }
        }
    }

    fun clearHistoryLogs() {
        cacheManager.clearSessionLogs()
        loadSessionLogs()
    }

    private fun loadSessionLogs() {
        _sessionLogs.value = cacheManager.readSessionLogs()
    }

    private fun vibrateDevice(shortPattern: Boolean) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val effect = if (shortPattern) {
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (shortPattern) 40L else 200L)
            }
        } catch (ignored: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        sessionTimerJob?.cancel()
        cooldownTimerJob?.cancel()
        cacheManager.destroy()
    }
}
