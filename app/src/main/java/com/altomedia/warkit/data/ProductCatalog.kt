package com.altomedia.warkit.data

import com.altomedia.warkit.model.Product

/**
 * Katalog produk warung (BAB 1 & unlock-by-level, BAB 8).
 * Produk awal (Level 1): beras, mi instan, minyak goreng, air mineral, telur.
 * Produk berikutnya terbuka saat level naik.
 */
object ProductCatalog {

    val all: List<Product> = listOf(
        // === Produk awal (Level 1) ===
        Product("beras",     "Beras",        "🍚", 10_000, 13_000, 1, 1.0f),
        Product("mi_instan", "Mi Instan",    "🍜",  3_000,  4_000, 1, 1.2f),
        Product("minyak",    "Minyak Goreng","🛢️", 18_000, 21_000, 1, 0.9f),
        Product("air",       "Air Mineral", "💧",  3_000,  4_500, 1, 1.1f),
        Product("telur",    "Telur",        "🥚",  2_500,  3_500, 1, 0.8f),

        // === Level 2 ===
        Product("gula",      "Gula",         "🍬", 12_000, 15_000, 2, 0.7f),
        Product("kopi",      "Kopi Sachet",  "☕",   1_000,  1_500, 2, 1.0f),

        // === Level 4 ===
        Product("teh",       "Teh Botol",    "🧋",   4_000,  6_000, 4, 0.9f),
        Product("snack",     "Snack",        "🍪",   5_000,  7_000, 4, 1.0f),

        // === Level 6 ===
        Product("rokok",     "Rokok",        "🚬",  25_000, 28_000, 6, 0.6f),
        Product("sabun",     "Sabun Mandi",  "🧼",   3_500,  5_000, 6, 0.5f),

        // === Level 8 ===
        Product("shampoo",   "Shampoo",      "🧴",  15_000, 18_000, 8, 0.5f),
        Product("susu",      "Susu Kotak",   "🥛",   7_000,  9_000, 8, 0.8f),

        // === BAB 14: Produk kategori baru (Level 9-11) ===
        Product("deterjen",  "Deterjen",     "🧽",  12_000, 16_000, 9,  0.5f),
        Product("sikat_gigi","Sikat Gigi",   "🪥",   4_000,  6_500, 9,  0.5f),
        Product("pasta_gigi","Pasta Gigi",   "🪥",   7_000, 10_000, 9,  0.6f),
        Product("biskuit",   "Biskuit",      "🥠",   6_000,  8_500, 10, 1.0f),
        Product("permen",    "Permen",       "🍭",   2_000,  3_000, 10, 1.1f),
        Product("saus",      "Saus",         "🍅",   5_000,  7_500, 11, 0.6f),
    )

    fun byId(id: String): Product = all.first { it.id == id }

    /** Produk yang sudah terbuka pada [level] tertentu. */
    fun unlocked(level: Int): List<Product> = all.filter { it.unlockLevel <= level }

    /** Produk awal (BAB 1). */
    fun starter(): List<Product> = all.filter { it.unlockLevel == 1 }
}
