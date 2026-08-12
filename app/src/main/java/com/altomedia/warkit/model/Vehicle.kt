package com.altomedia.warkit.model

/**
 * Kendaraan pengiriman supplier (BAB 23). Semakin baik, pengiriman makin
 * cepat, kapasitas makin besar, biaya operasional makin murah.
 */
enum class Vehicle(
    val displayName: String,
    val emoji: String,
    val speed: Float,         // multiplier kecepatan pengiriman
    val capacity: Int,        // kapasitas angkut per trip
    val costPerTrip: Int,     // biaya operasional per trip
    val unlockLevel: Int,
    val unlockCost: Int
) {
    MOTOR_BOX("Motor Box", "🏍️", 1.0f, 50,   5_000,  1, 0),
    PICKUP("Pickup",        "🛻", 1.4f, 120,  9_000,  5, 1_500_000),
    TRUK_KECIL("Truk Engkel","🚚", 1.8f, 250, 15_000, 9, 4_000_000),
    TRUK_BESAR("Truk Box",  "🚛", 2.4f, 500, 22_000, 13, 10_000_000),
    TRAILER("Trailer",      "🚛💨", 3.2f, 1000, 30_000, 17, 25_000_000);

    companion object {
        fun at(ordinal: Int): Vehicle = entries.getOrElse(ordinal) { MOTOR_BOX }
    }
}

/**
 * Mesin kasir (BAB 25). Upgrade mempercepat scan & mengurangi antrean.
 */
enum class CashRegister(
    val displayName: String,
    val emoji: String,
    val scanSpeed: Float,     // multiplier kecepatan transaksi
    val unlockLevel: Int,
    val upgradeCost: Int
) {
    KASIR_MANUAL("Kasir Manual",   "🧮", 1.0f, 1, 0),
    SCANNER_BARCODE("Scanner Barcode", "📷", 1.5f, 6, 1_200_000),
    KASIR_DIGITAL("Kasir Digital", "💻", 2.0f, 10, 4_000_000),
    KASIR_OTOMATIS("Kasir Otomatis","🤖", 3.0f, 14, 12_000_000);

    companion object {
        fun at(level: Int): CashRegister = entries.getOrElse(level - 1) { KASIR_MANUAL }
    }
}
