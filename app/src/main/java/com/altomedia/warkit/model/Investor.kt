package com.altomedia.warkit.model

/**
 * Investor (BAB 31). Memberi modal/bonus jika pemain memenuhi target
 * pendapatan harian, pelanggan, reputasi, & omzet selama 30 hari.
 */
data class Investor(
    val name: String,
    val emoji: String,
    val capitalBoost: Long,        // tambahan modal saat deal
    val constructionDiscount: Float, // diskon biaya pembangunan cabang
    val reputationBonus: Int,
    val incomeMult: Float,         // bonus pendapatan
    val targetDailyIncome: Long,
    val targetCustomers: Int,
    val targetReputation: Int,
    val targetOmzet: Long,
    val durationDays: Int = 30
)

/**
 * Target progres investor (BAB 31). Dilacak harian.
 */
data class InvestorProgress(
    var active: Boolean = false,
    var daysElapsed: Int = 0,
    var daysMet: Int = 0,
    var dealAccepted: Boolean = false,
    var totalOmzet: Long = 0
) {
    fun isComplete(duration: Int): Boolean = daysElapsed >= duration
}

/**
 * Jenis pelatihan pegawai (BAB 37).
 */
enum class TrainingType(
    val displayName: String,
    val emoji: String,
    val cost: Int,
    val speedBoost: Float,
    val satisfactionBoost: Int,
    val efficiencyBoost: Int,
    val incomeBoost: Float
) {
    PELAYANAN("Pelayanan Pelanggan", "😊", 300_000, 0.0f, 2, 0, 0.05f),
    STOK("Manajemen Stok",        "📦", 350_000, 0.0f, 0, 3, 0.03f),
    KEBERSIHAN("Kebersihan",       "🧹", 250_000, 0.0f, 1, 1, 0.02f),
    KECEPATAN("Kecepatan Kerja",   "⚡", 400_000, 0.2f, 0, 0, 0.04f),
    KEAMANAN("Keamanan",           "🛡️", 300_000, 0.0f, 0, 0, 0.0f);
}

/**
 * Level bangunan warung (BAB 39). Visual & fasilitas meningkat tiap level.
 */
enum class BuildingLevel(
    val displayName: String,
    val emoji: String,
    val comfort: Float,           // multiplier kepuasan pelanggan
    val transactionMult: Float,   // multiplier nilai transaksi
    val reputationBoost: Int,
    val upgradeCost: Long,
    val unlockLevel: Int
) {
    WARUNG_KAYU("Warung Kayu",          "🏚️", 1.0f, 1.0f, 0, 0, 1),
    WARUNG_SEMI("Warung Semi Permanen", "🏠", 1.1f, 1.1f, 10, 2_000_000, 10),
    WARUNG_MODERN("Warung Modern",      "🏬", 1.2f, 1.2f, 20, 8_000_000, 14),
    MINIMARKET("Minimarket",            "🏪", 1.35f, 1.35f, 35, 25_000_000, 18);

    companion object {
        fun at(level: Int): BuildingLevel = entries.getOrElse(level - 1) { WARUNG_KAYU }
    }
}

/**
 * Fasilitas warung modern (BAB 39).
 */
data class Facility(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Int,
    val comfortBoost: Float,
    val reputationBoost: Int,
    val unlockLevel: Int
)
