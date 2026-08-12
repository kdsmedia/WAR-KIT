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
import com.altomedia.warkit.model.Supplier

/**
 * Dialog Supplier (BAB 13): pilih pemasok untuk harga beli lebih murah.
 */
class SupplierDialog(
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
            text = "🚚 SUPPLIER"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(TextView(ctx).apply {
            text = "Supplier lebih tinggi memberi harga beli lebih murah " +
                "dan restock lebih cepat. Buka produk langka di level lebih tinggi."
            textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
        })
        root.addView(TextView(ctx).apply {
            text = "Supplier aktif: ${state.supplier.emoji} ${state.supplier.displayName} " +
                "(diskon ${(state.supplier.discount * 100).toInt()}%)"
            textSize = 13f; setTextColor(Color.parseColor("#43A047")); setPadding(0, 0, 0, 16)
        })

        for (s in Supplier.entries) {
            val unlocked = state.level >= s.unlockLevel
            val active = state.supplier == s
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (unlocked) Color.parseColor("#FFF3E0") else Color.parseColor("#EEEEEE"))
                    cornerRadius = 16f
                    if (active) setStroke(4, Color.parseColor("#43A047"))
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${s.emoji} ${s.displayName}"; textSize = 14f
                setTextColor(if (unlocked) Color.parseColor("#3E2C1C") else Color.parseColor("#9E9E9E"))
            })
            card.addView(TextView(ctx).apply {
                text = "Diskon harga beli: ${(s.discount * 100).toInt()}% • " +
                    "Kecepatan restock x${s.restockSpeed}"
                textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 4)
            })
            when {
                active -> card.addView(TextView(ctx).apply {
                    text = "✅ Aktif"; textSize = 12f; setTextColor(Color.parseColor("#43A047"))
                })
                unlocked && state.supplier.ordinal < s.ordinal -> card.addView(Button(ctx).apply {
                    text = if (s.unlockCost == 0) "Pilih" else "Buka -> Rp${s.unlockCost}"
                    setOnClickListener {
                        if (state.unlockSupplier(s)) { onChange(); refresh() }
                    }
                })
                unlocked -> card.addView(TextView(ctx).apply {
                    text = "Sudah dilewati"; textSize = 12f
                    setTextColor(Color.parseColor("#9E9E9E"))
                })
                else -> card.addView(TextView(ctx).apply {
                    text = "🔒 Terbuka di Level ${s.unlockLevel}"; textSize = 12f
                    setTextColor(Color.parseColor("#9E9E9E"))
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
