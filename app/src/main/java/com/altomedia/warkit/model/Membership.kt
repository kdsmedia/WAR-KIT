package com.altomedia.warkit.model

/**
 * Kartu member pelanggan (BAB 43). Semakin tinggi tier, semakin sering
 * pelanggan kembali & nilai transaksi naik.
 */
enum class MembershipTier(
    val displayName: String,
    val emoji: String,
    val cost: Int,           // biaya meluncurkan program tier ini
    val returnMult: Float,   // multiplier pelanggan kembali
    val billMult: Float,     // multiplier nilai transaksi
    val unlockLevel: Int
) {
    NONE("Tidak Ada", "➖", 0, 1.0f, 1.0f, 1),
    SILVER("Silver", "🥈", 2_000_000, 1.1f, 1.05f, 13),
    GOLD("Gold", "🥇", 6_000_000, 1.2f, 1.1f, 16),
    PLATINUM("Platinum", "💎", 15_000_000, 1.35f, 1.2f, 19);
}

/**
 * Metode pembayaran (BAB 44). Pembayaran digital mempercepat antrean &
 * mengurangi kesalahan kasir.
 */
enum class PaymentMethod(
    val displayName: String,
    val emoji: String,
    val speedMult: Float,        // multiplier kecepatan transaksi
    val errorReduction: Float,   // pengurangan kesalahan kasir
    val unlockLevel: Int,
    val upgradeCost: Int
) {
    TUNAI("Tunai", "💵", 1.0f, 0.0f, 1, 0),
    DEBIT("Kartu Debit", "💳", 1.2f, 0.3f, 13, 2_500_000),
    QRIS("QRIS", "📱", 1.4f, 0.5f, 16, 5_000_000),
    DOMPET_DIGITAL("Dompet Digital", "👛", 1.6f, 0.6f, 19, 10_000_000);

    companion object {
        fun at(level: Int): PaymentMethod = entries.getOrElse(level) { TUNAI }
    }
}
