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
import com.altomedia.warkit.data.ShelfConfig

/**
 * Dialog Upgrade (BAB 6): upgrade rak & gudang.
 */
class UpgradeDialog(
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
            text = "⬆️ UPGRADE WARUNG"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        // Rak
        val cur = ShelfConfig.get(state.shelfLevel)
        root.addView(TextView(ctx).apply {
            text = "📦 Rak Level ${state.shelfLevel}\nKapasitas: ${cur.capacity}/slot • Estetika: ${cur.aesthetic}"
            textSize = 14f; setTextColor(Color.parseColor("#3E2C1C")); setPadding(0, 8, 0, 8)
        })
        val next = ShelfConfig.next(state.shelfLevel)
        if (next != null) {
            root.addView(TextView(ctx).apply {
                text = "Next Lv.${next.level}: Kapasitas ${next.capacity}/slot, Estetika ${next.aesthetic}"
                textSize = 12f; setTextColor(Color.parseColor("#5D4037"))
            })
            val cost = state.upgradeShelfCost()
            val btn = Button(ctx).apply {
                text = "Upgrade Rak -> Rp$cost"
                setOnClickListener {
                    if (state.upgradeShelf()) onChange()
                    refresh()
                }
            }
            root.addView(btn)
        } else {
            root.addView(TextView(ctx).apply {
                text = "Rak sudah level maksimal!"; textSize = 12f
                setTextColor(Color.parseColor("#43A047"))
            })
        }

        // Gudang
        root.addView(TextView(ctx).apply {
            text = "🏪 Gudang Level ${state.warehouseLevel}\nKapasitas: ${state.warehouseCapacity}"
            textSize = 14f; setTextColor(Color.parseColor("#3E2C1C"))
            setPadding(0, 24, 0, 8)
        })
        val wBtn = Button(ctx).apply {
            text = "Upgrade Gudang -> Rp${state.upgradeWarehouseCost()}"
            setOnClickListener {
                if (state.upgradeWarehouse()) onChange()
                refresh()
            }
        }
        root.addView(wBtn)

        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 24, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }

    private fun refresh() { setContentView(buildView()) }
}
