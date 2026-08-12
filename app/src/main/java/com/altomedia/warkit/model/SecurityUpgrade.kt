package com.altomedia.warkit.model

/**
 * Upgrade keamanan warung (BAB 28). Mengurangi kehilangan stok & gangguan,
 * serta menambah reputasi (pelanggan merasa aman).
 */
data class SecurityUpgrade(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Int,
    val theftReduction: Float,   // pengurangan tingkat pencurian (0..1)
    val reputationBoost: Int,
    val unlockLevel: Int
)

/**
 * Event musiman (BAB 29 Ramadhan, BAB 30 Lebaran). Menggeser produk terlaris,
 * menaikkan jumlah pelanggan & nilai belanja, serta membuka misi khusus.
 */
enum class SeasonalEvent(
    val displayName: String,
    val emoji: String,
    val customerMult: Float,
    val billMult: Float,
    val vipChanceBonus: Float
) {
    NONE("Biasa", "📅", 1.0f, 1.0f, 0.0f),
    RAMADHAN("Ramadhan", "🌙", 1.3f, 1.1f, 0.03f),
    LEBARAN("Lebaran", "🕌", 1.6f, 1.3f, 0.08f),
    FESTIVAL("Festival Belanja", "🎉", 1.8f, 1.2f, 0.10f);

    /** Produk terlaris saat event (BAB 29, 30, 38). */
    fun hotProducts(): List<String> = when (this) {
        RAMADHAN -> listOf("sirup", "kurma", "minyak", "gula", "tepung", "mi_instan", "susu")
        LEBARAN  -> listOf("sirup", "kurma", "kue_lebaran", "gula", "minyak", "biskuit", "susu")
        FESTIVAL -> listOf("biskuit", "permen", "sirup", "snack", "air", "susu")
        NONE     -> emptyList()
    }
}
