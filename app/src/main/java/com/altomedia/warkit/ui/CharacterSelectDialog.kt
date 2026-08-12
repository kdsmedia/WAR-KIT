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
import com.altomedia.warkit.model.SellerCharacter

/**
 * Dialog pemilihan karakter penjual (README). Tampil di awal permainan
 * jika belum ada save. Wajib dipilih sebelum bermain.
 */
class CharacterSelectDialog(
    context: Context,
    private val state: GameState,
    private val onPick: (SellerCharacter) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiTheme.applyPopup(this)
        setContentView(buildView())
        setCancelable(false)
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
        val title = TextView(ctx).apply {
            text = "PILIH KARAKTER PENJUAL"
            textSize = 22f
            setTextColor(Color.parseColor("#3E2C1C"))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }
        root.addView(title)

        for (c in SellerCharacter.entries) {
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                setOnClickListener {
                    state.seller = c
                    onPick(c)
                    dismiss()
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.parseColor("#FFF3E0"))
                    cornerRadius = 24f
                    setStroke(4, Color.parseColor("#E76F51"))
                }
            }
            val name = TextView(ctx).apply {
                text = "${c.emoji} ${c.displayName}"
                textSize = 18f
                setTextColor(Color.parseColor("#B8523A"))
            }
            val desc = TextView(ctx).apply {
                text = c.description
                textSize = 13f
                setTextColor(Color.parseColor("#5D4037"))
                setPadding(0, 8, 0, 8)
            }
            val perks = TextView(ctx).apply {
                text = "Keunggulan:\n" + c.perks.joinToString("\n") { "• $it" }
                textSize = 12f
                setTextColor(Color.parseColor("#43A047"))
            }
            card.addView(name); card.addView(desc); card.addView(perks)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
            root.addView(card, lp)
        }
        scroll.addView(root)
        return scroll
    }
}
