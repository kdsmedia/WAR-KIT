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
import com.altomedia.warkit.model.SeasonalEvent

/**
 * Dialog Event Musiman (BAB 29 Ramadhan & BAB 30 Lebaran).
 * Pemain memulai event; saat event selesai, tampilkan laporan pencapaian.
 */
class EventDialog(
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
            text = "📅 EVENT MUSIMAN"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        // Status event aktif
        if (state.seasonalEvent != SeasonalEvent.NONE) {
            root.addView(TextView(ctx).apply {
                text = "${state.seasonalEvent.emoji} ${state.seasonalEvent.displayName} BERLANGSUNG\n" +
                    "Sisa ${state.eventDaysLeft} hari • Pelanggan x${state.seasonalEvent.customerMult}\n" +
                    "Belanja x${state.seasonalEvent.billMult} • VIP +${(state.seasonalEvent.vipChanceBonus * 100).toInt()}%"
                textSize = 13f; setTextColor(Color.parseColor("#43A047")); setPadding(0, 8, 0, 16)
            })
            root.addView(Button(ctx).apply {
                text = "Akhiri Event & Lihat Laporan"
                setOnClickListener {
                    showReport(root)
                }
            })
        } else {
            root.addView(TextView(ctx).apply {
                text = "Tidak ada event aktif. Pilih event untuk dimulai:"
                textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
            })
            // BAB 29: Ramadhan
            root.addView(eventCard(SeasonalEvent.RAMADHAN,
                "Bulan Ramadhan tiba! Suasana desa ramai menjelang berbuka. " +
                    "Produk terlaris: Sirup, Kurma, Minyak, Gula, Tepung, Mi Instan, Minuman."))
            // BAB 30: Lebaran
            root.addView(eventCard(SeasonalEvent.LEBARAN,
                "Hari Raya Idulfitri! Hampir seluruh warga berbelanja. " +
                    "Pelanggan VIP lebih sering muncul. Jaga stok produk tertentu!"))
        }

        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 24, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }

    private fun eventCard(ev: SeasonalEvent, desc: String): LinearLayout {
        val ctx = context
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#FFF3E0")); cornerRadius = 16f
            }
        }
        card.addView(TextView(ctx).apply {
            text = "${ev.emoji} ${ev.displayName}"; textSize = 14f
            setTextColor(Color.parseColor("#3E2C1C"))
        })
        card.addView(TextView(ctx).apply {
            text = desc; textSize = 11f
            setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 8)
        })
        card.addView(Button(ctx).apply {
            text = "Mulai ${ev.displayName} (5 hari)"
            setOnClickListener {
                state.startEvent(ev, 5); onChange(); refresh()
            }
        })
        return card
    }

    /** BAB 30: laporan pencapaian event. */
    private fun showReport(root: LinearLayout) {
        root.removeAllViews()
        val ctx = context
        root.addView(TextView(ctx).apply {
            text = "📊 LAPORAN EVENT ${state.seasonalEvent.displayName}"; textSize = 18f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })
        root.addView(reportLine("Total pelanggan dilayani", "${state.totalCustomersServed}"))
        root.addView(reportLine("Total pendapatan sesi", "Rp${state.sessionIncome}"))
        root.addView(reportLine("Total produk terjual", "${state.totalProductsSold}"))
        root.addView(reportLine("Pelanggan VIP dilayani", "${state.totalVipServed}"))
        root.addView(reportLine("Reputasi warung", "${state.reputation} (${state.reputationTier().displayName})"))
        root.addView(TextView(ctx).apply {
            text = "\n«Kesuksesanmu menarik perhatian investor lokal. Mereka menawarkan kerja sama " +
                "untuk membuka usaha yang lebih besar.»"
            textSize = 12f; setTextColor(Color.parseColor("#6D4C41")); setPadding(0, 16, 0, 16)
        })
        root.addView(Button(ctx).apply {
            text = "Selesai"
            setOnClickListener {
                state.endEvent(); onChange(); dismiss()
            }
        })
    }

    private fun reportLine(label: String, value: String) = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL; setPadding(8, 4, 8, 4)
        addView(TextView(context).apply {
            text = label; textSize = 12f
            setTextColor(Color.parseColor("#5D4037"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(TextView(context).apply {
            text = value; textSize = 12f
            setTextColor(Color.parseColor("#3E2C1C")); setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
    }

    private fun refresh() { setContentView(buildView()) }
}
