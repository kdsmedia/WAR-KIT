package com.altomedia.warkit.data

import com.altomedia.warkit.model.Achievement
import com.altomedia.warkit.model.BuildingSkin

/**
 * Achievement endgame (BAB 50).
 */
object AchievementBank {

    val all: List<Achievement> = listOf(
        Achievement("first_customer", "Pelanggan Pertama", "Layani pelanggan pertama", "👋", target = 1),
        Achievement("serve_100", "Layani 100 Pelanggan", "Layani 100 pelanggan", "👥", target = 100),
        Achievement("serve_1000", "Layani 1.000 Pelanggan", "Layani 1.000 pelanggan", "👨‍👩‍👧‍👦", target = 1000),
        Achievement("serve_10000", "Layani 10.000 Pelanggan", "Layani 10.000 pelanggan", "🏙️", target = 10000),
        Achievement("income_1m", "Omzet 1 Juta", "Hasilkan Rp1.000.000 omzet", "💰", target = 1_000_000),
        Achievement("income_100m", "Omzet 100 Juta", "Hasilkan Rp100.000.000 omzet", "🏦", target = 100_000_000),
        Achievement("income_1b", "Omzet 1 Miliar", "Hasilkan Rp1.000.000.000 omzet", "💎", target = 1_000_000_000),
        Achievement("vip_50", "VIP 50x", "Layani 50 pelanggan VIP", "⭐", target = 50),
        Achievement("branch_10", "10 Cabang", "Buka 10 cabang", "🗺️", target = 10),
        Achievement("branch_100", "100 Cabang", "Buka 100 cabang", "🇮🇩", target = 100),
        Achievement("province_all", "Seluruh Indonesia", "Buka semua 10 provinsi", "🏆", target = 10),
        Achievement("employee_50", "50 Pegawai", "Rekrut 50 pegawai total", "🧑‍💼", target = 50),
        Achievement("rep_legendary", "Legendaris", "Capai tier reputasi Legendaris", "👑", target = 1),
    )

    val skins: List<BuildingSkin> = listOf(
        BuildingSkin("national_award", "Skin Penghargaan Nasional", "🏅", "NATIONAL_AWARD"),
        BuildingSkin("gold_building", "Skin Bangunan Emas", "🪙", "RAJA_WARUNG"),
        BuildingSkin("confetti", "Efek Konfeti", "🎉", "PRESTIGE"),
        BuildingSkin("mascot_statue", "Patung Maskot", "🗿", "RAJA_WARUNG"),
        BuildingSkin("profile_frame", "Bingkai Profil Eksklusif", "🖼️", "PRESTIGE"),
    )

    fun byId(id: String): Achievement = all.first { it.id == id }
}
