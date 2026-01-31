package com.dev.utilix

data class FeatureItem(
    val id: String, // Unique identifier like "flashlight", "dice"
    val titleRes: Int,
    val iconRes: Int,
    var isActive: Boolean = false, // To toggle state (e.g. Flashlight ON/OFF)
    var activeText: String? = null // To display dynamic text (e.g. "FLASH ON")
)
