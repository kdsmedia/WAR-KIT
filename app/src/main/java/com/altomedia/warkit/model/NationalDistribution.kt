package com.altomedia.warkit.model

/**
 * Pusat Distribusi Nasional (BAB 45). Mengatur stok nasional, mengirim
 * barang otomatis, mengurangi biaya logistik, mempercepat pengiriman.
 */
data class NationalDistribution(
    var level: Int = 0,
    var active: Boolean = false
) {
    fun logisticsCostReduction(): Float = (level * 0.05f).coerceAtMost(0.5f)
    fun deliverySpeedMult(): Float = 1f + (level * 0.15f)
    fun stockCapacity(): Int = 5000 * (level + 1)
    fun upgradeCost(): Long = 10_000_000L * (level + 1)
}

/**
 * Krisis pasokan (BAB 46). Dipicu acak; pemain ambil keputusan cepat.
 */
data class SupplyCrisis(
    var active: Boolean = false,
    var daysLeft: Int = 0,
    var severity: Float = 0f  // 0..1
) {
    fun resolve() { active = false; daysLeft = 0; severity = 0f }
}

/**
 * Achievement / prestasi (BAB 50 endgame). Melacak kemajuan jangka panjang.
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    var progress: Int = 0,
    var target: Int,
    var claimed: Boolean = false
) {
    val completed: Boolean get() = progress >= target
}

/**
 * Tantangan berkalanya (BAB 50 endgame): harian, mingguan, bulanan.
 */
enum class ChallengeType(val displayName: String) {
    DAILY("Tantangan Harian"),
    WEEKLY("Tantangan Mingguan"),
    MONTHLY("Tantangan Bulanan"),
    COMMUNITY("Misi Komunitas")
}

/**
 * Skin bangunan eksklusif (BAB 47, 49, 50). Hadiah visual.
 */
data class BuildingSkin(
    val id: String,
    val name: String,
    val emoji: String,
    val source: String  // "NATIONAL_AWARD", "RAJA_WARUNG", "PRESTIGE"
)
