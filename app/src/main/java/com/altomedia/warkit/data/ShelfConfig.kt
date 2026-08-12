package com.altomedia.warkit.data

import com.altomedia.warkit.model.ShelfLevel

/**
 * Konfigurasi level rak (BAB 6). Tiap level menambah kapasitas, estetika,
 * dan reputasi. Visual berubah kayu -> besi pada level 2.
 */
object ShelfConfig {

    private val levels: List<ShelfLevel> = listOf(
        ShelfLevel(1,  20,  5, 1_000_000),
        ShelfLevel(2,  35, 10, 2_500_000),
        ShelfLevel(3,  55, 18, 6_000_000),
        ShelfLevel(4,  80, 28, 12_000_000),
        ShelfLevel(5, 120, 40, 25_000_000),
    )

    fun get(level: Int): ShelfLevel = levels[minOf(level, levels.size) - 1]

    fun maxLevel(): Int = levels.size

    fun next(level: Int): ShelfLevel? =
        if (level < levels.size) levels[level] else null  // index level (level+1) adalah next
}
