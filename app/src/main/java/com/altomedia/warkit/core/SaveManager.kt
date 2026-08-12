package com.altomedia.warkit.core

import android.content.Context
import android.content.SharedPreferences
import com.altomedia.warkit.data.EmployeeBank
import com.altomedia.warkit.model.CashRegister
import com.altomedia.warkit.model.Promotion
import com.altomedia.warkit.model.SeasonalEvent
import com.altomedia.warkit.model.SellerCharacter
import com.altomedia.warkit.model.Supplier
import com.altomedia.warkit.model.TimeOfDay
import com.altomedia.warkit.model.Vehicle
import com.altomedia.warkit.model.Weather
import org.json.JSONArray
import org.json.JSONObject

/**
 * Save/load state via SharedPreferences (JSON sederhana).
 * Menangani pendapatan idle offline (BAB 5) berdasar waktu tersimpan.
 */
class SaveManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("warkit_save", Context.MODE_PRIVATE)

    fun save(state: GameState) {
        val now = System.currentTimeMillis()
        val json = JSONObject().apply {
            put("money", state.money)
            put("diamond", state.diamond)
            put("level", state.level)
            put("exp", state.exp)
            put("reputation", state.reputation)
            put("energy", state.energy)
            put("booster", state.booster)
            put("chest", state.chest)
            put("day", state.day)
            put("shelfLevel", state.shelfLevel)
            put("warehouseLevel", state.warehouseLevel)
            put("customersServedToday", state.customersServedToday)
            put("totalCustomersServed", state.totalCustomersServed)
            put("totalProductsSold", state.totalProductsSold)
            put("restockCount", state.restockCount)
            put("lastDailyResetDay", state.lastDailyResetDay)
            put("savedAt", now)
            put("seller", state.seller?.name ?: JSONObject.NULL)

            // Warehouse
            val wh = JSONObject()
            state.warehouse.forEach { (k, v) -> wh.put(k, v) }
            put("warehouse", wh)

            // Shelves
            val shelvesArr = JSONArray()
            state.shelves.forEach { shelf ->
                val arr = JSONArray()
                shelf.forEach { item ->
                    arr.put(JSONObject().put("p", item.productId).put("s", item.stock).put("c", item.capacity))
                }
                shelvesArr.put(arr)
            }
            put("shelves", shelvesArr)

            // Missions
            put("missions", missionsToJson(state.missions))
            put("dailyMissions", missionsToJson(state.dailyMissions))

            // BAB 11-19
            put("employees", JSONArray().also { arr ->
                state.employees.forEach { arr.put(it.id) }
            })
            put("supplier", state.supplier.name)
            put("decorations", JSONArray().also { arr ->
                state.decorations.forEach { arr.put(it) }
            })
            put("timeOfDay", state.timeOfDay.name)
            put("weather", state.weather.name)
            put("totalVipServed", state.totalVipServed)

            // BAB 21-30
            put("branches", JSONArray().also { arr ->
                state.branches.forEach { b ->
                    arr.put(JSONObject().apply {
                        put("id", b.id); put("balance", b.balance)
                        put("reputation", b.reputation); put("managerLevel", b.managerLevel)
                        put("totalIncome", b.totalIncome)
                    })
                }
            })
            put("vehicleLevel", state.vehicleLevel)
            put("cashRegisterLevel", state.cashRegisterLevel)
            put("promotion", state.promotion.name)
            put("promotionDaysLeft", state.promotionDaysLeft)
            put("competitorActive", state.competition.competitorActive)
            put("competitorStrength", state.competition.competitorStrength)
            put("security", JSONArray().also { arr -> state.security.forEach { arr.put(it) } })
            put("seasonalEvent", state.seasonalEvent.name)
            put("eventDaysLeft", state.eventDaysLeft)
        }
        prefs.edit().putString("state", json.toString()).putLong("savedAt", now).apply()
        state.lastSavedAt = now
    }

    fun load(state: GameState): Long {
        val raw = prefs.getString("state", null) ?: return 0L
        return try {
            val json = JSONObject(raw)
            val savedAt = json.optLong("savedAt", 0L)
            state.money = json.optLong("money", 500_000)
            state.diamond = json.optInt("diamond", 0)
            state.level = json.optInt("level", 1)
            state.exp = json.optInt("exp", 0)
            state.reputation = json.optInt("reputation", 0)
            state.energy = json.optInt("energy", 100)
            state.booster = json.optInt("booster", 0)
            state.chest = json.optInt("chest", 0)
            state.day = json.optInt("day", 1)
            state.shelfLevel = json.optInt("shelfLevel", 1)
            state.warehouseLevel = json.optInt("warehouseLevel", 1)
            state.customersServedToday = json.optInt("customersServedToday", 0)
            state.totalCustomersServed = json.optInt("totalCustomersServed", 0)
            state.totalProductsSold = json.optInt("totalProductsSold", 0)
            state.restockCount = json.optInt("restockCount", 0)
            state.lastDailyResetDay = json.optInt("lastDailyResetDay", 1)
            state.lastSavedAt = savedAt

            val sellerName = json.optString("seller", "")
            if (sellerName.isNotEmpty()) {
                state.seller = SellerCharacter.valueOf(sellerName)
            }

            // Warehouse
            state.warehouse.clear()
            val wh = json.optJSONObject("warehouse")
            wh?.keys()?.forEach { k -> state.warehouse[k] = wh.getInt(k) }

            // Shelves
            state.shelves.clear()
            val shelvesArr = json.optJSONArray("shelves")
            if (shelvesArr != null) {
                for (i in 0 until shelvesArr.length()) {
                    val arr = shelvesArr.getJSONArray(i)
                    val shelf = mutableListOf<com.altomedia.warkit.model.ShelfItem>()
                    for (j in 0 until arr.length()) {
                        val o = arr.getJSONObject(j)
                        shelf.add(com.altomedia.warkit.model.ShelfItem(
                            o.getString("p"), o.getInt("s"), o.getInt("c")))
                    }
                    state.shelves.add(shelf)
                }
            }

            // Missions
            state.missions = jsonToMissions(json.optJSONArray("missions"))
            state.dailyMissions = jsonToMissions(json.optJSONArray("dailyMissions"))

            // BAB 11-19
            state.employees.clear()
            json.optJSONArray("employees")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.getString(i)
                    EmployeeBank.all.firstOrNull { it.id == id }?.let { state.employees.add(it) }
                }
            }
            state.supplier = runCatching {
                Supplier.valueOf(json.optString("supplier", "DISTRIBUTOR_DESA"))
            }.getOrDefault(Supplier.DISTRIBUTOR_DESA)
            state.decorations.clear()
            json.optJSONArray("decorations")?.let { arr ->
                for (i in 0 until arr.length()) state.decorations.add(arr.getString(i))
            }
            state.timeOfDay = runCatching {
                TimeOfDay.valueOf(json.optString("timeOfDay", "PAGI"))
            }.getOrDefault(TimeOfDay.PAGI)
            state.weather = runCatching {
                Weather.valueOf(json.optString("weather", "CERAH"))
            }.getOrDefault(Weather.CERAH)
            state.totalVipServed = json.optInt("totalVipServed", 0)

            // BAB 21-30
            state.branches.clear()
            json.optJSONArray("branches")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val tmpl = com.altomedia.warkit.data.BranchBank.byId(o.getString("id"))
                    state.branches.add(tmpl.copy(
                        balance = o.optLong("balance"),
                        reputation = o.optInt("reputation"),
                        managerLevel = o.optInt("managerLevel"),
                        totalIncome = o.optLong("totalIncome")
                    ))
                }
            }
            state.vehicleLevel = json.optInt("vehicleLevel", 0)
            state.cashRegisterLevel = json.optInt("cashRegisterLevel", 1)
            state.promotion = runCatching {
                Promotion.valueOf(json.optString("promotion", "TIDAK_ADA"))
            }.getOrDefault(Promotion.TIDAK_ADA)
            state.promotionDaysLeft = json.optInt("promotionDaysLeft", 0)
            state.competition.competitorActive = json.optBoolean("competitorActive", false)
            state.competition.competitorStrength = json.optDouble("competitorStrength", 0.0).toFloat()
            state.security.clear()
            json.optJSONArray("security")?.let { arr ->
                for (i in 0 until arr.length()) state.security.add(arr.getString(i))
            }
            state.seasonalEvent = runCatching {
                SeasonalEvent.valueOf(json.optString("seasonalEvent", "NONE"))
            }.getOrDefault(SeasonalEvent.NONE)
            state.eventDaysLeft = json.optInt("eventDaysLeft", 0)

            savedAt
        } catch (e: Exception) {
            0L
        }
    }

    private fun missionsToJson(list: List<com.altomedia.warkit.model.Mission>): JSONArray {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id); put("title", m.title); put("description", m.description)
                put("target", m.target); put("progress", m.progress); put("daily", m.daily)
                put("completed", m.completed); put("claimed", m.claimed)
                put("rm", m.rewardMoney); put("rd", m.rewardDiamond)
                put("re", m.rewardExp); put("rb", m.rewardBooster); put("rc", m.rewardChest)
            })
        }
        return arr
    }

    private fun jsonToMissions(arr: JSONArray?): MutableList<com.altomedia.warkit.model.Mission> {
        if (arr == null) return com.altomedia.warkit.data.MissionBank.starter().toMutableList()
        val list = mutableListOf<com.altomedia.warkit.model.Mission>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(com.altomedia.warkit.model.Mission(
                id = o.getString("id"), title = o.getString("title"),
                description = o.getString("description"), target = o.getInt("target"),
                progress = o.getInt("progress"), daily = o.optBoolean("daily"),
                completed = o.optBoolean("completed"), claimed = o.optBoolean("claimed"),
                rewardMoney = o.optInt("rm"), rewardDiamond = o.optInt("rd"),
                rewardExp = o.optInt("re"), rewardBooster = o.optInt("rb"), rewardChest = o.optInt("rc")
            ))
        }
        return list
    }

    fun hasSave(): Boolean = prefs.getString("state", null) != null

    fun clear() { prefs.edit().clear().apply() }
}
