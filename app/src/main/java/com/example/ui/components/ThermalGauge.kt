package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceThermals
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.IceCyan
import com.example.ui.theme.PureBlackOled
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalCool
import com.example.ui.theme.ThermalCritical
import com.example.ui.theme.ThermalHot
import com.example.ui.theme.ThermalWarm

@Composable
fun ThermalGauge(
    thermals: DeviceThermals,
    useFahrenheit: Boolean,
    onToggleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Normal temp range 25°C to 48°C
    val normalizedProgress = ((thermals.temperatureC - 25f) / (48f - 25f)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = normalizedProgress,
        animationSpec = tween(durationMillis = 600),
        label = "thermal_gauge"
    )

    val tempDisplay = if (useFahrenheit) {
        "%.1f°F".format(thermals.temperatureF)
    } else {
        "%.1f°C".format(thermals.temperatureC)
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("thermal_gauge_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer decorative ring & gauge arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track (240 degrees sweep starting from 150 deg)
            drawArc(
                color = CardBorderDark.copy(alpha = 0.5f),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Thermal gradient sweep
            val thermalGradient = Brush.sweepGradient(
                colors = listOf(
                    ThermalCool,
                    ThermalWarm,
                    ThermalHot,
                    ThermalCritical
                )
            )

            drawArc(
                brush = thermalGradient,
                startAngle = 150f,
                sweepAngle = 240f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner display content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onToggleUnit() }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = "Temperature",
                    tint = thermals.thermalStatusColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "BATTERY TEMP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = tempDisplay,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                modifier = Modifier.testTag("temp_display_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(thermals.thermalStatusColor.copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = thermals.thermalStatusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = thermals.thermalStatusColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to switch ${if (useFahrenheit) "°C" else "°F"}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted.copy(alpha = 0.7f)
            )
        }
    }
}
