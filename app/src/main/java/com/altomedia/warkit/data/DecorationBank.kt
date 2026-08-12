package com.altomedia.warkit.data

import com.altomedia.warkit.model.Decoration

/**
 * Daftar dekorasi warung (BAB 15).
 */
object DecorationBank {

    val all: List<Decoration> = listOf(
        Decoration("pot_bunga", "Pot Bunga", "🪴", 250_000, 5, 0.01f, 10, 7),
        Decoration("banner", "Banner", "🪧", 400_000, 8, 0.01f, 15, 7),
        Decoration("lampu", "Lampu", "💡", 600_000, 12, 0.02f, 20, 7),
        Decoration("cat_baru", "Cat Baru", "🎨", 1_200_000, 20, 0.03f, 35, 7),
        Decoration("neon_box", "Neon Box", "霓", 2_500_000, 35, 0.05f, 55, 7),
        Decoration("kanopi", "Kanopi", "⛺", 4_000_000, 50, 0.07f, 80, 7),
    )

    fun unlocked(level: Int): List<Decoration> = all.filter { it.unlockLevel <= level }
    fun byId(id: String): Decoration = all.first { it.id == id }
}
