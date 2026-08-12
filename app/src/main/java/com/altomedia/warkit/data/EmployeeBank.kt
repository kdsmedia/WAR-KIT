package com.altomedia.warkit.data

import com.altomedia.warkit.model.Employee

/**
 * Daftar pegawai yang bisa direkrut (BAB 11). Kasir Pemula tersedia pertama.
 */
object EmployeeBank {

    val all: List<Employee> = listOf(
        Employee("kasir_pemula", "Kasir Pemula", "🧑‍💼", Employee.Role.CASHIER,
            workSpeed = 1.2f, friendliness = 1, efficiency = 0,
            dailyWage = 25_000, hireCost = 500_000, unlockLevel = 5),
        Employee("kasir_mahir", "Kasir Mahir", "👩‍💼", Employee.Role.CASHIER,
            workSpeed = 1.6f, friendliness = 2, efficiency = 5,
            dailyWage = 60_000, hireCost = 2_000_000, unlockLevel = 8),
        Employee("stocker", "Stocker", "🧑‍🔧", Employee.Role.STOCKER,
            workSpeed = 1.4f, friendliness = 0, efficiency = 8,
            dailyWage = 45_000, hireCost = 1_500_000, unlockLevel = 9),
        Employee("cleaner", "Cleaning Service", "🧹", Employee.Role.CLEANER,
            workSpeed = 1.0f, friendliness = 3, efficiency = 0,
            dailyWage = 30_000, hireCost = 1_000_000, unlockLevel = 10),
        Employee("manajer", "Manajer Warung", "🧑‍💼", Employee.Role.MANAGER,
            workSpeed = 1.3f, friendliness = 2, efficiency = 15,
            dailyWage = 120_000, hireCost = 8_000_000, unlockLevel = 12),
    )

    fun unlocked(level: Int): List<Employee> = all.filter { it.unlockLevel <= level }
    fun byId(id: String): Employee = all.first { it.id == id }
}
