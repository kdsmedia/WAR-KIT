package com.altomedia.warkit.data

import com.altomedia.warkit.model.Branch

/**
 * Daftar lokasi cabang yang bisa dibuka (BAB 21). Dimulai dari desa
 * Sukamaju, lalu kota-kota lain terkunci sampai pemain membukanya.
 */
object BranchBank {

    val all: List<Branch> = listOf(
        Branch("sukamaju",  "Desa Sukamaju",  "🏘️",  5_000_000, 10),
        Branch("bandung",   "Kota Bandung",   "🏙️", 15_000_000, 13),
        Branch("surabaya",  "Kota Surabaya",  "🌆", 25_000_000, 16),
        Branch("medan",     "Kota Medan",     "🌃", 40_000_000, 19),
        Branch("makassar",  "Kota Makassar",  "🌅", 60_000_000, 22),
        Branch("jakarta",   "Kota Jakarta",   "🏢", 100_000_000, 25),
    )

    fun unlocked(level: Int): List<Branch> = all.filter { it.unlockLevel <= level }
    fun byId(id: String): Branch = all.first { it.id == id }
}
