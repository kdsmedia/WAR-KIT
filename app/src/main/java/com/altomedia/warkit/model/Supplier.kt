package com.altomedia.warkit.model

/**
 * Supplier (BAB 13). Semakin tinggi tier, harga beli makin murah &
 * produk langka mulai tersedia. Memengaruhi efisiensi restock.
 */
enum class Supplier(
    val displayName: String,
    val emoji: String,
    val discount: Float,       // diskon harga beli (0..1)
    val restockSpeed: Float,   // multiplier kecepatan restock
    val unlockLevel: Int,
    val unlockCost: Int
) {
    DISTRIBUTOR_DESA("Distributor Desa", "🚚", 0.00f, 1.0f, 1, 0),
    SUPPLIER_KOTA("Supplier Kota", "🏭", 0.10f, 1.3f, 3, 750_000),
    SUPPLIER_PABRIK("Supplier Pabrik", "🏗️", 0.20f, 1.6f, 6, 3_000_000);

    /** Harga beli efektif setelah diskon supplier. */
    fun effectiveBuy(basePrice: Int): Int = (basePrice * (1f - discount)).toInt()
}
