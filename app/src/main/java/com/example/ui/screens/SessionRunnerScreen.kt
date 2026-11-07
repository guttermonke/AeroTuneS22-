package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameSession
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceCyan
import com.example.ui.theme.PureBlackOled
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalCool
import com.example.ui.theme.ThermalCritical
import com.example.ui.theme.ThermalWarm

@Composable
fun SessionRunnerScreen(
    activeSession: GameSession,
    sessionLogs: List<String>,
    useFahrenheit: Boolean,
    onStartSession: (String, String) -> Unit,
    onStopSession: () -> Unit,
    onClearLogs: () -> Unit,
    onNavigateToChill: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var gameInput by remember { mutableStateOf("Coin Master") }
    val offerwalls = listOf("Freecash", "Mistplay", "Swagbucks", "Tapjoy", "Other")
    var selectedOfferwall by remember { mutableStateOf("Freecash") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Offer Session Runner",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Live Playtime & Thermal Throttling Guard",
                style = MaterialTheme.typography.bodySmall,
                color = IceCyan
            )
        }

        // Active Session HUD Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (activeSession.isTracking) IceCyan else CardBorderDark
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (activeSession.isTracking) ThermalCool else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (activeSession.isTracking) "SESSION TRACKING ACTIVE" else "SESSION READY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (activeSession.isTracking) ThermalCool else TextMuted,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IceCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = activeSession.offerWall,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = IceCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Stopwatch
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeSession.formattedDuration,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        ),
                        color = if (activeSession.isTracking) TextPrimary else TextMuted,
                        modifier = Modifier.testTag("session_duration_text")
                    )
                    Text(
                        text = if (activeSession.isTracking) activeSession.gameName else "Tap 'Start Offer Session' below",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thermal Delta Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainer)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("START TEMP", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "%.1f°C".format(activeSession.startTempC),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CURRENT", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "%.1f°C".format(activeSession.currentTempC),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (activeSession.currentTempC >= 41.5f) ThermalCritical else IceCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HEAT DELTA", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        val delta = activeSession.tempDeltaC
                        val deltaStr = (if (delta >= 0) "+%.1f°C" else "%.1f°C").format(delta)
                        Text(
                            text = deltaStr,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (delta > 3.0f) ThermalWarm else ThermalCool
                        )
                    }
                }

                // Throttling warning if hot
                if (activeSession.currentTempC >= 41.5f) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThermalCritical.copy(alpha = 0.18f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Throttling",
                                tint = ThermalCritical,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Snapdragon 8 Gen 1 is at 41.5°C+! Take a 3-min OLED Chill break before frames drop.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ThermalCritical
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Action
                if (activeSession.isTracking) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onStopSession,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThermalCritical,
                                contentColor = PureBlackOled
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("stop_session_btn")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop & Log", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = onNavigateToChill,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainer,
                                contentColor = FrostTeal
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrostTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("cooldown_break_btn")
                        ) {
                            Icon(Icons.Default.AcUnit, contentDescription = "Chill", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cool Down", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                } else {
                    Button(
                        onClick = { onStartSession(gameInput, selectedOfferwall) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IceCyan,
                            contentColor = PureBlackOled
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_session_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Offer Game Session",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Setup Form (when not tracking)
        if (!activeSession.isTracking) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SESSION CONFIGURATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gameInput,
                        onValueChange = { gameInput = it },
                        label = { Text("Offer Game Title") },
                        placeholder = { Text("e.g. Coin Master, Monopoly Go, Raid") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IceCyan,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Target Offer Wall:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        offerwalls.take(3).forEach { wall ->
                            val isSelected = selectedOfferwall == wall
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IceCyan else SurfaceContainer)
                                    .clickable { selectedOfferwall = wall }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = wall,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) PureBlackOled else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Past Sessions Cache History
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SESSION LOGS (LOCAL CACHE)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    if (sessionLogs.isNotEmpty()) {
                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear logs",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (sessionLogs.isEmpty()) {
                    Text(
                        text = "No recorded sessions yet. Completed sessions will be cached locally with zero network overhead.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        lineHeight = 17.sp
                    )
                } else {
                    sessionLogs.forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = RobotoMonoFontFamily),
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
