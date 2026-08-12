package com.altomedia.warkit.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.altomedia.warkit.R

/**
 * Helper gaya modern untuk popup/dialog: menerapkan latar kartu rounded,
 * header gradient, tombol modern, serta factory judul/info bergaya kartun 2D
 * modern profesional.
 */
object UiTheme {

    /** Terapkan pada dialog: latar rounded, dim, animasi, gravity center. */
    fun applyPopup(dialog: Dialog) {
        val window: Window = dialog.window ?: return
        window.setBackgroundDrawableResource(R.drawable.popup_bg)
        window.setDimAmount(0.5f)
        window.setGravity(Gravity.CENTER)
        try {
            window.setWindowAnimations(android.R.style.Animation_Dialog)
        } catch (_: Exception) { }
    }

    /** Tombol primary modern (gradient hangat, teks putih bold, rounded). */
    fun primaryButton(ctx: Context, text: String, onClick: () -> Unit): Button =
        Button(ctx).apply {
            this.text = text
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            textSize = 14f
            setBackgroundResource(R.drawable.btn_primary)
            setOnClickListener { onClick() }
        }

    /** Tombol sekunder modern (cream, border). */
    fun secondaryButton(ctx: Context, text: String, onClick: () -> Unit): Button =
        Button(ctx).apply {
            this.text = text
            setTextColor(Color.parseColor("#B8523A"))
            typeface = Typeface.DEFAULT_BOLD
            textSize = 14f
            setBackgroundResource(R.drawable.btn_secondary)
            setOnClickListener { onClick() }
        }

    /** Judul popup modern (teks besar bold, aksen oranye). */
    fun title(ctx: Context, t: String): TextView = TextView(ctx).apply {
        text = t
        textSize = 20f
        setTextColor(Color.parseColor("#B8523A"))
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setPadding(0, 10, 0, 14)
    }

    /** Sub-judul / section modern (aksen oranye terang). */
    fun subTitle(ctx: Context, t: String): TextView = TextView(ctx).apply {
        text = t
        textSize = 15f
        setTextColor(Color.parseColor("#E76F51"))
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 14, 0, 6)
    }

    /** Baris info modern (cokelat gelap). */
    fun info(ctx: Context, t: String): TextView = TextView(ctx).apply {
        text = t
        textSize = 13f
        setTextColor(Color.parseColor("#5D4037"))
        setPadding(4, 4, 4, 4)
    }

    /** Kartu konten modern (rounded, latar cream, border tipis). */
    fun card(ctx: Context): LinearLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(18, 14, 18, 14)
        val bg = GradientDrawable().apply {
            cornerRadius = 16f
            setColor(Color.parseColor("#FFF8E7"))
            setStroke(2, Color.parseColor("#FFE0B2"))
        }
        background = bg
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 12; topMargin = 6 }
    }

    /** Garis pemisah tipis. */
    fun divider(ctx: Context): View = View(ctx).apply {
        setBackgroundColor(Color.parseColor("#FFE0B2"))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 2
        ).apply { topMargin = 6; bottomMargin = 10 }
    }
}
