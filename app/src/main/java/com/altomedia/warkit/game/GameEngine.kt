package com.altomedia.warkit.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.altomedia.warkit.core.CustomerSpawner
import com.altomedia.warkit.core.GameState
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.Satisfaction
import com.altomedia.warkit.data.ProductCatalog

/**
 * Update logika permainan tiap frame. Memisahkan update dari render agar
 * GameView tetap rapi. Dipanggil dengan delta-time (detik).
 */
class GameEngine(val state: GameState) {

    val spawner = CustomerSpawner(state)
    private var spawnTimer = 0f

    // Posisi-posisi penting di scene (dinormalisasi relatif lebar layar)
    var cashierX = 0f
    var cashierY = 0f
    var shelfY = 0f
    var exitX = 0f

    fun configure(width: Float, height: Float) {
        cashierX = width * 0.72f
        cashierY = height * 0.62f
        shelfY = height * 0.30f
        exitX = -40f
    }

    /** Update tiap frame. dt = detik. */
    fun update(dt: Float) {
        if (!state.shopOpen) return

        // Hari berganti tiap 120 detik gameplay (simulasi)
        // (tracking day dipisah; di-handle di MainActivity via timer)

        // Spawn pelanggan
        spawnTimer += dt
        if (spawnTimer >= spawner.spawnInterval()) {
            spawnTimer = 0f
            spawner.spawn()?.let { state.customers.add(it) }
        }

        // Update tiap pelanggan
        val toRemove = mutableListOf<Customer>()
        for (c in state.customers) {
            updateCustomer(c, dt)
            if (c.phase == Customer.Phase.DONE) toRemove.add(c)
        }
        toRemove.forEach { state.customers.remove(it) }
    }

    private fun updateCustomer(c: Customer, dt: Float) {
        when (c.phase) {
            Customer.Phase.ENTERING -> {
                c.x += c.speed * dt
                if (c.x >= shelfY * 0.4f) {  // sampai area rak
                    c.phase = Customer.Phase.PICKING
                    c.waited = 0f
                }
            }
            Customer.Phase.PICKING -> {
                c.waited += dt
                // Ambil barang satu per satu
                val pickTime = 0.6f
                if (c.waited >= pickTime) {
                    c.waited = 0f
                    val idx = c.pickedItems
                    if (idx < c.shoppingList.size) {
                        val pid = c.shoppingList[idx]
                        val taken = state.takeFromShelf(pid)
                        if (taken > 0) {
                            c.pickedItems++
                            c.bill += ProductCatalog.byId(pid).sellPrice
                        } else {
                            // stok habis: skip item, kepuasan turun
                            c.satisfaction = Satisfaction.KECEWA
                        }
                    } else {
                        // selesai ambil, lanjut ke kasir
                        c.phase = Customer.Phase.QUEUING
                        c.waited = 0f
                    }
                }
                // kesabaran
                if (c.waited > c.patience && c.satisfaction != Satisfaction.MARAH) {
                    degradeSatisfaction(c)
                }
            }
            Customer.Phase.QUEUING -> {
                // bergerak ke kasir
                val dx = cashierX - c.x
                val dy = cashierY - c.y
                val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (dist < 5f) {
                    c.phase = Customer.Phase.PAYING
                    c.waited = 0f
                } else {
                    c.x += (dx / dist) * c.speed * dt
                    c.y += (dy / dist) * c.speed * dt
                }
                c.waited += dt
                if (c.waited > c.patience) degradeSatisfaction(c)
            }
            Customer.Phase.PAYING -> {
                c.waited += dt
                if (c.waited >= 0.8f) {
                    state.completePurchase(c)
                    c.phase = Customer.Phase.LEAVING
                    c.waited = 0f
                }
            }
            Customer.Phase.LEAVING -> {
                c.x -= c.speed * dt * 1.5f
                if (c.x <= exitX) c.phase = Customer.Phase.DONE
            }
            Customer.Phase.DONE -> { /* dihapus */ }
        }
    }

    private fun degradeSatisfaction(c: Customer) {
        c.satisfaction = when (c.satisfaction) {
            Satisfaction.SANGAT_PUAS -> Satisfaction.PUAS
            Satisfaction.PUAS -> Satisfaction.NETRAL
            Satisfaction.NETRAL -> Satisfaction.KECEWA
            Satisfaction.KECEWA -> Satisfaction.MARAH
            Satisfaction.MARAH -> Satisfaction.MARAH
        }
    }
}
