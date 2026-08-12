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

/**
 * Dialog Achievement & Endless Empire (BAB 50).
 */
class AchievementDialog(
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
            text = "🏆 PRESTASI & ENDLESS EMPIRE"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })

        // Status endgame
        root.addView(TextView(ctx).apply {
            val status = if (state.endlessEmpireUnlocked)
                "✅ Endless Empire aktif! Bangun kerajaan bisnis tanpa batas."
            else "Mode Endless Empire terbuka setelah meraih gelar Raja Warung."
            text = status; textSize = 12f
            setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
        })

        // Gelar Raja Warung
        if (state.rajaWarungTitle) {
            root.addView(TextView(ctx).apply {
                text = "👑 GELAR: Raja Warung Indonesia\n" +
                    "Skin bangunan emas, patung maskot, bingkai profil & efek konfeti terbuka!"
                textSize = 13f; setTextColor(Color.parseColor("#F9A825"))
                setPadding(16, 8, 16, 16)
            })
        }

        // Prestise
        root.addView(TextView(ctx).apply {
            text = "Prestise: ${state.prestige}"; textSize = 12f
            setTextColor(Color.parseColor("#1565C0")); setPadding(0, 8, 0, 16)
        })

        // Daftar achievement
        root.addView(TextView(ctx).apply {
            text = "DAFTAR PRESTASI"; textSize = 14f
            setTextColor(Color.parseColor("#1565C0")); setPadding(0, 8, 0, 8)
        })
        for (a in state.achievements) {
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; setPadding(12, 12, 12, 12)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (a.completed) Color.parseColor("#C8E6C9") else Color.parseColor("#FFF3E0"))
                    cornerRadius = 12f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${a.emoji} ${a.name}"; textSize = 13f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            card.addView(TextView(ctx).apply {
                text = "${a.description}\nProgress: ${a.progress}/${a.target}" +
                    if (a.completed && !a.claimed) " (Klaim hadiah!)" else if (a.claimed) " ✅ Diklaim" else ""
                textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 4)
            })
            if (a.completed && !a.claimed) {
                card.addView(Button(ctx).apply {
                    text = "Klaim Hadiah (+Rp1.000.000)"
                    setOnClickListener {
                        a.claimed = true
                        state.money += 1_000_000
                        onChange(); refresh()
                    }
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
