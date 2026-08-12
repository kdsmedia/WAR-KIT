package com.altomedia.warkit.model

/**
 * Produk yang dijual di warung. Data ekonomi (harga beli/jual, popularitas)
 * didefinisikan di [ProductCatalog]. Setiap rak menyimpan [ShelfItem] yang
 * merujuk ke [Product.id].
 */
data class Product(
    val id: String,
    val name: String,
    val emoji: String,
    val buyPrice: Int,      // harga beli (modal)
    val sellPrice: Int,     // harga jual ke pelanggan
    val unlockLevel: Int,   // level minimum untuk produk ini tersedia
    val popularity: Float   // bobot kemunculan dalam daftar belanja pelanggan
) {
    val profit: Int get() = sellPrice - buyPrice
}

/**
 * Item fisik di rak: pasangan (productId, stok terisi).
 */
data class ShelfItem(
    val productId: String,
    var stock: Int,
    val capacity: Int
)

/**
 * Level rak: kapasitas & estetika meningkat tiap level.
 * Visual berubah dari kayu -> besi pada level 2 (lihat GameView).
 */
data class ShelfLevel(
    val level: Int,
    val capacity: Int,
    val aesthetic: Int,
    val upgradeCost: Int
)
