package com.altomedia.warkit.core

import com.altomedia.warkit.data.ProductCatalog
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.CustomerType
import com.altomedia.warkit.model.Satisfaction
import kotlin.random.Random

/**
 * Generator pelanggan (BAB 3 & BAB 7). Menghasilkan pelanggan acak dengan
 * tipe, daftar belanja, kecepatan, dan kesabaran yang bervariasi.
 */
class CustomerSpawner(private val state: GameState) {

    private var nextId: Long = 1L

    /** Jeda spawn (detik) makin pendek seiring naik level, reputasi, & cuaca. */
    fun spawnInterval(): Float {
        val base = 4.0f
        val reduce = (state.level * 0.2f) + (state.reputation * 0.01f)
        val tierBoost = (state.reputationTier().customerMult - 1f) * 0.5f
        val weatherMult = 1f / state.weather.customerMult()
        return ((base - reduce - tierBoost) * weatherMult).coerceIn(0.4f, 4.0f)
    }

    /** Probabilitas tipe pelanggan dipengaruhi waktu (BAB 18). */
    private fun pickType(): CustomerType {
        // 60% pelanggan dominan sesuai waktu, 40% acak (BAB 18)
        if (Random.nextFloat() < 0.6f) return state.timeOfDay.dominantCustomer
        val r = Random.nextFloat()
        return when {
            r < 0.30f -> CustomerType.IBU_RUMAH_TANGGA
            r < 0.50f -> CustomerType.ANAK_SEKOLAH
            r < 0.68f -> CustomerType.OJOL
            r < 0.85f -> CustomerType.PETANI
            else      -> CustomerType.KARYAWAN
        }
    }

    fun spawn(): Customer? {
        if (!state.shopOpen) return null
        val type = pickType()
        val unlocked = ProductCatalog.unlocked(state.level)

        // Preferensi: tipe + waktu + cuaca + event (BAB 7, 18, 19, 29, 30)
        val timePref = state.timeOfDay.preferredProducts()
        val weatherPref = state.weather.boostedProducts()
        val eventPref = state.seasonalEvent.hotProducts()
        val preferred = (type.preferredProducts() + timePref + weatherPref + eventPref).distinct()
            .filter { id -> unlocked.any { it.id == id } && state.shelfHas(id) }
        val pool = if (preferred.isNotEmpty()) preferred else unlocked.map { it.id }
        if (pool.isEmpty()) return null

        // BAB 14: belanja campuran — gabungkan preferensi + acak
        val itemCount = Random.nextInt(1, minOf(4, pool.size + 1))
        val shoppingList = mutableListOf<String>()
        repeat(itemCount) {
            val candidate = if (Random.nextFloat() < 0.6f && preferred.isNotEmpty())
                preferred[Random.nextInt(preferred.size)] else pool[Random.nextInt(pool.size)]
            shoppingList.add(candidate)
        }

        val speed = 60f + Random.nextFloat() * 40f
        val patience = 12f + Random.nextFloat() * 8f

        // BAB 16: VIP dipengaruhi karakter, tier reputasi, dekorasi
        val isVip = Random.nextFloat() < state.vipChance()
        val finalPatience = if (isVip) patience + 12f else patience
        // VIP belanja lebih banyak (BAB 16: beras, minyak, susu, gula, air)
        val vipExtra = if (isVip) listOf("beras", "minyak", "susu", "gula", "air")
            .filter { unlocked.any { p -> p.id == it } && state.shelfHas(it) } else emptyList()
        val finalList = if (isVip) (shoppingList + vipExtra).distinct() else shoppingList

        // Pegawai kasir mempercepat transaksi (BAB 11)
        val cashierMult = if (state.hasCashier()) state.workSpeedMult() else 1f

        return Customer(
            id = nextId++,
            type = type,
            shoppingList = finalList,
            speed = speed,
            patience = finalPatience,
            satisfaction = Satisfaction.SANGAT_PUAS,
            x = -30f,
            y = 300f,
            phase = Customer.Phase.ENTERING,
            isVip = isVip
        ).also { it.bill = 0 }
    }
}
