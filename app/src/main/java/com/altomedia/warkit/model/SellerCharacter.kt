package com.altomedia.warkit.model

/**
 * Karakter penjual yang dipilih pemain (lihat README.md). Setiap karakter
 * memberikan bonus pasif yang memengaruhi ekonomi & gameplay.
 */
enum class SellerCharacter(
    val displayName: String,
    val emoji: String,
    val description: String,
    val perks: List<String>
) {
    BU_SARI(
        "Bu Sari", "👩",
        "Ibu rumah tangga yang ramah mengenal hampir semua pelanggan.",
        listOf("Pelayanan lebih ramah", "Pelanggan lebih sering kembali", "Bonus reputasi tinggi")
    ),
    ANDI(
        "Andi", "🧑",
        "Pemuda bercita-cita membangun usaha besar dari warung sederhana.",
        listOf("Kecepatan kerja tinggi", "Upgrade lebih cepat", "Bonus EXP besar")
    ),
    PAK_BUDI(
        "Pak Budi", "👨",
        "Berpengalaman: teliti stok, jago hitung untung, jaga kualitas.",
        listOf("Keuntungan meningkat", "Stok efisien", "Biaya operasional hemat")
    ),
    NEK_WATI(
        "Nek Wati", "👵",
        "Warungnya dikenal puluhan tahun; suasananya hangat.",
        listOf("Pelanggan VIP sering muncul", "Reputasi cepat naik", "Pendapatan offline besar")
    );

    /**
     * Multiplier keuntungan penjualan (BAB 5). Pak Budi & Andi memberi bonus.
     */
    fun profitMultiplier(): Float = when (this) {
        PAK_BUDI -> 1.15f
        ANDI     -> 1.05f
        else     -> 1.0f
    }

    /** Bonus reputasi per transaksi puas (Bu Sari & Nek Wati). */
    fun reputationBonus(): Int = when (this) {
        BU_SARI  -> 2
        NEK_WATI -> 3
        else     -> 1
    }

    /** Bonus EXP per transaksi (Andi). */
    fun expBonus(): Int = when (this) {
        ANDI -> 3
        else -> 0
    }

    /** Diskon biaya upgrade rak/gudang (Andi: upgrade cepat, Pak Budi: hemat). */
    fun upgradeCostMultiplier(): Float = when (this) {
        ANDI     -> 0.85f
        PAK_BUDI -> 0.9f
        else     -> 1.0f
    }

    /** Multiplier pendapatan idle saat offline (Nek Wati). */
    fun offlineIncomeMultiplier(): Float = when (this) {
        NEK_WATI -> 1.5f
        else     -> 1.0f
    }

    /** Peluang pelanggan VIP muncul (Nek Wati). */
    fun vipChance(): Float = when (this) {
        NEK_WATI -> 0.15f
        else     -> 0.03f
    }
}
