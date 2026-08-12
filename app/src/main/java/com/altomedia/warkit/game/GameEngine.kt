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

    // Posisi-posisi penting di scene (dinormalisasi relatif lebar/tinggi layar portrait)
    var cashierX = 0f
    var cashierY = 0f
    var shelfY = 0f
    var exitX = 0f
    var enterX = 0f
    var queueY = 0f

    fun configure(width: Float, height: Float) {
        // Layout portrait: rak di atas, kasir di tengah-bawah, antrean vertikal di kanan
        cashierX = width * 0.34f
        cashierY = height * 0.60f
        shelfY = height * 0.30f
        exitX = -50f
        enterX = width * 0.5f
        queueY = height * 0.78f
    }

    /** Update tiap frame. dt = detik. */
    fun update(dt: Float) {
        if (!state.shopOpen) return

        // BAB 18: maju periode waktu
        state.tickTimeOfDay(dt)
        // BAB 24: update masa simpan produk segar
        state.tickFreshness(dt)

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
                // Masuk dari pintu bawah, berjalan ke depan rak (tengah-atas scene)
                if (c.x < cashierX) c.x += c.speed * dt
                val targetY = shelfY + 40f
                if (c.y < targetY) c.y += c.speed * dt
                if (c.y >= targetY) {
                    c.y = targetY
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
                // bergerak ke kasir (tengah-bawah)
                val dx = cashierX - c.x
                val dy = cashierY - c.y
                val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (dist < 8f) {
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
                // BAB 11/25/44: pegawai kasir + mesin kasir + pembayaran digital
                val payDuration = 0.8f / (state.workSpeedMult() * state.cashRegister().scanSpeed *
                    state.paymentMethod().speedMult)
                if (c.waited >= payDuration) {
                    state.completePurchase(c)
                    c.phase = Customer.Phase.LEAVING
                    c.waited = 0f
                }
            }
            Customer.Phase.LEAVING -> {
                // Keluar lewat pintu bawah
                c.y += c.speed * dt * 1.5f
                if (c.y >= queueY + 120f) c.phase = Customer.Phase.DONE
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
