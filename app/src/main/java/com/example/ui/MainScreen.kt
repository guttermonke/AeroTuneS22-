package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OLEDChillScreen
import com.example.ui.screens.OfferwallAuditScreen
import com.example.ui.screens.SessionRunnerScreen
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.IceCyan
import com.example.ui.theme.PureBlackOled
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun MainScreen(
    viewModel: AeroTuneViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val thermals by viewModel.thermals.collectAsState()
    val memory by viewModel.memory.collectAsState()
    val storage by viewModel.storage.collectAsState()
    val complianceItems by viewModel.complianceItems.collectAsState()
    val tempHistory by viewModel.tempHistory.collectAsState()
    val useFahrenheit by viewModel.useFahrenheit.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val cooldownSecondsRemaining by viewModel.cooldownSecondsRemaining.collectAsState()
    val isCooldownActive by viewModel.isCooldownActive.collectAsState()
    val freedMessage by viewModel.freedMemoryMessage.collectAsState()
    val sessionLogs by viewModel.sessionLogs.collectAsState()

    // Handle back button: return to dashboard (tab 0) if on any other tab
    BackHandler(enabled = selectedTab != 0) {
        viewModel.selectTab(0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (selectedTab == 3) PureBlackOled else DeepSpace,
        bottomBar = {
            if (selectedTab != 3) { // Hide navigation bar in OLED Chill mode for pure black
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextSecondary,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlackOled,
                            selectedTextColor = IceCyan,
                            indicatorColor = IceCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Offerwall") },
                        label = { Text("Offer Rules") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlackOled,
                            selectedTextColor = IceCyan,
                            indicatorColor = IceCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_offerwall")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(Icons.Default.Gamepad, contentDescription = "Session") },
                        label = { Text("Session") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlackOled,
                            selectedTextColor = IceCyan,
                            indicatorColor = IceCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_session")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(Icons.Default.AcUnit, contentDescription = "OLED Chill") },
                        label = { Text("Chill") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlackOled,
                            selectedTextColor = IceCyan,
                            indicatorColor = IceCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_chill")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    thermals = thermals,
                    memory = memory,
                    storage = storage,
                    tempHistory = tempHistory,
                    useFahrenheit = useFahrenheit,
                    freedMessage = freedMessage,
                    onToggleUnit = { viewModel.toggleTempUnit() },
                    onTrimMemory = { viewModel.triggerSafeMemoryTrim() },
                    onOpenStorage = { viewModel.handleComplianceAction(com.example.model.ComplianceActionType.OPEN_STORAGE_SETTINGS) },
                    onRefresh = { viewModel.refreshAll() },
                    onNavigateToSession = { viewModel.selectTab(2) },
                    onNavigateToChill = { viewModel.selectTab(3) },
                    onComplianceAction = { viewModel.handleComplianceAction(it) }
                )
                1 -> OfferwallAuditScreen(
                    complianceItems = complianceItems,
                    onActionClick = { viewModel.handleComplianceAction(it) }
                )
                2 -> SessionRunnerScreen(
                    activeSession = activeSession,
                    sessionLogs = sessionLogs,
                    useFahrenheit = useFahrenheit,
                    onStartSession = { name, wall -> viewModel.startSession(name, wall) },
                    onStopSession = { viewModel.stopSession() },
                    onClearLogs = { viewModel.clearHistoryLogs() },
                    onNavigateToChill = { viewModel.selectTab(3) }
                )
                3 -> OLEDChillScreen(
                    thermals = thermals,
                    cooldownSecondsRemaining = cooldownSecondsRemaining,
                    isCooldownActive = isCooldownActive,
                    onStartCooldown = { mins -> viewModel.startCooldownTimer(mins) },
                    onStopCooldown = { viewModel.stopCooldownTimer() },
                    onBack = { viewModel.selectTab(0) }
                )
            }
        }
    }
}
