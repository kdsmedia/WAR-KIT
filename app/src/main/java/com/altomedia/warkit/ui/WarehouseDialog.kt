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
import com.altomedia.warkit.model.Mission

/**
 * Dialog Gudang (BAB 4): beli stok produk dari supplier & lihat stok.
 * Juga menampilkan stok yang ada di rak.
 */
class WarehouseDialog(
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
        root.addView(title("🏪 GUDANG & SUPPLIER"))
        root.addView(info("Kapasitas Gudang: ${state.warehouse.values.sum()}/${state.warehouseCapacity} (Lv.${state.warehouseLevel})"))
        root.addView(info("Saldo: Rp${state.money}"))

        // Upgrade gudang
        val upgBtn = Button(ctx).apply {
            text = "Upgrade Gudang -> Rp${state.upgradeWarehouseCost()}"
            setOnClickListener {
                if (state.upgradeWarehouse()) onChange()
                refresh()
            }
        }
        root.addView(upgBtn)

        // Daftar produk untuk dibeli stoknya
        root.addView(subTitle("BELI STOK PRODUK"))
        for (p in ProductCatalog.unlocked(state.level)) {
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(8, 8, 8, 8)
            }
            row.addView(TextView(ctx).apply {
                text = "${p.emoji} ${p.name}\nBeli Rp${p.buyPrice} | Jual Rp${p.sellPrice} (+Rp${p.profit})"
                textSize = 12f
                setTextColor(Color.parseColor("#3E2C1C"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            val inWh = state.warehouse[p.id] ?: 0
            val onShelf = state.shelves.sumOf { sh -> sh.filter { it.productId == p.id }.sumOf { it.stock } }
            row.addView(TextView(ctx).apply {
                text = "Gudang:$inWh Rak:$onShelf"
                textSize = 11f
                setTextColor(Color.parseColor("#5D4037"))
                setPadding(8, 0, 8, 0)
            })
            val buy10 = Button(ctx).apply {
                text = "+10\nRp${p.buyPrice * 10}"
                textSize = 10f
                setOnClickListener {
                    if (state.buyStock(p.id, 10)) onChange()
                    refresh()
                }
            }
            val buy50 = Button(ctx).apply {
                text = "+50\nRp${p.buyPrice * 50}"
                textSize = 10f
                setOnClickListener {
                    if (state.buyStock(p.id, 50)) onChange()
                    refresh()
                }
            }
            row.addView(buy10)
            row.addView(buy50)
            root.addView(row)
        }

        // Restock rak
        root.addView(subTitle("ISI RAK DARI GUDANG"))
        state.shelves.forEachIndexed { shelfIdx, shelf ->
            shelf.forEachIndexed { slotIdx, item ->
                val p = ProductCatalog.byId(item.productId)
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(8, 4, 8, 4)
                }
                row.addView(TextView(ctx).apply {
                    text = "Rak ${shelfIdx + 1} • ${p.emoji} ${p.name}: ${item.stock}/${item.capacity} (Gudang:${state.warehouse[item.productId] ?: 0})"
                    textSize = 12f
                    setTextColor(Color.parseColor("#3E2C1C"))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                row.addView(Button(ctx).apply {
                    text = "Isi"
                    setOnClickListener {
                        state.restockShelf(shelfIdx, slotIdx)
                        onChange(); refresh()
                    }
                })
                root.addView(row)
            }
        }

        val close = Button(ctx).apply { text = "Tutup"; setOnClickListener { dismiss() } }
        root.addView(close)
        scroll.addView(root)
        return scroll
    }

    private fun refresh() { setContentView(buildView()) }

    private fun title(t: String) = TextView(context).apply {
        text = t; textSize = 20f; setTextColor(Color.parseColor("#B8523A"))
        gravity = Gravity.CENTER; setPadding(0, 8, 0, 16)
    }
    private fun subTitle(t: String) = TextView(context).apply {
        text = t; textSize = 15f; setTextColor(Color.parseColor("#E76F51"))
        setPadding(0, 16, 0, 8)
    }
    private fun info(t: String) = TextView(context).apply {
        text = t; textSize = 13f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 4)
    }
}
