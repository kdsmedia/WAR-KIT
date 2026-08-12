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

    /** Jeda spawn (detik) makin pendek seiring naik level & reputasi. */
    fun spawnInterval(): Float {
        val base = 4.0f
        val reduce = (state.level * 0.2f) + (state.reputation * 0.01f)
        return (base - reduce).coerceIn(0.8f, 4.0f)
    }

    /** Probabilitas tipe pelanggan muncul (BAB 7). */
    private fun pickType(): CustomerType {
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

        // Daftar belanja: 1-3 item, condong ke produk preferensi tipe (BAB 7)
        val preferred = type.preferredProducts().filter { id ->
            unlocked.any { it.id == id } && state.shelfHas(id)
        }
        val pool = if (preferred.isNotEmpty()) preferred else unlocked.map { it.id }
        if (pool.isEmpty()) return null

        val itemCount = Random.nextInt(1, minOf(4, pool.size + 1))
        val shoppingList = mutableListOf<String>()
        repeat(itemCount) {
            val pid = pool[Random.nextInt(pool.size)]
            // 60% peluang pilih preferensi, 40% acak
            val candidate = if (Random.nextFloat() < 0.6f && preferred.isNotEmpty())
                preferred[Random.nextInt(preferred.size)] else pool[Random.nextInt(pool.size)]
            shoppingList.add(candidate)
        }

        val speed = 60f + Random.nextFloat() * 40f   // px/detik
        val patience = 12f + Random.nextFloat() * 8f  // detik

        // VIP (Nek Wati): lebih sabar + belanja lebih banyak (BAB 7)
        val isVip = Random.nextFloat() < (state.seller?.vipChance() ?: 0.03f)
        val finalPatience = if (isVip) patience + 8f else patience
        val finalList = if (isVip) shoppingList + pool.random() else shoppingList

        return Customer(
            id = nextId++,
            type = type,
            shoppingList = finalList.distinct(),
            speed = speed,
            patience = finalPatience,
            satisfaction = Satisfaction.SANGAT_PUAS,
            x = -30f,
            y = 300f,
            phase = Customer.Phase.ENTERING
        )
    }
}
