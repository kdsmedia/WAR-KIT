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
import com.altomedia.warkit.data.BranchBank

/**
 * Dialog Cabang (BAB 21 & 22): buka cabang baru + upgrade manajer (Pak Hendra).
 */
class BranchDialog(
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
            text = "🗺️ PETA CABANG INDONESIA"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(TextView(ctx).apply {
            text = "Buka cabang untuk memperluas bisnis. Tiap cabang punya saldo, " +
                "gudang, pegawai & reputasi sendiri. Keuntungan otomatis masuk ke saldo utama.\n" +
                "Cabang aktif: ${state.branches.size} • Pendapatan cabang: Rp${state.branchIncomePerSecond()}/detik"
            textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
        })

        // Cabang aktif + manajer (BAB 22)
        if (state.branches.isNotEmpty()) {
            root.addView(TextView(ctx).apply {
                text = "CABANG AKTIF"; textSize = 14f
                setTextColor(Color.parseColor("#43A047")); setPadding(0, 8, 0, 8)
            })
        }
        for (b in state.branches) {
            val mgrTier = when {
                b.managerLevel == 0 -> "Tanpa Manajer"
                b.managerLevel < 5 -> "Pemula"
                b.managerLevel < 10 -> "Profesional"
                else -> "Ahli Bisnis"
            }
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.parseColor("#C8E6C9")); cornerRadius = 16f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${b.emoji} ${b.name}"; textSize = 14f
                setTextColor(Color.parseColor("#1B5E20"))
            })
            card.addView(TextView(ctx).apply {
                text = "Reputasi: ${b.reputation} • Pendapatan: Rp${b.totalIncome} • " +
                    "Manajer: ${b.managerLevel}/10 ($mgrTier)"
                textSize = 11f; setTextColor(Color.parseColor("#33691E")); setPadding(0, 4, 0, 8)
            })
            card.addView(Button(ctx).apply {
                text = "Upgrade Manajer (Pak Hendra) -> Rp${500_000 * (b.managerLevel + 1)}"
                setOnClickListener {
                    if (state.upgradeBranchManager(b)) { onChange(); refresh() }
                }
            })
            root.addView(card)
        }

        // Cabang tersedia
        root.addView(TextView(ctx).apply {
            text = "CABANG TERSEDIA UNTUK DIBUKA"; textSize = 14f
            setTextColor(Color.parseColor("#E76F51")); setPadding(0, 24, 0, 8)
        })
        for (b in BranchBank.unlocked(state.level)) {
            if (state.branches.any { it.id == b.id }) continue
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.parseColor("#FFF3E0")); cornerRadius = 16f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${b.emoji} ${b.name}"; textSize = 14f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            card.addView(Button(ctx).apply {
                text = "Buka Cabang -> Rp${b.openCost}"
                setOnClickListener {
                    if (state.openBranch(b)) { onChange(); refresh() }
                }
            })
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
