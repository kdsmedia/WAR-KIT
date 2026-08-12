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
import com.altomedia.warkit.model.MembershipTier
import com.altomedia.warkit.model.PaymentMethod

/**
 * Dialog Modernisasi (BAB 43 member, 44 pembayaran digital, 45 pusat distribusi nasional).
 */
class ModernDialog(
    context: Context,
    private val state: GameState,
    private val onChange: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiTheme.applyPopup(this)
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
            setBackgroundColor(Color.TRANSPARENT)
        }
        root.addView(TextView(ctx).apply {
            text = "💳 MODERNISASI LAYANAN"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        // BAB 43: Program member
        root.addView(section("🏷️ PROGRAM MEMBER (BAB 43)"))
        root.addView(info("Aktif: ${state.membershipTier.emoji} ${state.membershipTier.displayName} • " +
            "Pelanggan kembali x${state.membershipTier.returnMult} • " +
            "Nilai transaksi x${state.membershipTier.billMult}"))
        for (t in MembershipTier.entries) {
            if (t == MembershipTier.NONE) continue
            root.addView(Button(ctx).apply {
                text = "${t.emoji} ${t.displayName} -> Rp${t.cost} (level ${t.unlockLevel}+)"
                setOnClickListener { if (state.launchMembership(t)) { onChange(); refresh() } }
            })
        }

        // BAB 44: Pembayaran digital
        root.addView(section("📱 PEMBAYARAN DIGITAL (BAB 44)"))
        val pm = state.paymentMethod()
        root.addView(info("Aktif: ${pm.emoji} ${pm.displayName} • Kecepatan x${pm.speedMult} • " +
            "Kurangi kesalahan ${(pm.errorReduction * 100).toInt()}%"))
        val nextPm = PaymentMethod.entries.getOrNull(state.paymentLevel + 1)
        if (nextPm != null) {
            root.addView(Button(ctx).apply {
                text = "Upgrade ke ${nextPm.displayName} -> Rp${nextPm.upgradeCost}"
                setOnClickListener { if (state.upgradePayment()) { onChange(); refresh() } }
            })
        } else {
            root.addView(info("Pembayaran sudah maksimal!"))
        }

        // BAB 45: Pusat distribusi nasional
        root.addView(section("🏭 PUSAT DISTRIBUSI NASIONAL (BAB 45)"))
        if (state.nationalDist.active) {
            root.addView(info("✅ Aktif! Level ${state.nationalDist.level} • " +
                "Kapasitas ${state.nationalDist.stockCapacity()} • " +
                "Kecepatan x${state.nationalDist.deliverySpeedMult()} • " +
                "Hemat logistik ${(state.nationalDist.logisticsCostReduction() * 100).toInt()}%"))
            root.addView(Button(ctx).apply {
                text = "Upgrade Pusat Distribusi -> Rp${state.nationalDist.upgradeCost()}"
                setOnClickListener { if (state.upgradeNationalDist()) { onChange(); refresh() } }
            })
        } else {
            root.addView(info("Bangun pusat distribusi nasional untuk mengatur stok seluruh " +
                "cabang, kirim otomatis, kurangi biaya logistik. (level 23+, Rp50.000.000)"))
            root.addView(Button(ctx).apply {
                text = "Bangun Pusat Distribusi -> Rp50.000.000"
                isEnabled = state.level >= 23
                setOnClickListener { if (state.activateNationalDist()) { onChange(); refresh() } }
            })
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
