package com.altomedia.warkit.data

import com.altomedia.warkit.model.Mission
import com.altomedia.warkit.model.MissionType

/**
 * Bank misi (BAB 9). Misi harian reset tiap hari dengan hadiah lebih besar.
 */
object MissionBank {

    /** Misi awal yang aktif sejak permainan dimulai. */
    fun starter(): List<Mission> = listOf(
        Mission("m_serve_20", "Layani 20 Pelanggan",
            "Layani 20 pelanggan dengan baik.", 20,
            rewardMoney = 100_000, rewardExp = 50),
        Mission("m_sell_mi_50", "Jual 50 Mi Instan",
            "Jual 50 mi instan.", 50,
            rewardMoney = 150_000, rewardDiamond = 1, rewardExp = 40),
        Mission("m_restock_5", "Isi Stok 5 Kali",
            "Isi ulang stok 5 kali.", 5,
            rewardMoney = 80_000, rewardExp = 30),
        Mission("m_upgrade", "Upgrade Rak",
            "Upgrade rak minimal 1 level.", 1,
            rewardMoney = 200_000, rewardDiamond = 1, rewardBooster = 1),
        Mission("m_levelup", "Naik Level",
            "Naik ke level 2.", 2,
            rewardMoney = 120_000, rewardDiamond = 1, rewardBooster = 1, rewardChest = 1),
    )

    /** Misi harian (reset tiap hari, hadiah lebih besar). */
    fun daily(): List<Mission> = listOf(
        Mission("d_serve_50", "Misi Harian: Layani 50 Pelanggan",
            "Layani 50 pelanggan hari ini.", 50, daily = true,
            rewardMoney = 300_000, rewardDiamond = 3, rewardExp = 100, rewardChest = 1),
        Mission("d_sell_100", "Misi Harian: Jual 100 Produk",
            "Jual 100 produk apapun hari ini.", 100, daily = true,
            rewardMoney = 250_000, rewardDiamond = 2, rewardExp = 80),
        Mission("d_restock_10", "Misi Harian: Isi Stok 10 Kali",
            "Isi ulang stok 10 kali hari ini.", 10, daily = true,
            rewardMoney = 200_000, rewardDiamond = 2, rewardExp = 60),
    )
}

/**
 * Pemetaan misi -> tipe untuk tracking progress.
 */
fun Mission.type(): MissionType = when {
    id.startsWith("m_serve") || id.startsWith("d_serve")  -> MissionType.SERVE_CUSTOMERS
    id.startsWith("m_sell")  || id.startsWith("d_sell")  -> MissionType.SELL_PRODUCT
    id.startsWith("m_restock")|| id.startsWith("d_restock")-> MissionType.RESTOCK
    id.startsWith("m_upgrade")                            -> MissionType.UPGRADE
    id.startsWith("m_levelup")                             -> MissionType.LEVEL_UP
    else                                                   -> MissionType.SERVE_CUSTOMERS
}
