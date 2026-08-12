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
import com.altomedia.warkit.data.SecurityBank
import com.altomedia.warkit.model.CashRegister
import com.altomedia.warkit.model.Promotion
import com.altomedia.warkit.model.Vehicle

/**
 * Dialog Operasional (BAB 23 kendaraan, 25 mesin kasir, 26 promosi, 28 keamanan).
 */
class OperationsDialog(
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
            text = "⚙️ OPERASIONAL WARUNG"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        // BAB 23: Kendaraan
        root.addView(section("🚚 PENGIRIMAN (BAB 23)"))
        val v = state.vehicle()
        root.addView(info("Aktif: ${v.emoji} ${v.displayName} • Kapasitas ${v.capacity} • Kecepatan x${v.speed}"))
        val nextV = Vehicle.entries.getOrNull(state.vehicleLevel + 1)
        if (nextV != null) {
            root.addView(Button(ctx).apply {
                text = "Upgrade ke ${nextV.displayName} -> Rp${nextV.unlockCost}"
                setOnClickListener { if (state.upgradeVehicle()) { onChange(); refresh() } }
            })
        } else {
            root.addView(info("Kendaraan sudah maksimal!"))
        }

        // BAB 25: Mesin kasir
        root.addView(section("🧮 MESIN KASIR (BAB 25)"))
        val cr = state.cashRegister()
        root.addView(info("Aktif: ${cr.emoji} ${cr.displayName} • Kecepatan scan x${cr.scanSpeed}"))
        val nextCr = CashRegister.entries.getOrNull(state.cashRegisterLevel)
        if (nextCr != null) {
            root.addView(Button(ctx).apply {
                text = "Upgrade ke ${nextCr.displayName} -> Rp${nextCr.upgradeCost}"
                setOnClickListener { if (state.upgradeCashRegister()) { onChange(); refresh() } }
            })
        } else {
            root.addView(info("Mesin kasir sudah maksimal!"))
        }

        // BAB 26: Promosi
        root.addView(section("🏷️ PROMOSI (BAB 26)"))
        root.addView(info("Aktif: ${state.promotion.emoji} ${state.promotion.displayName}" +
            if (state.promotionDaysLeft > 0) " (${state.promotionDaysLeft} hari lagi)" else ""))
        for (p in Promotion.entries) {
            if (p == Promotion.TIDAK_ADA) continue
            root.addView(Button(ctx).apply {
                text = "${p.emoji} ${p.displayName} -> Rp${p.cost} (${p.durationDays} hari)"
                setOnClickListener { if (state.startPromotion(p)) { onChange(); refresh() } }
            })
        }
        root.addView(Button(ctx).apply {
            text = "➖ Matikan Promosi"
            setOnClickListener { if (state.startPromotion(Promotion.TIDAK_ADA)) { onChange(); refresh() } }
        })

        // BAB 28: Keamanan
        root.addView(section("🛡️ KEAMANAN (BAB 28)"))
        root.addView(info("Pengurangan pencurian: ${(state.theftReduction() * 100).toInt()}%"))
        for (s in SecurityBank.unlocked(state.level)) {
            val owned = state.security.contains(s.id)
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (owned) Color.parseColor("#C8E6C9") else Color.parseColor("#FFF3E0"))
                    cornerRadius = 16f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${s.emoji} ${s.name}"; textSize = 14f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            card.addView(TextView(ctx).apply {
                text = "-${(s.theftReduction * 100).toInt()}% pencurian • +${s.reputationBoost} reputasi"
                textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 8)
            })
            if (!owned) {
                card.addView(Button(ctx).apply {
                    text = "Beli -> Rp${s.cost}"
                    setOnClickListener { if (state.buySecurity(s.id)) { onChange(); refresh() } }
                })
            } else {
                card.addView(TextView(ctx).apply {
                    text = "✅ Terpasang"; textSize = 12f
                    setTextColor(Color.parseColor("#43A047"))
                })
            }
            root.addView(card)
        }

        // BAB 27: Status persaingan
        root.addView(section("🏪 PERSAINGAN (BAB 27)"))
        if (state.competition.competitorActive) {
            root.addView(info("Minimarket kompetitor aktif! Daya tarik: " +
                "${(state.competition.competitorStrength * 100).toInt()}%"))
            root.addView(info("Nilai warungmu: ${(state.playerShopScore() * 100).toInt()}% • " +
                "Retensi pelanggan: ${(state.competition.retentionRate() * 100).toInt()}%"))
        } else {
            root.addView(info("Belum ada kompetitor. Tingkatkan warungmu untuk siap bersaing."))
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
