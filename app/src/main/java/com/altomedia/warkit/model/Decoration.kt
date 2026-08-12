package com.altomedia.warkit.model

/**
 * Dekorasi warung (BAB 15). Tiap dekorasi menambah reputasi & peluang VIP,
 * serta mempercantik visual warung.
 */
data class Decoration(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Int,
    val reputationBoost: Int,
    val vipBoost: Float,      // tambahan peluang VIP
    val aestheticBoost: Int,
    val unlockLevel: Int
)
