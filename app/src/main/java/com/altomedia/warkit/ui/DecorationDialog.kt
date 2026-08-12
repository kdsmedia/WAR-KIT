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
import com.altomedia.warkit.data.DecorationBank

/**
 * Dialog Dekorasi (BAB 15): beli dekorasi untuk reputasi & peluang VIP.
 */
class DecorationDialog(
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
            text = "🎨 DEKORASI WARUNG"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(TextView(ctx).apply {
            text = "Dekorasi menambah reputasi, peluang VIP, & estetika warung.\n" +
                "Total bonus: +${state.decorationReputation()} reputasi, " +
                "+${"%.1f".format(state.decorationVipBoost() * 100)}% VIP"
            textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
        })

        for (d in DecorationBank.unlocked(state.level)) {
            val owned = state.decorations.contains(d.id)
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(if (owned) Color.parseColor("#C8E6C9") else Color.parseColor("#FFF3E0"))
                    cornerRadius = 16f
                }
            }
            card.addView(TextView(ctx).apply {
                text = "${d.emoji} ${d.name}"; textSize = 14f
                setTextColor(Color.parseColor("#3E2C1C"))
            })
            card.addView(TextView(ctx).apply {
                text = "+${d.reputationBoost} reputasi • +${(d.vipBoost * 100).toInt()}% VIP • " +
                    "+${d.aestheticBoost} estetika"
                textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 8)
            })
            if (owned) {
                card.addView(TextView(ctx).apply {
                    text = "✅ Dimiliki"; textSize = 12f
                    setTextColor(Color.parseColor("#43A047"))
                })
            } else {
                card.addView(Button(ctx).apply {
                    text = "Beli -> Rp${d.cost}"
                    setOnClickListener {
                        if (state.buyDecoration(d.id)) { onChange(); refresh() }
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
