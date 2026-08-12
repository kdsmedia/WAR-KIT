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
import com.altomedia.warkit.data.ProvinceBank

/**
 * Dialog Provinsi (BAB 41 ekspansi antar provinsi & BAB 42 produk khas daerah).
 */
class ProvinceDialog(
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
            text = "🇮🇩 EKSPANSI ANTAR PROVINSI"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(TextView(ctx).apply {
            text = "Provinsi aktif: ${state.provinces.size}/10 • " +
                "Multiplier pelanggan: x${state.provinceCustomerMult()}\n" +
                "Tiap provinsi punya pelanggan, produk favorit, biaya & persaingan berbeda."
            textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
        })

        for (p in ProvinceBank.provinces) {
            val owned = state.provinces.contains(p.id)
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (owned) Color.parseColor("#C8E6C9") else Color.parseColor("#FFF3E0"))
                    cornerRadius = 16f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${p.emoji} ${p.name}"; textSize = 14f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            // BAB 42: produk khas daerah
            val regional = ProvinceBank.regionalByProvince(p.id)
            val prodNames = regional.joinToString(", ") { it.name }
            card.addView(TextView(ctx).apply {
                text = "Produk khas: $prodNames\n" +
                    "Pelanggan x${p.customerMult} • Biaya ops x${p.opCostMult} • " +
                    "Persaingan ${(p.competitionStrength * 100).toInt()}%"
                textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 8)
            })
            if (!owned) {
                val canUnlock = state.level >= p.unlockLevel
                card.addView(Button(ctx).apply {
                    text = if (canUnlock) "Buka Provinsi -> Rp${p.unlockCost}"
                    else "Terkunci (level ${p.unlockLevel})"
                    isEnabled = canUnlock
                    setOnClickListener { if (state.openProvince(p.id)) { onChange(); refresh() } }
                })
            } else {
                card.addView(TextView(ctx).apply {
                    text = "✅ Provinsi aktif"; textSize = 12f
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

    private fun refresh() { setContentView(buildView()) }
}
