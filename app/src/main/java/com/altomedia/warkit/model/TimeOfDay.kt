package com.altomedia.warkit.model

/**
 * Periode waktu dalam sehari (BAB 18). Mempengaruhi tipe pelanggan dominan
 * dan produk yang dibeli. Satu hari game = beberapa periode berurutan.
 */
enum class TimeOfDay(
    val displayName: String,
    val emoji: String,
    val hourLabel: String,
    val dominantCustomer: CustomerType
) {
    PAGI("Pagi", "🌅", "06.00-10.00", CustomerType.IBU_RUMAH_TANGGA),
    SIANG("Siang", "☀️", "10.00-15.00", CustomerType.KARYAWAN),
    SORE("Sore", "🌇", "15.00-18.00", CustomerType.ANAK_SEKOLAH),
    MALAM("Malam", "🌙", "18.00-22.00", CustomerType.KARYAWAN);

    /** Produk yang lebih laku pada periode ini (BAB 18). */
    fun preferredProducts(): List<String> = when (this) {
        PAGI  -> listOf("beras", "telur", "gula", "minyak")
        SIANG -> listOf("mi_instan", "kopi", "air", "rokok")
        SORE  -> listOf("snack", "air", "mi_instan", "permen")
        MALAM -> listOf("mi_instan", "kopi", "rokok", "susu")
    }

    companion object {
        /** Urutan siklus periode dalam sehari. */
        val cycle = listOf(PAGI, SIANG, SORE, MALAM)
        /** Durasi tiap periode (detik gameplay). */
        const val PERIOD_SECONDS = 30f
    }
}

/**
 * Cuaca (BAB 19). Menggeser permintaan produk tertentu.
 */
enum class Weather(
    val displayName: String,
    val emoji: String
) {
    CERAH("Cerah", "☀️"),
    MENDUNG("Mendung", "☁️"),
    HUJAN("Hujan", "🌧️"),
    PANAS("Panas", "🥵");

    /** Produk yang permintaannya meningkat sesuai cuaca (BAB 19). */
    fun boostedProducts(): List<String> = when (this) {
        HUJAN  -> listOf("kopi", "mi_instan", "teh")
        PANAS  -> listOf("air", "susu", "snack")
        CERAH  -> listOf("air", "snack")
        MENDUNG -> listOf("kopi", "teh")
    }

    /** Multiplier spawn pelanggan (cuaca buruk sedikit mengurangi). */
    fun customerMult(): Float = when (this) {
        CERAH -> 1.0f
        MENDUNG -> 0.95f
        HUJAN -> 0.85f
        PANAS -> 1.1f
    }
}
