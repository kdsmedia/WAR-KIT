package com.altomedia.warkit.core

import com.altomedia.warkit.data.MissionBank
import com.altomedia.warkit.data.ProductCatalog
import com.altomedia.warkit.data.SecurityBank
import com.altomedia.warkit.data.ShelfConfig
import com.altomedia.warkit.data.type
import com.altomedia.warkit.model.Branch
import com.altomedia.warkit.model.CashRegister
import com.altomedia.warkit.model.CompetitionState
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.Employee
import com.altomedia.warkit.model.Mission
import com.altomedia.warkit.model.MissionType
import com.altomedia.warkit.model.Promotion
import com.altomedia.warkit.model.ReputationTier
import com.altomedia.warkit.model.SeasonalEvent
import com.altomedia.warkit.model.SellerCharacter
import com.altomedia.warkit.model.ShelfItem
import com.altomedia.warkit.model.Supplier
import com.altomedia.warkit.model.TimeOfDay
import com.altomedia.warkit.model.Vehicle
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

    // === BAB 21: Cabang ===
    val branches: MutableList<Branch> = mutableListOf()
    fun openBranch(b: Branch): Boolean {
        if (branches.any { it.id == b.id }) return false
        if (level < b.unlockLevel) return false
        if (money < b.openCost) return false
        money -= b.openCost
        branches.add(b.copy())
        return true
    }
    fun branchIncomePerSecond(): Long =
        branches.sumOf { (it.totalIncome / (day.coerceAtLeast(1))).coerceAtLeast(0L) }

    // === BAB 22: Manajer cabang (Pak Hendra) ===
    fun upgradeBranchManager(b: Branch): Boolean {
        val cost = 500_000 * (b.managerLevel + 1)
        if (money < cost || b.managerLevel >= 10) return false
        money -= cost
        b.managerLevel++
        return true
    }

    // === BAB 23: Kendaraan pengiriman ===
    var vehicleLevel: Int = 0  // ordinal Vehicle
    fun vehicle(): Vehicle = Vehicle.at(vehicleLevel)
    fun upgradeVehicle(): Boolean {
        val next = Vehicle.entries.getOrNull(vehicleLevel + 1) ?: return false
        if (level < next.unlockLevel || money < next.unlockCost) return false
        money -= next.unlockCost
        vehicleLevel++
        return true
    }

    // === BAB 25: Mesin kasir ===
    var cashRegisterLevel: Int = 1
    fun cashRegister(): CashRegister = CashRegister.at(cashRegisterLevel)
    fun upgradeCashRegister(): Boolean {
        val next = CashRegister.entries.getOrNull(cashRegisterLevel) ?: return false
        if (level < next.unlockLevel || money < next.upgradeCost) return false
        money -= next.upgradeCost
        cashRegisterLevel++
        return true
    }

    // === BAB 26: Promosi ===
    var promotion: Promotion = Promotion.TIDAK_ADA
    var promotionDaysLeft: Int = 0
    fun startPromotion(p: Promotion): Boolean {
        if (p == Promotion.TIDAK_ADA) { promotion = p; promotionDaysLeft = 0; return true }
        if (money < p.cost) return false
        money -= p.cost
        promotion = p
        promotionDaysLeft = p.durationDays
        return true
    }

    // === BAB 27: Persaingan bisnis ===
    var competition: CompetitionState = CompetitionState()
    fun playerShopScore(): Float {
        // Gabungan: reputasi, kebersihan(pegawai cleaner), kelengkapan produk, dekorasi, harga(supplier)
        val rep = (reputationTier().ordinal + 1) / 6f
        val cleaner = if (employees.any { it.role == Employee.Role.CLEANER }) 0.2f else 0f
        val products = (ProductCatalog.unlocked(level).size / 30f).coerceAtMost(1f)
        val decor = (decorations.size / 6f).coerceAtMost(1f)
        val supplierScore = (supplier.ordinal / 3f)
        return ((rep + cleaner + products + decor + supplierScore) / 5f).coerceIn(0f, 1f)
    }

    // === BAB 28: Keamanan ===
    val security: MutableList<String> = mutableListOf()
    fun buySecurity(id: String): Boolean {
        if (security.contains(id)) return false
        val s = SecurityBank.byId(id)
        if (level < s.unlockLevel || money < s.cost) return false
        money -= s.cost
        security.add(id)
        reputation += s.reputationBoost
        return true
    }
    fun theftReduction(): Float = security.fold(0f) { acc, id ->
        acc + SecurityBank.byId(id).theftReduction
    }

    // === BAB 29/30: Event musiman ===
    var seasonalEvent: SeasonalEvent = SeasonalEvent.NONE
    var eventDaysLeft: Int = 0
    fun startEvent(ev: SeasonalEvent, durationDays: Int = 5) {
        seasonalEvent = ev
        eventDaysLeft = durationDays
    }
    fun endEvent() { seasonalEvent = SeasonalEvent.NONE; eventDaysLeft = 0 }

    // === BAB 31: Investor ===
    var investorActive: Boolean = false
    var investorDaysElapsed: Int = 0
    var investorDaysMet: Int = 0
    var investorDealAccepted: Boolean = false
    var investorIncomeMult: Float = 1f
    var investorConstructionDiscount: Float = 0f
    var investorTargetDailyIncome: Long = 1_000_000
    var investorTargetCustomers: Int = 100
    var investorTargetReputation: Int = 50
    fun startInvestor(): Boolean {
        if (investorActive || investorDealAccepted) return false
        investorActive = true; investorDaysElapsed = 0; investorDaysMet = 0
        return true
    }
    /** Cek harian target investor (dipanggil di advanceDay). */
    fun tickInvestor(dailyIncome: Long, dailyCustomers: Int) {
        if (!investorActive) return
        investorDaysElapsed++
        val met = dailyIncome >= investorTargetDailyIncome &&
            dailyCustomers >= investorTargetCustomers &&
            reputation >= investorTargetReputation
        if (met) investorDaysMet++
        if (investorDaysElapsed >= 30) {
            // Selesai: terima deal jika memenuhi >= 80% hari
            if (investorDaysMet >= 24) acceptInvestorDeal()
            investorActive = false
        }
    }
    private fun acceptInvestorDeal() {
        investorDealAccepted = true
        investorIncomeMult = 1.25f
        investorConstructionDiscount = 0.15f
        money += 10_000_000  // tambahan modal
        reputation += 20
    }

    // === BAB 32: Toko Grosir ===
    var grosirUnlocked: Boolean = false
    var grosirCustomersToday: Int = 0
    fun unlockGrosir(): Boolean {
        if (grosirUnlocked || level < 12) return false
        grosirUnlocked = true; return true
    }
    /** Pendapatan grosir harian (pembeli grosir belanja besar). */
    fun grosirDailyIncome(): Long {
        if (!grosirUnlocked) return 0
        val base = 200_000L * (warehouseLevel)
        return (base * investorIncomeMult).toLong()
    }

    // === BAB 33/34: Logistik & gudang distribusi ===
    var distWarehouseLevel: Int = 0
    fun upgradeDistWarehouse(): Boolean {
        val cost = 3_000_000L * (distWarehouseLevel + 1)
        if (money < cost) return false
        money -= cost; distWarehouseLevel++
        return true
    }
    fun distCapacity(): Int = 500 * (distWarehouseLevel + 1)
    fun distSpeed(): Float = 1f + (distWarehouseLevel * 0.2f)

    // === BAB 35: Kota (biaya operasional lebih tinggi) ===
    var cityBranchesUnlocked: Boolean = false
    fun cityMultiplier(): Float = if (cityBranchesUnlocked) 1.5f else 1f

    // === BAB 36: Pajak & biaya operasional ===
    fun dailyTax(): Long {
        val baseTax = (sessionIncome / 100) * 5  // ~5% pendapatan
        val cityTax = (baseTax * cityMultiplier()).toLong()
        val branchTax = branches.size * 50_000L
        return cityTax + branchTax
    }

    // === BAB 37: Pelatihan pegawai ===
    val trainedEmployees: MutableMap<String, MutableList<com.altomedia.warkit.model.TrainingType>> = mutableMapOf()
    fun trainEmployee(empId: String, t: com.altomedia.warkit.model.TrainingType): Boolean {
        if (money < t.cost) return false
        money -= t.cost
        trainedEmployees.getOrPut(empId) { mutableListOf() }.add(t)
        return true
    }
    fun trainingSpeedBoost(): Float = trainedEmployees.values.flatten()
        .sumOf { it.speedBoost.toDouble() }.toFloat()
    fun trainingSatisfactionBonus(): Int =
        trainedEmployees.values.flatten().sumOf { it.satisfactionBoost }
    fun trainingEfficiencyBonus(): Int =
        trainedEmployees.values.flatten().sumOf { it.efficiencyBoost }
    fun trainingIncomeMult(): Float = 1f + trainedEmployees.values.flatten()
        .sumOf { it.incomeBoost.toDouble() }.toFloat()

    // === BAB 39: Bangunan & fasilitas ===
    var buildingLevel: Int = 1
    fun building(): com.altomedia.warkit.model.BuildingLevel =
        com.altomedia.warkit.model.BuildingLevel.at(buildingLevel)
    fun upgradeBuilding(): Boolean {
        val next = com.altomedia.warkit.model.BuildingLevel.entries.getOrNull(buildingLevel) ?: return false
        val cost = (next.upgradeCost * (1f - investorConstructionDiscount)).toLong()
        if (level < next.unlockLevel || money < cost) return false
        money -= cost; buildingLevel++
        reputation += next.reputationBoost
        return true
    }
    val facilities: MutableList<String> = mutableListOf()
    fun buyFacility(id: String): Boolean {
        if (facilities.contains(id)) return false
        val f = com.altomedia.warkit.data.FacilityBank.byId(id)
        if (level < f.unlockLevel || money < f.cost) return false
        money -= f.cost; facilities.add(id); reputation += f.reputationBoost
        return true
    }
    fun facilityComfort(): Float = facilities.fold(0f) { acc, id ->
        acc + com.altomedia.warkit.data.FacilityBank.byId(id).comfortBoost
    }

    // === BAB 41: Provinsi ===
    val provinces: MutableList<String> = mutableListOf()
    fun openProvince(id: String): Boolean {
        if (provinces.contains(id)) return false
        val p = com.altomedia.warkit.data.ProvinceBank.byId(id)
        if (level < p.unlockLevel || money < p.unlockCost) return false
        money -= p.unlockCost
        provinces.add(id)
        return true
    }
    fun provinceCustomerMult(): Float {
        if (provinces.isEmpty()) return 1f
        return provinces.fold(1f) { acc, id ->
            acc * com.altomedia.warkit.data.ProvinceBank.byId(id).customerMult
        } / provinces.size.coerceAtLeast(1)
    }
    fun provinceOpCostMult(): Float {
        if (provinces.isEmpty()) return 1f
        return provinces.fold(0f) { acc, id ->
            acc + com.altomedia.warkit.data.ProvinceBank.byId(id).opCostMult
        } / provinces.size
    }

    // === BAB 43: Program member ===
    var membershipTier: com.altomedia.warkit.model.MembershipTier =
        com.altomedia.warkit.model.MembershipTier.NONE
    fun launchMembership(t: com.altomedia.warkit.model.MembershipTier): Boolean {
        if (money < t.cost || level < t.unlockLevel) return false
        money -= t.cost
        membershipTier = t
        return true
    }

    // === BAB 44: Pembayaran digital ===
    var paymentLevel: Int = 0
    fun paymentMethod(): com.altomedia.warkit.model.PaymentMethod =
        com.altomedia.warkit.model.PaymentMethod.at(paymentLevel)
    fun upgradePayment(): Boolean {
        val next = com.altomedia.warkit.model.PaymentMethod.entries.getOrNull(paymentLevel + 1) ?: return false
        if (level < next.unlockLevel || money < next.upgradeCost) return false
        money -= next.upgradeCost
        paymentLevel++
        return true
    }

    // === BAB 45: Pusat distribusi nasional ===
    var nationalDist: com.altomedia.warkit.model.NationalDistribution =
        com.altomedia.warkit.model.NationalDistribution()
    fun activateNationalDist(): Boolean {
        if (nationalDist.active) return false
        val cost = 50_000_000L
        if (money < cost || level < 23) return false
        money -= cost
        nationalDist.active = true
        return true
    }
    fun upgradeNationalDist(): Boolean {
        if (!nationalDist.active) return false
        val cost = nationalDist.upgradeCost()
        if (money < cost) return false
        money -= cost
        nationalDist.level++
        return true
    }

    // === BAB 46: Krisis pasokan ===
    var supplyCrisis: com.altomedia.warkit.model.SupplyCrisis =
        com.altomedia.warkit.model.SupplyCrisis()
    fun triggerCrisis() {
        if (supplyCrisis.active) return
        supplyCrisis.active = true
        supplyCrisis.daysLeft = 3
        supplyCrisis.severity = (0.3f + kotlin.random.Random.nextFloat() * 0.4f)
    }
    fun resolveCrisisAlternativeSupplier(): Boolean {
        val cost = (2_000_000L * (1 + supplyCrisis.severity)).toLong()
        if (money < cost) return false
        money -= cost
        supplyCrisis.resolve()
        return true
    }
    fun resolveCrisisTransferStock(): Boolean {
        // Transfer dari cabang lain — butuh >= 1 cabang
        if (branches.isEmpty()) return false
        supplyCrisis.resolve()
        return true
    }
    fun resolveCrisisReducePromo() {
        promotion = com.altomedia.warkit.model.Promotion.TIDAK_ADA
        promotionDaysLeft = 0
        supplyCrisis.resolve()
    }

    // === BAB 47: Penghargaan nasional ===
    var nationalAwardReceived: Boolean = false
    fun checkNationalAward(): Boolean {
        if (nationalAwardReceived) return false
        if (reputation >= 1500 && branches.size >= 10 && provinces.size >= 5) {
            nationalAwardReceived = true
            reputation += 100
            return true
        }
        return false
    }

    // === BAB 48: Persaingan nasional ===
    var nationalCompetitionActive: Boolean = false
    fun nationalRetentionRate(): Float {
        if (!nationalCompetitionActive) return 1f
        val score = playerShopScore()
        return (0.5f + score * 0.5f).coerceIn(0.3f, 1f)
    }

    // === BAB 49: Raja Warung ===
    var rajaWarungTitle: Boolean = false
    fun checkRajaWarung(): Boolean {
        if (rajaWarungTitle) return false
        if (branches.size >= 100 && totalCustomersServed >= 10000 && reputation >= 3000) {
            rajaWarungTitle = true
            return true
        }
        return false
    }

    // === BAB 50: Endless Empire ===
    var endlessEmpireUnlocked: Boolean = false
    val achievements: MutableList<com.altomedia.warkit.model.Achievement> =
        com.altomedia.warkit.data.AchievementBank.all.map { it.copy() }.toMutableList()
    var prestige: Int = 0
    fun unlockEndlessEmpire() { endlessEmpireUnlocked = true }
    fun updateAchievements() {
        achievements.find { it.id == "first_customer" }?.let { it.progress = if (totalCustomersServed >= 1) 1 else 0 }
        achievements.find { it.id == "serve_100" }?.let { it.progress = totalCustomersServed }
        achievements.find { it.id == "serve_1000" }?.let { it.progress = totalCustomersServed }
        achievements.find { it.id == "serve_10000" }?.let { it.progress = totalCustomersServed }
        achievements.find { it.id == "income_1m" }?.let { it.progress = totalIncomeEarned.toInt() }
        achievements.find { it.id == "income_100m" }?.let { it.progress = totalIncomeEarned.toInt() }
        achievements.find { it.id == "income_1b" }?.let { it.progress = totalIncomeEarned.toInt() }
        achievements.find { it.id == "vip_50" }?.let { it.progress = totalVipServed }
        achievements.find { it.id == "branch_10" }?.let { it.progress = branches.size }
        achievements.find { it.id == "branch_100" }?.let { it.progress = branches.size }
        achievements.find { it.id == "province_all" }?.let { it.progress = provinces.size }
        achievements.find { it.id == "employee_50" }?.let { it.progress = totalEmployeesHired }
        achievements.find { it.id == "rep_legendary" }?.let {
            it.progress = if (reputationTier() == com.altomedia.warkit.model.ReputationTier.WARUNG_LEGENDARIS) 1 else 0
        }
    }
    var totalIncomeEarned: Long = 0
    var totalEmployeesHired: Int = 0

    /** Tier reputasi saat ini (BAB 17). */
    fun reputationTier(): ReputationTier = ReputationTier.at(
        reputation + decorationReputation()
    )

    /** Peluang VIP total: karakter + tier reputasi + dekorasi + event (BAB 16,17,29,30). */
    fun vipChance(): Float {
        val base = (seller?.vipChance() ?: 0.03f)
        val tier = reputationTier().vipChanceBonus
        return base + tier + decorationVipBoost() + seasonalEvent.vipChanceBonus
    }

    /** Multiplier jumlah pelanggan: tier * cuaca * event * promosi * kompetisi * provinsi (BAB 17,19,26,27,29,30,41,48). */
    fun customerSpawnMult(): Float {
        val tier = reputationTier().customerMult
        val weather = weather.customerMult()
        val event = seasonalEvent.customerMult
        val promo = promotion.customerMult
        val competition = competition.retentionRate() * nationalRetentionRate()
        val province = provinceCustomerMult()
        return tier * weather * event * promo * competition * province
    }

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
     * Promosi (BAB 26) menaikkan nilai belanja tapi mengurangi margin.
     * Mesin kasir (BAB 25) & event (BAB 29/30) memengaruhi pendapatan.
     */
    fun completePurchase(c: Customer) {
        val s = seller
        val profitMult = s?.profitMultiplier() ?: 1f
        // Efisiensi pegawai mengurangi biaya -> menambah margin (BAB 11)
        val effBonus = 1f + (employeeEfficiency() / 100f)
        // BAB 26: promosi menaikkan nilai belanja & mengurangi margin
        // BAB 43: member menaikkan nilai transaksi
        val billMult = promotion.billMult * seasonalEvent.billMult * building().transactionMult *
            membershipTier.billMult
        val profitPenalty = promotion.profitPenalty
        val totalBill = (c.bill * billMult).toInt()
        val baseProfit = (totalBill * profitMult * effBonus * (1f - profitPenalty) *
            investorIncomeMult * trainingIncomeMult()).toInt()
        val satisfactionBonus = c.satisfactionBonus() + trainingSatisfactionBonus()
        var earned = baseProfit + satisfactionBonus * 500

        money += earned
        sessionIncome += earned
        totalIncomeEarned += earned
        lastIncome = earned
        val repGain = (s?.reputationBonus() ?: 1) + employeeFriendliness() +
            satisfactionBonus + promotion.reputationBoost
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

    /** Maju ke hari berikutnya: gaji pegawai, reset cuaca, cek misi harian, BAB 26/29/30. */
    fun advanceDay() {
        day++
        val wage = dailyWage()
        money -= wage
        customersServedToday = 0
        // Ganti cuaca secara periodik (BAB 19)
        if (day % nextWeatherChange == 0) {
            weather = Weather.entries.random()
        }
        // BAB 26: promosi berkurang harian
        if (promotionDaysLeft > 0) {
            promotionDaysLeft--
            if (promotionDaysLeft == 0) promotion = Promotion.TIDAK_ADA
        }
        // BAB 29/30: event musiman berkurang harian
        if (eventDaysLeft > 0) {
            eventDaysLeft--
            if (eventDaysLeft == 0) endEvent()
        }
        // BAB 21: kirim keuntungan cabang ke saldo utama
        val branchEarn = branchIncomePerSecond() * 120  // ~1 hari gameplay
        if (branchEarn > 0) {
            money += branchEarn
            sessionIncome += branchEarn
        }
        // BAB 27: kompetitor muncul di level tertentu
        if (level >= 12 && !competition.competitorActive) {
            competition.competitorActive = true
            competition.competitorStrength = 0.3f
        }
        competition.playerShopScore = playerShopScore()
        // BAB 32: pendapatan grosir harian
        val grosirEarn = grosirDailyIncome()
        if (grosirEarn > 0) {
            money += grosirEarn
            sessionIncome += grosirEarn
        }
        // BAB 35: kota terbuka di level 14
        if (level >= 14) cityBranchesUnlocked = true
        // BAB 36: bayar pajak & biaya operasional harian
        val tax = dailyTax()
        if (tax > 0) money -= tax
        // BAB 31: cek target investor harian
        tickInvestor(dailyIncome = (sessionIncome / day.coerceAtLeast(1)),
            dailyCustomers = customersServedToday)
        // BAB 46: krisis pasokan acak (level 20+, ~5% chance/hari)
        if (level >= 20 && !supplyCrisis.active && kotlin.random.Random.nextFloat() < 0.05f) {
            triggerCrisis()
        }
        if (supplyCrisis.active) {
            supplyCrisis.daysLeft--
            if (supplyCrisis.daysLeft <= 0 && supplyCrisis.active) {
                // krisis tidak ditangani: pendapatan turun
                money -= (1_000_000L * (1 + supplyCrisis.severity).toInt())
                supplyCrisis.resolve()
            }
        }
        // BAB 47: cek penghargaan nasional
        checkNationalAward()
        // BAB 48: persaingan nasional aktif di level 25+
        if (level >= 25) nationalCompetitionActive = true
        // BAB 49: cek gelar Raja Warung
        checkRajaWarung()
        // BAB 50: update achievement & unlock endless empire
        updateAchievements()
        if (rajaWarungTitle) unlockEndlessEmpire()
        checkDailyReset()
    }

    /** BAB 24: update masa simpan produk segar di rak; tandai busuk. */
    fun tickFreshness(dt: Float) {
        for (shelf in shelves) {
            for (item in shelf) {
                if (item.stock <= 0) { item.freshnessTimer = 0f; item.spoiled = false; continue }
                val product = ProductCatalog.byId(item.productId)
                if (product.isFresh && !item.spoiled) {
                    if (item.freshnessTimer <= 0f) item.freshnessTimer = product.shelfLifeSeconds
                    item.freshnessTimer -= dt
                    if (item.freshnessTimer <= 0f) {
                        item.spoiled = true
                        item.stock = 0  // produk busuk dibuang
                    }
                }
            }
        }
    }

    /** BAB 24: buang produk busuk (reset flag, ruang bebas). */
    fun discardSpoiled() {
        for (shelf in shelves) {
            for (item in shelf) {
                if (item.spoiled) { item.spoiled = false; item.freshnessTimer = 0f }
            }
        }
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
