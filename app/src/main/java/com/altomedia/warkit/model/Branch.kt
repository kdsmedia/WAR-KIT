package com.altomedia.warkit.model

/**
 * Cabang warung (BAB 21). Tiap cabang punya saldo, gudang, pegawai, dan
 * reputasi sendiri, tapi keuntungan otomatis mengalir ke saldo utama.
 */
data class Branch(
    val id: String,
    val name: String,
    val emoji: String,
    val openCost: Long,
    val unlockLevel: Int,
    var balance: Long = 0,         // saldo cabang (sebelum dikirim ke utama)
    val warehouse: MutableMap<String, Int> = mutableMapOf(),
    var reputation: Int = 0,
    var managerLevel: Int = 0,     // BAB 22: level manajer cabang
    var totalIncome: Long = 0
) {
    /** Efisiensi cabang berdasar level manajer (BAB 22). */
    fun managerEfficiency(): Float = 1f + (managerLevel * 0.05f)
}
