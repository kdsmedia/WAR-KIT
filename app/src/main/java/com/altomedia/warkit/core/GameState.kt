package com.altomedia.warkit.core

import com.altomedia.warkit.data.MissionBank
import com.altomedia.warkit.data.ProductCatalog
import com.altomedia.warkit.data.ShelfConfig
import com.altomedia.warkit.data.type
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.Employee
import com.altomedia.warkit.model.Mission
import com.altomedia.warkit.model.MissionType
import com.altomedia.warkit.model.ReputationTier
import com.altomedia.warkit.model.SellerCharacter
import com.altomedia.warkit.model.ShelfItem
import com.altomedia.warkit.model.Supplier
import com.altomedia.warkit.model.TimeOfDay
import com.altomedia.warkit.model.Weather
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
    // BAB 12: 100/250/500 slot (+ lanjutan)
    val warehouseCapacity: Int get() = when (warehouseLevel) {
        1 -> 100
        2 -> 250
        3 -> 500
        else -> 500 + (warehouseLevel - 3) * 250
    }
    fun upgradeWarehouseCost(): Int {
        val mult = seller?.upgradeCostMultiplier() ?: 1f
        return ((1_500_000 * warehouseLevel) * mult).toInt()
    }

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

    // === BAB 11: Pegawai ===
    val employees: MutableList<Employee> = mutableListOf()
    fun hasCashier(): Boolean = employees.any { it.role == Employee.Role.CASHIER }
    fun workSpeedMult(): Float = employees.fold(1f) { acc, e -> acc * e.workSpeed }
    fun employeeFriendliness(): Int = employees.sumOf { it.friendliness }
    fun employeeEfficiency(): Int = employees.sumOf { it.efficiency }

    // === BAB 13: Supplier ===
    var supplier: Supplier = Supplier.DISTRIBUTOR_DESA
    fun supplierUnlocked(level: Int): List<Supplier> =
        Supplier.entries.filter { it.unlockLevel <= level }

    // === BAB 15: Dekorasi ===
    val decorations: MutableList<String> = mutableListOf()
    fun decorationReputation(): Int = decorations.sumOf {
        com.altomedia.warkit.data.DecorationBank.byId(it).reputationBoost
    }
    fun decorationVipBoost(): Float = decorations.fold(0f) { acc, id ->
        acc + com.altomedia.warkit.data.DecorationBank.byId(id).vipBoost
    }

    // === BAB 18: Waktu ===
    var timeOfDay: TimeOfDay = TimeOfDay.PAGI
    var periodTimer: Float = 0f  // detik tersisa periode ini

    // === BAB 19: Cuaca ===
    var weather: Weather = Weather.CERAH
    var nextWeatherChange: Int = 3  // ganti cuaca tiap N hari

    // === BAB 16: VIP tracking ===
    var totalVipServed: Int = 0

    /** Tier reputasi saat ini (BAB 17). */
    fun reputationTier(): ReputationTier = ReputationTier.at(
        reputation + decorationReputation()
    )

    /** Peluang VIP total: karakter + tier reputasi + dekorasi (BAB 16 & 17). */
    fun vipChance(): Float {
        val base = (seller?.vipChance() ?: 0.03f)
        val tier = reputationTier().vipChanceBonus
        return base + tier + decorationVipBoost()
    }

    /** Multiplier jumlah pelanggan: tier reputasi * cuaca (BAB 17 & 19). */
    fun customerSpawnMult(): Float =
        reputationTier().customerMult * weather.customerMult()

    /** Biaya operasional harian pegawai (dikurangi efisiensi). */
    fun dailyWage(): Int {
        val raw = employees.sumOf { it.dailyWage }
        val discount = raw * employeeEfficiency() / 100
        return (raw - discount).coerceAtLeast(0)
    }

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
     * dan menambah progress misi. Pegawai (BAB 11) menambah kecepatan & reputasi.
     * VIP (BAB 16) memberi bonus diamond & reputasi ekstra.
     */
    fun completePurchase(c: Customer) {
        val s = seller
        val profitMult = s?.profitMultiplier() ?: 1f
        // Efisiensi pegawai mengurangi biaya -> menambah margin (BAB 11)
        val effBonus = 1f + (employeeEfficiency() / 100f)
        val totalBill = c.bill
        val baseProfit = (totalBill * profitMult * effBonus).toInt()
        val satisfactionBonus = c.satisfactionBonus()
        var earned = baseProfit + satisfactionBonus * 500

        money += earned
        sessionIncome += earned
        lastIncome = earned
        val repGain = (s?.reputationBonus() ?: 1) + employeeFriendliness() + satisfactionBonus
        reputation += repGain
        addExp(10 + (s?.expBonus() ?: 0))

        // BAB 16: bonus VIP
        if (c.isVip) {
            earned += 50_000
            money += 50_000
            sessionIncome += 50_000
            diamond += 1
            reputation += 5
            totalVipServed++
        }

        customersServedToday++
        totalCustomersServed++
        totalProductsSold += c.pickedItems

        // Pelayanan buruk (MARAH) menurunkan reputasi (BAB 17)
        if (c.satisfaction == com.altomedia.warkit.model.Satisfaction.MARAH) {
            reputation = (reputation - 2).coerceAtLeast(0)
        }

        updateMissions(MissionType.SERVE_CUSTOMERS, 1)
        updateMissions(MissionType.LEVEL_UP, level)
        c.shoppingList.forEach { pid ->
            updateMissions(MissionType.SELL_PRODUCT, 1, pid)
        }
    }

    // === Stok (BAB 4 & BAB 13: supplier) ===
    /** Beli stok ke supplier: uang berkurang, masuk ke gudang. */
    fun buyStock(productId: String, qty: Int): Boolean {
        val product = ProductCatalog.byId(productId)
        val unitPrice = supplier.effectiveBuy(product.buyPrice)  // BAB 13
        val cost = unitPrice * qty
        if (money < cost) return false
        val currentTotal = warehouse.values.sum()
        if (currentTotal + qty > warehouseCapacity) return false
        money -= cost
        warehouse.merge(productId, qty) { a, b -> a + b }
        restockCount++
        updateMissions(MissionType.RESTOCK, 1)
        return true
    }

    /** Harga beli efektif produk (untuk ditampilkan di UI). */
    fun effectiveBuyPrice(productId: String): Int {
        val product = ProductCatalog.byId(productId)
        return supplier.effectiveBuy(product.buyPrice)
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

    fun upgradeWarehouse(): Boolean {
        val cost = upgradeWarehouseCost()
        if (money < cost) return false
        money -= cost
        warehouseLevel++
        return true
    }

    // === BAB 11: Pegawai ===
    fun hireEmployee(emp: Employee): Boolean {
        if (employees.any { it.id == emp.id }) return false
        if (money < emp.hireCost) return false
        money -= emp.hireCost
        employees.add(emp)
        return true
    }

    fun fireEmployee(emp: Employee) { employees.remove(emp) }

    // === BAB 13: Supplier ===
    fun unlockSupplier(s: Supplier): Boolean {
        if (s.unlockLevel > level) return false
        if (supplier.ordinal >= s.ordinal) return false
        if (money < s.unlockCost) return false
        money -= s.unlockCost
        supplier = s
        return true
    }

    // === BAB 15: Dekorasi ===
    fun buyDecoration(id: String): Boolean {
        if (decorations.contains(id)) return false
        val d = com.altomedia.warkit.data.DecorationBank.byId(id)
        if (money < d.cost) return false
        money -= d.cost
        decorations.add(id)
        reputation += d.reputationBoost
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

    /** Estimasi pendapatan idle per detik berdasar level & reputasi (BAB 5 & 11). */
    fun idleRatePerSecond(): Float {
        val base = (level * 50 + reputation * 5)
        val customerBonus = totalCustomersServed.coerceAtMost(500) * 2
        // Pegawai & tier reputasi meningkatkan idle income
        val empMult = workSpeedMult()
        val tierMult = reputationTier().customerMult
        return ((base + customerBonus) * empMult * tierMult)
    }

    /** Maju ke hari berikutnya: gaji pegawai, reset cuaca, cek misi harian. */
    fun advanceDay() {
        day++
        val wage = dailyWage()
        money -= wage
        customersServedToday = 0
        // Ganti cuaca secara periodik (BAB 19)
        if (day % nextWeatherChange == 0) {
            weather = Weather.entries.random()
        }
        checkDailyReset()
    }

    /** Update waktu siang/malam (BAB 18). Dipanggil tiap detik dari engine. */
    fun tickTimeOfDay(dt: Float) {
        periodTimer += dt
        if (periodTimer >= TimeOfDay.PERIOD_SECONDS) {
            periodTimer = 0f
            val idx = TimeOfDay.cycle.indexOf(timeOfDay)
            timeOfDay = TimeOfDay.cycle[(idx + 1) % TimeOfDay.cycle.size]
        }
    }
}
