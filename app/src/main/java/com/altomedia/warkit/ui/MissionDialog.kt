package com.altomedia.warkit.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.altomedia.warkit.core.GameState
import com.altomedia.warkit.model.Mission

/**
 * Dialog Misi (BAB 9): tampilkan misi biasa & harian, progress, dan klaim hadiah.
 */
class MissionDialog(
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
            text = "🎯 MISI"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(section("MISI UTAMA", state.missions))
        root.addView(section("MISI HARIAN (reset tiap hari)", state.dailyMissions))
        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 24, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }

    private fun section(title: String, missions: List<Mission>): LinearLayout {
        val ctx = context
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }
        container.addView(TextView(ctx).apply {
            text = title; textSize = 15f
            setTextColor(Color.parseColor("#E76F51")); setPadding(0, 8, 0, 8)
        })
        for (m in missions) {
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.parseColor("#FFF3E0"))
                    cornerRadius = 16f
                    if (m.isReady()) setStroke(4, Color.parseColor("#43A047"))
                }
            }
            card.addView(TextView(ctx).apply {
                text = m.title; textSize = 14f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            card.addView(TextView(ctx).apply {
                text = m.description; textSize = 11f
                setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 4)
            })
            // Progress
            val progText = TextView(ctx).apply {
                text = "Progress: ${m.progress}/${m.target}"
                textSize = 12f
                setTextColor(Color.parseColor("#5D4037"))
            }
            card.addView(progText)
            // Reward
            val rewards = buildString {
                if (m.rewardMoney > 0) append("💵Rp${m.rewardMoney} ")
                if (m.rewardDiamond > 0) append("💎${m.rewardDiamond} ")
                if (m.rewardExp > 0) append("EXP${m.rewardExp} ")
                if (m.rewardBooster > 0) append("⚡${m.rewardBooster} ")
                if (m.rewardChest > 0) append("🎁${m.rewardChest}")
            }
            card.addView(TextView(ctx).apply {
                text = "Hadiah: $rewards"; textSize = 11f
                setTextColor(Color.parseColor("#B8523A")); setPadding(0, 4, 0, 8)
            })
            if (m.isReady()) {
                card.addView(Button(ctx).apply {
                    text = "KLAIM HADIAH"
                    setOnClickListener {
                        state.claimMission(m)
                        onChange(); refresh()
                    }
                })
            } else if (m.completed && m.claimed) {
                card.addView(TextView(ctx).apply {
                    text = "✅ Sudah diklaim"; textSize = 12f
                    setTextColor(Color.parseColor("#43A047"))
                })
            }
            container.addView(card)
        }
        return container
    }

    private fun refresh() { setContentView(buildView()) }
}
