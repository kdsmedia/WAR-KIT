package com.altomedia.warkit.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.altomedia.warkit.core.GameState
import com.altomedia.warkit.data.FacilityBank
import com.altomedia.warkit.model.TrainingType

/**
 * Dialog Bisnis (BAB 31 investor, 32 grosir, 33/34 logistik, 37 pelatihan, 39 bangunan).
 */
class BusinessDialog(
    context: Context,
    private val state: GameState,
    private val onChange: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildView())
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun buildView(): ScrollView {
        val ctx = context
        val scroll = ScrollView(ctx)
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#FDF6E3"))
        }
        root.addView(TextView(ctx).apply {
            text = "💼 PENGEMBANGAN BISNIS"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        // BAB 31: Investor
        root.addView(section("🤝 INVESTOR (BAB 31)"))
        if (state.investorDealAccepted) {
            root.addView(info("✅ Deal investor diterima! Bonus pendapatan x${state.investorIncomeMult}, " +
                "diskon bangun cabang ${(state.investorConstructionDiscount * 100).toInt()}%"))
        } else if (state.investorActive) {
            root.addView(info("Hari ${state.investorDaysElapsed}/30 • Target tercapai ${state.investorDaysMet} hari"))
            root.addView(info("Target: Pendapatan Rp${state.investorTargetDailyIncome}/hari, " +
                "${state.investorTargetCustomers} pelanggan, reputasi ${state.investorTargetReputation}"))
            root.addView(info("Buktikan warungmu stabil 30 hari (>= 24 hari target tercapai = deal diterima)"))
        } else {
            root.addView(info("Investor menawarkan kerja sama: tambahan modal, diskon pembangunan, " +
                "bonus reputasi & pendapatan. Buktikan warungmu stabil selama 30 hari."))
            root.addView(Button(ctx).apply {
                text = "Mulai Tantangan Investor (30 hari)"
                setOnClickListener { if (state.startInvestor()) { onChange(); refresh() } }
            })
        }

        // BAB 32: Grosir
        root.addView(section("📦 TOKO GROSIR (BAB 32)"))
        if (state.grosirUnlocked) {
            root.addView(info("✅ Grosir aktif! Pendapatan harian: Rp${state.grosirDailyIncome()}"))
        } else if (state.level >= 12) {
            root.addView(Button(ctx).apply {
                text = "Buka Toko Grosir (gratis, level 12+)"
                setOnClickListener { if (state.unlockGrosir()) { onChange(); refresh() } }
            })
        } else {
            root.addView(info("Pembeli grosir datang lebih jarang tapi belanja besar. " +
                "Buka di level 12."))
        }

        // BAB 33/34: Logistik & gudang distribusi
        root.addView(section("🚛 LOGISTIK & DISTRIBUSI (BAB 33/34)"))
        root.addView(info("Gudang Distribusi Level ${state.distWarehouseLevel} • " +
            "Kapasitas ${state.distCapacity()} • Kecepatan x${state.distSpeed()}"))
        root.addView(Button(ctx).apply {
            text = "Upgrade Gudang Distribusi -> Rp${3_000_000L * (state.distWarehouseLevel + 1)}"
            setOnClickListener { if (state.upgradeDistWarehouse()) { onChange(); refresh() } }
        })

        // BAB 37: Pelatihan pegawai
        root.addView(section("🎓 PELATIHAN PEGAWAI (BAB 37)"))
        if (state.employees.isEmpty()) {
            root.addView(info("Rekrut pegawai dulu sebelum melatih."))
        } else {
            root.addView(info("Bonus pelatihan: kecepatan +${state.trainingSpeedBoost()}, " +
                "kepuasan +${state.trainingSatisfactionBonus()}, " +
                "efisiensi +${state.trainingEfficiencyBonus()}, " +
                "pendapatan x${state.trainingIncomeMult()}"))
            for (emp in state.employees) {
                root.addView(TextView(ctx).apply {
                    text = "${emp.emoji} ${emp.name} (${emp.role.name.lowercase()})"
                    textSize = 12f; setTextColor(Color.parseColor("#3E2C1C")); setPadding(8, 8, 8, 4)
                })
                for (t in TrainingType.entries) {
                    root.addView(Button(ctx).apply {
                        text = "${t.emoji} ${t.displayName} -> Rp${t.cost}"
                        setOnClickListener { if (state.trainEmployee(emp.id, t)) { onChange(); refresh() } }
                    })
                }
            }
        }

        // BAB 39: Bangunan & fasilitas
        root.addView(section("🏢 BANGUNAN & FASILITAS (BAB 39)"))
        val b = state.building()
        root.addView(info("Bangunan: ${b.emoji} ${b.displayName} • Kenyamanan x${b.comfort} • " +
            "Transaksi x${b.transactionMult}"))
        val nextB = com.altomedia.warkit.model.BuildingLevel.entries.getOrNull(state.buildingLevel)
        if (nextB != null) {
            root.addView(Button(ctx).apply {
                text = "Upgrade ke ${nextB.displayName} -> Rp${nextB.upgradeCost}"
                setOnClickListener { if (state.upgradeBuilding()) { onChange(); refresh() } }
            })
        } else {
            root.addView(info("Bangunan sudah maksimal!"))
        }
        root.addView(info("Fasilitas terpasang: ${state.facilities.size} • " +
            "Bonus kenyamanan +${(state.facilityComfort() * 100).toInt()}%"))
        for (f in FacilityBank.unlocked(state.level)) {
            val owned = state.facilities.contains(f.id)
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (owned) Color.parseColor("#C8E6C9") else Color.parseColor("#FFF3E0"))
                    cornerRadius = 16f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${f.emoji} ${f.name}"; textSize = 14f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            card.addView(TextView(ctx).apply {
                text = "+${(f.comfortBoost * 100).toInt()}% kenyamanan • +${f.reputationBoost} reputasi"
                textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 8)
            })
            if (!owned) {
                card.addView(Button(ctx).apply {
                    text = "Beli -> Rp${f.cost}"
                    setOnClickListener { if (state.buyFacility(f.id)) { onChange(); refresh() } }
                })
            } else {
                card.addView(TextView(ctx).apply {
                    text = "✅ Terpasang"; textSize = 12f
                    setTextColor(Color.parseColor("#43A047"))
                })
            }
            root.addView(card)
        }

        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 24, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }

    private fun section(title: String) = TextView(context).apply {
        text = title; textSize = 14f
        setTextColor(Color.parseColor("#1565C0")); setPadding(0, 16, 0, 8)
    }
    private fun info(t: String) = TextView(context).apply {
        text = t; textSize = 12f
        setTextColor(Color.parseColor("#5D4037")); setPadding(8, 4, 8, 8)
    }
    private fun refresh() { setContentView(buildView()) }
}
