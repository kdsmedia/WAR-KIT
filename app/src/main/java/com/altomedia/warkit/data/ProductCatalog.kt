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

        // === BAB 24: Produk segar (masa simpan terbatas) ===
        Product("sayur",  "Sayur",   "🥬",  3_000,  5_000, 7,  0.9f, shelfLifeSeconds = 90f,  isFresh = true),
        Product("buah",   "Buah",    "🍎",  5_000,  8_000, 7,  0.9f, shelfLifeSeconds = 120f, isFresh = true),
        Product("cabai",  "Cabai",   "🌶️",  4_000,  7_000, 8,  0.8f, shelfLifeSeconds = 80f,  isFresh = true),
        Product("bawang", "Bawang",  "🧅",  3_500,  5_500, 8,  0.8f, shelfLifeSeconds = 100f, isFresh = true),
        Product("tomat",  "Tomat",   "🍅",  3_000,  5_000, 8,  0.8f, shelfLifeSeconds = 80f,  isFresh = true),
        Product("kentang","Kentang", "🥔",  4_000,  6_500, 9,  0.7f, shelfLifeSeconds = 140f, isFresh = true),

        // === BAB 29/30: Produk event Ramadhan & Lebaran ===
        Product("sirup",      "Sirup",       "🥤",   8_000, 12_000, 5,  0.7f),
        Product("kurma",      "Kurma",       "🌴",  15_000, 22_000, 5,  0.8f),
        Product("tepung",     "Tepung",      "🌾",  10_000, 14_000, 6,  0.6f),
        Product("kue_lebaran","Kue Lebaran", "🍪",  20_000, 30_000, 6,  0.9f, shelfLifeSeconds = 200f, isFresh = true),

        // === BAB 42: Produk khas daerah ===
        Product("keripik_singkong","Keripik Singkong","🍠",  5_000,  9_000, 13, 0.6f),
        Product("opak",            "Opak",            "🫓",  4_000,  7_500, 13, 0.5f),
        Product("getuk",           "Getuk",           "🍮",  5_000,  8_500, 15, 0.6f),
        Product("wingko",          "Wingko",          "🥮",  6_000, 10_000, 15, 0.6f),
        Product("kerupuk_udang",   "Kerupuk Udang",   "🦐",  8_000, 13_000, 17, 0.7f),
        Product("kopi_lokal",      "Kopi Lokal",      "☕", 10_000, 16_000, 18, 0.8f),
        Product("pie_susu",        "Pie Susu",        "🥧", 12_000, 18_000, 20, 0.8f),
        Product("kopi_toraja",     "Kopi Toraja",     "☕", 15_000, 23_000, 22, 0.9f),
    )

    fun byId(id: String): Product = all.first { it.id == id }

    /** Produk yang sudah terbuka pada [level] tertentu. */
    fun unlocked(level: Int): List<Product> = all.filter { it.unlockLevel <= level }

    /** Produk awal (BAB 1). */
    fun starter(): List<Product> = all.filter { it.unlockLevel == 1 }
}
