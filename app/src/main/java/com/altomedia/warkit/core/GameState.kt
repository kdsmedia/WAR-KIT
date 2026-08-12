package com.altomedia.warkit.core

import com.altomedia.warkit.data.MissionBank
import com.altomedia.warkit.data.ProductCatalog
import com.altomedia.warkit.data.ShelfConfig
import com.altomedia.warkit.data.type
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.Mission
import com.altomedia.warkit.model.MissionType
import com.altomedia.warkit.model.SellerCharacter
import com.altomedia.warkit.model.ShelfItem
import java.util.concurrent.CopyOnWriteArrayList

/**
 * State global permainan (BAB 1-10). Kelas ini menjadi sumber kebenaran
 * tunggal untuk uang, diamond, level, exp, rak, gudang, misi, dan reputasi.
 *
 * Mutable tapi diakses dari thread game-loop utama; pelanggan disimpan dalam
 * CopyOnWriteArrayList untuk keamanan baca dari thread render.
 */
class GameState {

    // === BAB 1: Kondisi awal ===
    var money: Long = 500_000
    var diamond: Int = 0
    var level: Int = 1
    var exp: Int = 0
    var reputation: Int = 0
    var energy: Int = 100
    var booster: Int = 0
    var chest: Int = 0

    var seller: SellerCharacter? = null
    var shopOpen: Boolean = false
    var day: Int = 1

    // Rak: 2 buah (BAB 1), tiap rak punya beberapa slot produk.
    val shelves: MutableList<MutableList<ShelfItem>> = mutableListOf(
        mutableListOf(
            ShelfItem("beras", 15, 20),
            ShelfItem("minyak", 12, 20),
            ShelfItem("telur", 10, 20),
        ),
        mutableListOf(
            ShelfItem("mi_instan", 18, 20),
            ShelfItem("air", 16, 20),
        ),
    )
    var shelfLevel: Int = 1

    // Gudang: stok cadangan sebelum dipindah ke rak (BAB 4).
    val warehouse: MutableMap<String, Int> = mutableMapOf(
        "beras" to 5, "mi_instan" to 12, "minyak" to 8, "air" to 10, "telur" to 10
    )
    var warehouseLevel: Int = 1
    val warehouseCapacity: Int get() = 200 + (warehouseLevel - 1) * 150

    // Pelanggan aktif di layar.
    val customers: MutableList<Customer> = CopyOnWriteArrayList()
    var customersServedToday: Int = 0
    var totalCustomersServed: Int = 0
    var totalProductsSold: Int = 0
    var restockCount: Int = 0

    // Misi (BAB 9)
    var missions: MutableList<Mission> = MissionBank.starter().toMutableList()
    var dailyMissions: MutableList<Mission> = MissionBank.daily().toMutableList()
    var lastDailyResetDay: Int = 1

    // Statistik pendapatan (untuk UI)
    var sessionIncome: Long = 0
    var lastIncome: Int = 0
    var lastSavedAt: Long = 0

    // === EXP & Level (BAB 8) ===
    fun expToNext(): Int = level * 100

    fun addExp(amount: Int) {
        exp += amount
        while (exp >= expToNext()) {
            exp -= expToNext()
            levelUp()
        }
    }

    /** Naik level (BAB 8): buka fitur baru + bonus. */
    private fun levelUp() {
        level++
        // Bonus level up: uang, diamond, energi, booster
        money += 50_000L * level
        diamond += 1
        energy = minOf(100, energy + 20)
        if (level % 3 == 0) booster += 1
        notifyLevelUp()
    }

    var onLevelUp: ((Int) -> Unit)? = null
    private fun notifyLevelUp() { onLevelUp?.invoke(level) }

    // === Transaksi (BAB 3 & BAB 5) ===
    fun canServe(): Boolean = shopOpen

    /**
     * Menyelesaikan transaksi pelanggan. Menghasilkan uang + EXP + reputasi,
     * dan menambah progress misi.
     */
    fun completePurchase(c: Customer) {
        val s = seller
        val profitMult = s?.profitMultiplier() ?: 1f
        val totalBill = c.bill
        val baseProfit = (totalBill * profitMult).toInt()
        val satisfactionBonus = c.satisfactionBonus()
        val earned = baseProfit + satisfactionBonus * 500

        money += earned
        sessionIncome += earned
        lastIncome = earned
        reputation += (s?.reputationBonus() ?: 1)
        addExp(10 + (s?.expBonus() ?: 0))

        customersServedToday++
        totalCustomersServed++
        totalProductsSold += c.pickedItems

        updateMissions(MissionType.SERVE_CUSTOMERS, 1)
        updateMissions(MissionType.LEVEL_UP, level)
        c.shoppingList.forEach { pid ->
            updateMissions(MissionType.SELL_PRODUCT, 1, pid)
        }
    }

    // === Stok (BAB 4) ===
    /** Beli stok ke supplier: uang berkurang, masuk ke gudang. */
    fun buyStock(productId: String, qty: Int): Boolean {
        val product = ProductCatalog.byId(productId)
        val cost = product.buyPrice * qty
        if (money < cost) return false
        val currentTotal = warehouse.values.sum()
        if (currentTotal + qty > warehouseCapacity) return false
        money -= cost
        warehouse.merge(productId, qty) { a, b -> a + b }
        restockCount++
        updateMissions(MissionType.RESTOCK, 1)
        return true
    }

    /** Pindahkan stok dari gudang ke rak. */
    fun restockShelf(shelfIndex: Int, slotIndex: Int): Boolean {
        val shelf = shelves.getOrNull(shelfIndex) ?: return false
        val item = shelf.getOrNull(slotIndex) ?: return false
        val inWarehouse = warehouse[item.productId] ?: 0
        if (inWarehouse <= 0) return false
        val space = item.capacity - item.stock
        if (space <= 0) return false
        val move = minOf(inWarehouse, space)
        item.stock += move
        warehouse[item.productId] = inWarehouse - move
        return true
    }

    /** Ambil barang dari rak oleh pelanggan. */
    fun takeFromShelf(productId: String, qty: Int = 1): Int {
        var taken = 0
        for (shelf in shelves) {
            for (item in shelf) {
                if (item.productId == productId && item.stock > 0) {
                    val take = minOf(item.stock, qty - taken)
                    item.stock -= take
                    taken += take
                    if (taken >= qty) return taken
                }
            }
        }
        return taken
    }

    fun shelfHas(productId: String): Boolean =
        shelves.any { it.any { it.productId == productId && it.stock > 0 } }

    // === Upgrade (BAB 6) ===
    fun upgradeShelfCost(): Int {
        val next = ShelfConfig.next(shelfLevel) ?: return -1
        val mult = seller?.upgradeCostMultiplier() ?: 1f
        return (next.upgradeCost * mult).toInt()
    }

    fun upgradeShelf(): Boolean {
        val cost = upgradeShelfCost()
        if (cost < 0 || money < cost) return false
        money -= cost
        shelfLevel++
        val cap = ShelfConfig.get(shelfLevel).capacity
        // perbesar kapasitas semua slot
        shelves.forEach { it.forEach { slot -> slot.copy(capacity = cap) } }
        reputation += ShelfConfig.get(shelfLevel).aesthetic
        updateMissions(MissionType.UPGRADE, 1)
        return true
    }

    fun upgradeWarehouseCost(): Int {
        val mult = seller?.upgradeCostMultiplier() ?: 1f
        return ((1_500_000 * warehouseLevel) * mult).toInt()
    }

    fun upgradeWarehouse(): Boolean {
        val cost = upgradeWarehouseCost()
        if (money < cost) return false
        money -= cost
        warehouseLevel++
        return true
    }

    // === Misi (BAB 9) ===
    fun updateMissions(type: MissionType, amount: Int, productId: String? = null) {
        val all = (missions.asSequence() + dailyMissions.asSequence())
        for (m in all) {
            if (m.completed || m.claimed) continue
            if (!missionMatches(m, type, productId)) continue
            m.progress = minOf(m.target, m.progress + amount)
            if (m.progress >= m.target) m.completed = true
        }
    }

    private fun missionMatches(m: Mission, type: MissionType, productId: String?): Boolean {
        val mt = m.type()
        if (mt != type) return false
        // Untuk misi jual produk spesifik, cek id produk (mi_instan).
        if (type == MissionType.SELL_PRODUCT && m.id.contains("mi_50")) {
            return productId == "mi_instan"
        }
        return true
    }

    fun claimMission(m: Mission): Boolean {
        if (!m.isReady()) return false
        money += m.rewardMoney
        diamond += m.rewardDiamond
        addExp(m.rewardExp)
        booster += m.rewardBooster
        chest += m.rewardChest
        m.claimed = true
        return true
    }

    /** Reset misi harian jika hari berganti (BAB 9). */
    fun checkDailyReset() {
        if (day != lastDailyResetDay) {
            dailyMissions = MissionBank.daily().map { it.copy() }.toMutableList()
            lastDailyResetDay = day
        }
    }

    /** Buka warung (BAB 2). */
    fun openShop() { shopOpen = true }
    fun closeShop() { shopOpen = false }

    /** Pendapatan idle (BAB 5): kasir tetap bekerja walau offline. */
    fun applyOfflineIncome(secondsAway: Long): Long {
        val rate = idleRatePerSecond()
        val income = (rate * secondsAway * (seller?.offlineIncomeMultiplier() ?: 1f)).toLong()
        money += income
        sessionIncome += income
        return income
    }

    /** Estimasi pendapatan idle per detik berdasar level & reputasi. */
    fun idleRatePerSecond(): Float {
        val base = (level * 50 + reputation * 5)
        val customerBonus = totalCustomersServed.coerceAtMost(500) * 2
        return (base + customerBonus).toFloat()
    }
}
