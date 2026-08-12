package com.altomedia.warkit.model

/**
 * Jenis promosi (BAB 26). Menarik pelanggan & menaikkan nilai belanja,
 * tapi diskon besar mengurangi margin per barang.
 */
enum class Promotion(
    val displayName: String,
    val emoji: String,
    val customerMult: Float,   // multiplier jumlah pelanggan
    val billMult: Float,       // multiplier nilai belanja
    val profitPenalty: Float,  // pengurangan margin (0..1)
    val reputationBoost: Int,
    val cost: Int,             // biaya menjalankan
    val durationDays: Int
) {
    TIDAK_ADA("Tidak Ada", "➖", 1.0f, 1.0f, 0.00f, 0, 0, 0),
    DISKON_10("Diskon 10%", "🏷️", 1.2f, 1.0f, 0.10f, 2, 100_000, 2),
    DISKON_20("Diskon 20%", "🏷️", 1.4f, 1.0f, 0.20f, 3, 250_000, 2),
    BELI_2_GRATIS_1("Beli 2 Gratis 1", "🎁", 1.3f, 1.15f, 0.15f, 4, 300_000, 3),
    PAKET_HEMAT("Paket Hemat", "📦", 1.25f, 1.2f, 0.08f, 3, 200_000, 3),
    CASHBACK("Cashback", "💸", 1.35f, 1.1f, 0.12f, 5, 350_000, 2);
}

/**
 * Tingkat persaingan bisnis (BAB 27). Kompetitor (minimarket) menarik
 * pelanggan; nilai warung pemain menentukan berapa yang kembali.
 */
data class CompetitionState(
    var competitorActive: Boolean = false,   // minimarket dibangun
    var competitorStrength: Float = 0f,      // 0..1 daya tarik kompetitor
    var playerShopScore: Float = 0f          // 0..1 nilai warung pemain
) {
    /** Fraksi pelanggan yang tetap ke warung pemain (BAB 27). */
    fun retentionRate(): Float {
        if (!competitorActive) return 1f
        val edge = (playerShopScore - competitorStrength).coerceIn(-0.5f, 0.5f)
        return (0.6f + edge).coerceIn(0.3f, 1f)
    }
}
