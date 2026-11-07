package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceThermals
import com.example.model.MemoryInfoData
import com.example.model.StorageInfoData
import com.example.ui.components.HardwareBadgeCard
import com.example.ui.components.MemoryStorageRow
import com.example.ui.components.SafeTrimActionCard
import com.example.ui.components.ThermalGauge
import com.example.ui.components.ThermalTrendCard
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceCyan
import com.example.ui.theme.PureBlackOled
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalWarm

@Composable
fun DashboardScreen(
    thermals: DeviceThermals,
    memory: MemoryInfoData,
    storage: StorageInfoData,
    tempHistory: List<Float>,
    useFahrenheit: Boolean,
    freedMessage: String?,
    onToggleUnit: () -> Unit,
    onTrimMemory: () -> Unit,
    onOpenStorage: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSession: () -> Unit,
    onNavigateToChill: () -> Unit,
    onComplianceAction: (com.example.model.ComplianceActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AeroTune S22+",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Offerwall Safe Thermal & Resource Runner",
                    style = MaterialTheme.typography.bodySmall,
                    color = IceCyan
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                    .testTag("refresh_dashboard_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = IceCyan
                )
            }
        }

        // Hardware Profile Badge
        HardwareBadgeCard()

        // Circular Thermal Gauge
        ThermalGauge(
            thermals = thermals,
            useFahrenheit = useFahrenheit,
            onToggleUnit = onToggleUnit
        )

        // Thermal Advice Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, thermals.thermalStatusColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(thermals.thermalStatusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Advice",
                        tint = thermals.thermalStatusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "S22+ THERMAL HEADROOM: ${(thermals.thermalHeadroom * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = thermals.thermalStatusColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = thermals.s22ThermalAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 3-Step Quick Setup Card for Galaxy S22+
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OFFERWALL & S22+ 1-TAP SETUP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = IceCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Essential 3 Steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Step 1: Usage Access
                    Button(
                        onClick = { onComplianceAction(com.example.model.ComplianceActionType.OPEN_USAGE_ACCESS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceDark,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("quick_setup_usage_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1. Usage", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Tracking", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = IceCyan)
                        }
                    }

                    // Step 2: Battery Optimization
                    Button(
                        onClick = { onComplianceAction(com.example.model.ComplianceActionType.OPEN_BATTERY_OPTIMIZATION) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceDark,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("quick_setup_battery_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("2. Battery", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Doze Exemption", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = FrostTeal)
                        }
                    }

                    // Step 3: 60Hz Display
                    Button(
                        onClick = { onComplianceAction(com.example.model.ComplianceActionType.OPEN_DISPLAY_SETTINGS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceDark,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("quick_setup_display_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("3. Screen", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text("60Hz Cool", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = ThermalWarm)
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToSession,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IceCyan,
                    contentColor = PureBlackOled
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("launch_session_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Session",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Offer Session",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = onNavigateToChill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceDark,
                    contentColor = FrostTeal
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostTeal),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("launch_chill_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = "Chill",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OLED Chill",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Memory & Storage Metrics
        MemoryStorageRow(
            memory = memory,
            storage = storage,
            onOpenStorage = onOpenStorage
        )

        // Safe Memory Trim Action
        SafeTrimActionCard(
            freedMessage = freedMessage,
            onTrimMemory = onTrimMemory
        )

        // Cached Thermal Trend
        ThermalTrendCard(
            tempHistory = tempHistory,
            currentTemp = thermals.temperatureC,
            useFahrenheit = useFahrenheit
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
