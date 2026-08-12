package com.altomedia.warkit.model

/**
 * Pegawai warung (BAB 11). Mulai dari Kasir Pemula. Atribut memengaruhi
 * kecepatan transaksi, keramahan (reputasi), dan gaji harian.
 */
data class Employee(
    val id: String,
    val name: String,
    val emoji: String,
    val role: Role,
    val workSpeed: Float,     // multiplier kecepatan transaksi
    val friendliness: Int,    // bonus reputasi per pelanggan puas
    val efficiency: Int,      // pengurang biaya operasional (%)
    val dailyWage: Int,       // gaji per hari
    val hireCost: Int,        // biaya rekrut sekali
    val unlockLevel: Int
) {
    enum class Role { CASHIER, STOCKER, CLEANER, MANAGER }
}
