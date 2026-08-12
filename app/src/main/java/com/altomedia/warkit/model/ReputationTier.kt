package com.altomedia.warkit.model

/**
 * Level reputasi warung (BAB 17). Naik bertahap saat poin reputasi mencapai
 * ambang batas. Semakin tinggi, semakin banyak pelanggan & VIP.
 */
enum class ReputationTier(
    val displayName: String,
    val minReputation: Int,
    val customerMult: Float,    // multiplier jumlah pelanggan
    val vipChanceBonus: Float,  // tambahan peluang VIP
    val emoji: String
) {
    PEMULA("Pemula", 0, 1.0f, 0.0f, "🌱"),
    TERPERCAYA("Terpercaya", 50, 1.2f, 0.02f, "👍"),
    FAVORIT_DESA("Favorit Desa", 150, 1.5f, 0.04f, "❤️"),
    WARUNG_POPULER("Warung Populer", 350, 1.9f, 0.07f, "🔥"),
    WARUNG_TERKENAL("Warung Terkenal", 700, 2.5f, 0.10f, "⭐"),
    WARUNG_LEGENDARIS("Warung Legendaris", 1200, 3.5f, 0.15f, "👑");

    companion object {
        fun at(reputation: Int): ReputationTier =
            entries.last { reputation >= it.minReputation }
    }
}
