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
import com.altomedia.warkit.data.ProductCatalog

/**
 * Dialog Produk (BAB 8): lihat produk yang sudah/belum terbuka per level.
 */
class ProductDialog(
    context: Context,
    private val state: GameState
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
            text = "🛒 DAFTAR PRODUK"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })
        root.addView(TextView(ctx).apply {
            text = "Produk terbuka otomatis saat naik level (BAB 8).\nLevel kamu: ${state.level}"
            textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 0, 0, 16)
        })

        for (p in ProductCatalog.all) {
            val unlocked = state.level >= p.unlockLevel
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 12, 12, 12)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (unlocked) Color.parseColor("#FFF3E0") else Color.parseColor("#EEEEEE"))
                    cornerRadius = 16f
                }
            }
            row.addView(TextView(ctx).apply {
                text = if (unlocked) p.emoji else "🔒"
                textSize = 24f; setPadding(0, 0, 12, 0)
            })
            val info = TextView(ctx).apply {
                text = if (unlocked)
                    "${p.name}\nBeli Rp${p.buyPrice} • Jual Rp${p.sellPrice} (+Rp${p.profit})"
                else
                    "${p.name}\n🔒 Terbuka di Level ${p.unlockLevel}"
                textSize = 12f
                setTextColor(if (unlocked) Color.parseColor("#3E2C1C") else Color.parseColor("#9E9E9E"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(info)
            root.addView(row)
            (root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply { bottomMargin = 8 }
        }

        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 24, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }
}
