package com.altomedia.warkit.model

/**
 * Provinsi yang bisa dibuka (BAB 41). Tiap provinsi punya jumlah pelanggan,
 * produk favorit, biaya operasional, dan tingkat persaingan berbeda.
 */
data class Province(
    val id: String,
    val name: String,
    val emoji: String,
    val unlockCost: Long,
    val unlockLevel: Int,
    val customerMult: Float,     // multiplier jumlah pelanggan
    val opCostMult: Float,       // multiplier biaya operasional
    val competitionStrength: Float, // daya tarik kompetitor lokal
    val favoriteProductIds: List<String>  // BAB 42: produk khas daerah
)

/**
 * Produk khas daerah (BAB 42). Harga lebih tinggi, permintaan khusus,
 * bonus reputasi daerah.
 */
data class RegionalProduct(
    val id: String,
    val name: String,
    val emoji: String,
    val provinceId: String,
    val buyPrice: Int,
    val sellPrice: Int,
    val reputationBonus: Int,
    val unlockLevel: Int
)
