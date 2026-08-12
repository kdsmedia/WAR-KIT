package com.altomedia.warkit.data

import com.altomedia.warkit.model.Province
import com.altomedia.warkit.model.RegionalProduct

/**
 * Daftar provinsi (BAB 41) + produk khas daerah (BAB 42).
 */
object ProvinceBank {

    val provinces: List<Province> = listOf(
        Province("jabar", "Jawa Barat", "🌾", 8_000_000, 15, 1.2f, 1.1f, 0.3f,
            listOf("keripik_singkong", "opak")),
        Province("jateng", "Jawa Tengah", "🏝️", 12_000_000, 17, 1.1f, 1.15f, 0.35f,
            listOf("getuk", "wingko")),
        Province("jatim", "Jawa Timur", "🌋", 15_000_000, 19, 1.25f, 1.2f, 0.4f,
            listOf("kerupuk_udang")),
        Province("sumut", "Sumatera Utara", "🌴", 18_000_000, 21, 1.0f, 1.25f, 0.45f,
            listOf("kopi_lokal")),
        Province("sumbar", "Sumatera Barat", "⛰️", 20_000_000, 22, 1.0f, 1.25f, 0.4f,
            listOf("kopi_lokal")),
        Province("kalsel", "Kalimantan Selatan", "🛶", 25_000_000, 24, 0.9f, 1.3f, 0.5f,
            listOf("kopi_lokal")),
        Province("sulsel", "Sulawesi Selatan", "⚓", 28_000_000, 26, 1.0f, 1.3f, 0.55f,
            listOf("kopi_toraja")),
        Province("bali", "Bali", "🌺", 30_000_000, 28, 1.3f, 1.4f, 0.6f,
            listOf("pie_susu")),
        Province("ntt", "Nusa Tenggara", "🏝️", 35_000_000, 30, 0.8f, 1.35f, 0.5f,
            listOf("kopi_lokal")),
        Province("papua", "Papua", "🦜", 50_000_000, 33, 0.7f, 1.5f, 0.7f,
            listOf("kopi_lokal")),
    )

    val regionalProducts: List<RegionalProduct> = listOf(
        RegionalProduct("keripik_singkong", "Keripik Singkong", "🍠", "jabar", 5_000, 9_000, 3, 13),
        RegionalProduct("opak", "Opak", "🫓", "jabar", 4_000, 7_500, 2, 13),
        RegionalProduct("getuk", "Getuk", "🍮", "jateng", 5_000, 8_500, 3, 15),
        RegionalProduct("wingko", "Wingko", "🥮", "jateng", 6_000, 10_000, 3, 15),
        RegionalProduct("kerupuk_udang", "Kerupuk Udang", "🦐", "jatim", 8_000, 13_000, 4, 17),
        RegionalProduct("kopi_lokal", "Kopi Lokal", "☕", "sumut", 10_000, 16_000, 4, 18),
        RegionalProduct("pie_susu", "Pie Susu", "🥧", "bali", 12_000, 18_000, 5, 20),
        RegionalProduct("kopi_toraja", "Kopi Toraja", "☕", "sulsel", 15_000, 23_000, 6, 22),
    )

    fun unlocked(level: Int): List<Province> = provinces.filter { it.unlockLevel <= level }
    fun byId(id: String): Province = provinces.first { it.id == id }
    fun regionalByProvince(provinceId: String): List<RegionalProduct> =
        regionalProducts.filter { it.provinceId == provinceId }
    fun regionalById(id: String): RegionalProduct = regionalProducts.first { it.id == id }
}
