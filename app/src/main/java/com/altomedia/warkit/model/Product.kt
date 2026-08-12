package com.altomedia.warkit.model

/**
 * Produk yang dijual di warung. Data ekonomi (harga beli/jual, popularitas)
 * didefinisikan di [ProductCatalog]. Setiap rak menyimpan [ShelfItem] yang
 * merujuk ke [Product.id].
 *
 * BAB 24: produk segar punya shelfLifeSeconds (masa simpan); 0 = tidak kedaluwarsa.
 */
data class Product(
    val id: String,
    val name: String,
    val emoji: String,
    val buyPrice: Int,      // harga beli (modal)
    val sellPrice: Int,     // harga jual ke pelanggan
    val unlockLevel: Int,   // level minimum untuk produk ini tersedia
    val popularity: Float,  // bobot kemunculan dalam daftar belanja pelanggan
    val shelfLifeSeconds: Float = 0f,  // BAB 24: 0 = awet, >0 = segar (busuk kalau lewat)
    val isFresh: Boolean = false       // BAB 24: kategori produk segar
) {
    val profit: Int get() = sellPrice - buyPrice
}

/**
 * Item fisik di rak: pasangan (productId, stok terisi).
 * BAB 24: freshnessTimer melacak sisa masa simpan produk segar di rak.
 */
data class ShelfItem(
    val productId: String,
    var stock: Int,
    val capacity: Int,
    var freshnessTimer: Float = 0f,   // BAB 24: sisa masa simpan (0 = busuk)
    var spoiled: Boolean = false      // BAB 24: produk busuk, harus dibuang
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
