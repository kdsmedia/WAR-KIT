package com.altomedia.warkit.data

import com.altomedia.warkit.model.SecurityUpgrade

/**
 * Daftar upgrade keamanan (BAB 28).
 */
object SecurityBank {

    val all: List<SecurityUpgrade> = listOf(
        SecurityUpgrade("cctv",        "CCTV",         "📹", 1_500_000, 0.40f, 5,  9),
        SecurityUpgrade("alarm",       "Alarm",        "🚨", 3_000_000, 0.25f, 8, 11),
        SecurityUpgrade("satpam",      "Satpam",       "👮", 5_000_000, 0.20f, 12, 13),
        SecurityUpgrade("lampu_parkir","Lampu Parkir", "💡", 2_000_000, 0.10f, 6, 12),
    )

    fun unlocked(level: Int): List<SecurityUpgrade> = all.filter { it.unlockLevel <= level }
    fun byId(id: String): SecurityUpgrade = all.first { it.id == id }
}
