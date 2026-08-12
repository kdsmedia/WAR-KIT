package com.altomedia.warkit.model

/**
 * Misi pemain (BAB 9). [daily] true => reset tiap hari.
 */
data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    var progress: Int = 0,
    val daily: Boolean = false,
    var completed: Boolean = false,
    var claimed: Boolean = false,
    val rewardMoney: Int = 0,
    val rewardDiamond: Int = 0,
    val rewardExp: Int = 0,
    val rewardBooster: Int = 0,
    val rewardChest: Int = 0
) {
    fun isReady(): Boolean = completed && !claimed
}

/**
 * Tipe misi untuk tracking progress.
 */
enum class MissionType {
    SERVE_CUSTOMERS,    // layani N pelanggan
    SELL_PRODUCT,       // jual N produk tertentu
    RESTOCK,            // isi stok N kali
    UPGRADE,            // upgrade rak/gudang
    LEVEL_UP            // naik level
}
