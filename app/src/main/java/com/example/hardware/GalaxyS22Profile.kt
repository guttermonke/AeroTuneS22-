package com.example.hardware

object GalaxyS22Profile {
    const val DEVICE_NAME = "Galaxy S22+ (Plus)"
    const val SOC_NAME = "Snapdragon 8 Gen 1 / Exynos 2200"
    const val RAM_TOTAL_GB = 8
    const val BATTERY_CAPACITY_MAH = 4500
    const val SCREEN_TECH = "Dynamic AMOLED 2X (6.6\")"
    const val REFRESH_RATE_MAX = "120Hz Adaptive"

    // Thermal Thresholds for S22+
    const val TEMP_SAFE_CEILING = 36.5f
    const val TEMP_WARM_CEILING = 40.0f
    const val TEMP_THROTTLING_POINT = 41.8f
    const val TEMP_CRITICAL_POINT = 43.5f

    data class ThermalOptimizationTip(
        val title: String,
        val description: String,
        val estimatedTempDrop: String,
        val isOfferwallSafe: Boolean
    )

    val OPTIMIZATION_TIPS = listOf(
        ThermalOptimizationTip(
            title = "Switch Display to Standard 60Hz",
            description = "Most offerwall games (Monopoly Go, Raid, Coin Master) are capped at 30-60 FPS. Running 120Hz Adaptive forces the display DDIC & GPU to run at double rate, generating ~1.2W of useless heat.",
            estimatedTempDrop = "-2.5°C to -3.8°C",
            isOfferwallSafe = true
        ),
        ThermalOptimizationTip(
            title = "Disable or Reduce 'RAM Plus' to 2GB",
            description = "Samsung's RAM Plus creates a swap partition on internal UFS storage. Heavy continuous gaming triggers constant memory paging, overheating the SoC and storage controller. 8GB physical LPDDR5 RAM is plenty.",
            estimatedTempDrop = "-1.5°C & smoother FPS",
            isOfferwallSafe = true
        ),
        ThermalOptimizationTip(
            title = "Avoid 45W Fast Charging While Playing",
            description = "Charging at 25W/45W generates high battery heat. Combined with the Snapdragon 8 Gen 1 gaming load, the device hits 43°C+ within 10 minutes. Use 10-15W or play on battery.",
            estimatedTempDrop = "-4.0°C to -6.0°C",
            isOfferwallSafe = true
        ),
        ThermalOptimizationTip(
            title = "Never Use Aggressive Task Killers",
            description = "Traditional battery savers and task killers will terminate Mistplay or Freecash background playtime tracking services, causing 0 reward coins and possible account flags. AeroTune uses zero-kill passive tuning.",
            estimatedTempDrop = "Prevents Offer Bans",
            isOfferwallSafe = true
        ),
        ThermalOptimizationTip(
            title = "OLED Pure Black Cooldown Breaks",
            description = "The S22+ AMOLED screen draws zero power on black pixels. Taking a 3-minute cooldown break with AeroTune's Chill Screen cools the phone rapidly while keeping your session alive.",
            estimatedTempDrop = "-3.0°C in 3 mins",
            isOfferwallSafe = true
        )
    )
}
