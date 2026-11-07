package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceThermals
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceCyan
import com.example.ui.theme.PureBlackOled
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalCool

@Composable
fun OLEDChillScreen(
    thermals: DeviceThermals,
    cooldownSecondsRemaining: Int,
    isCooldownActive: Boolean,
    onStartCooldown: (Int) -> Unit,
    onStopCooldown: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fan_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val mins = cooldownSecondsRemaining / 60
    val secs = cooldownSecondsRemaining % 60
    val timerText = "%02d:%02d".format(mins, secs)

    // AMOLED Pure Black Background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlackOled)
            .padding(20.dp)
            .testTag("oled_chill_screen")
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color(0xFF111827))
                .border(1.dp, Color(0xFF1E293B), CircleShape)
                .testTag("chill_back_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = IceCyan
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OLED CHILL CHAMBER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = IceCyan,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pure Black AMOLED Zero-Watt Cooling",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Center: Rotating Turbine Icon + Big Timer + Current Temp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF071224))
                        .border(1.5.dp, IceCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = "Turbine",
                        tint = IceCyan,
                        modifier = Modifier
                            .size(64.dp)
                            .rotate(if (isCooldownActive) rotation else 0f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = timerText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    ),
                    color = TextPrimary,
                    modifier = Modifier.testTag("cooldown_timer_text")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Current Battery: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "%.1f°C".format(thermals.temperatureC),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = thermals.thermalStatusColor
                    )
                }
            }

            // Bottom: Controls & Explanation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isCooldownActive) {
                    Button(
                        onClick = onStopCooldown,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFFF87171)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                            .testTag("stop_cooldown_btn")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "End Cooldown Session",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 5).forEach { min ->
                            Button(
                                onClick = { onStartCooldown(min) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (min == 3) IceCyan else Color(0xFF1E293B),
                                    contentColor = if (min == 3) PureBlackOled else TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("cooldown_${min}m_btn")
                            ) {
                                Text(
                                    text = "${min}m Rest",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Rest your Galaxy S22+ face-up on a hard surface during this timer. AMOLED pixels draw 0W, cooling the Snapdragon SoC by ~3°C rapidly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
