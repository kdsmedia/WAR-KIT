package com.altomedia.warkit.model

/**
 * Tipe pelanggan (BAB 7). Masing-masing punya preferensi produk berbeda
 * yang memengaruhi komposisi daftar belanja.
 */
enum class CustomerType(val label: String, val emoji: String) {
    IBU_RUMAH_TANGGA("Ibu Rumah Tangga", "👩"),
    ANAK_SEKOLAH("Anak Sekolah", "🧒"),
    OJOL("Ojol", "🛵"),
    PETANI("Petani", "👨‍🌾"),
    KARYAWAN("Karyawan", "💼");

    /**
     * Produk yang lebih sering dibeli tipe ini (id produk di katalog).
     */
    fun preferredProducts(): List<String> = when (this) {
        IBU_RUMAH_TANGGA -> listOf("beras", "minyak", "telur")
        ANAK_SEKOLAH    -> listOf("air", "mi_instan")
        OJOL           -> listOf("mi_instan", "air")
        PETANI         -> listOf("kopi", "rokok")
        KARYAWAN       -> listOf("kopi", "rokok", "mi_instan")
    }
}

/**
 * Status kepuasan pelanggan. Jika antre terlalu lama, turun ke MARAH.
 */
enum class Satisfaction { SANGAT_PUAS, PUAS, NETRAL, KECEWA, MARAH }

/**
 * Pelanggan AI yang berjalan masuk, mengambil barang, mengantri di kasir,
 * lalu membayar dan keluar.
 *
 * @param speed px/detik kecepatan berjalan
 * @param patience detik sebelum kepuasan mulai turun
 */
data class Customer(
    val id: Long,
    val type: CustomerType,
    val shoppingList: List<String>,   // productId yang akan dibeli
    val speed: Float,
    val patience: Float,              // detik
    var waited: Float = 0f,
    var satisfaction: Satisfaction = Satisfaction.SANGAT_PUAS,
    var x: Float = 0f,
    var y: Float = 0f,
    var phase: Phase = Phase.ENTERING,
    var bill: Int = 0,                // total yang harus dibayar
    var pickedItems: Int = 0
) {
    /** State machine sederhana untuk alur pelanggan. */
    enum class Phase { ENTERING, PICKING, QUEUING, PAYING, LEAVING, DONE }

    fun satisfactionValue(): Float = when (satisfaction) {
        Satisfaction.SANGAT_PUAS -> 1.0f
        Satisfaction.PUAS        -> 0.8f
        Satisfaction.NETRAL      -> 0.6f
        Satisfaction.KECEWA      -> 0.3f
        Satisfaction.MARAH      -> 0.0f
    }

    fun satisfactionBonus(): Int = when (satisfaction) {
        Satisfaction.SANGAT_PUAS -> 5
        Satisfaction.PUAS        -> 2
        Satisfaction.NETRAL     -> 0
        Satisfaction.KECEWA      -> 0
        Satisfaction.MARAH       -> 0
    }
}
