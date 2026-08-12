package com.altomedia.warkit.data

import com.altomedia.warkit.model.Facility

/**
 * Daftar fasilitas warung modern (BAB 39).
 */
object FacilityBank {

    val all: List<Facility> = listOf(
        Facility("pintu_otomatis", "Pintu Otomatis", "🚪", 1_500_000, 0.05f, 5, 10),
        Facility("ac",             "AC",             "❄️", 3_000_000, 0.08f, 8, 12),
        Facility("musik_latar",    "Musik Latar",    "🎵", 1_000_000, 0.04f, 4, 11),
        Facility("pembayaran_digital","Pembayaran Digital","💳", 2_500_000, 0.06f, 7, 13),
        Facility("area_parkir",    "Area Parkir Luas","🅿️", 4_000_000, 0.07f, 10, 14),
    )

    fun unlocked(level: Int): List<Facility> = all.filter { it.unlockLevel <= level }
    fun byId(id: String): Facility = all.first { it.id == id }
}
