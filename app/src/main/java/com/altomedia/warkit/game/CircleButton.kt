package com.altomedia.warkit.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.MotionEvent
import android.view.View

/**
 * Tombol modern (rounded-rect dengan gradient, bayangan, ikon emoji + label).
 * Dipakai di overlay UI. Gaya flat 2D modern profesional.
 */
class CircleButton(
    context: Context,
    private val icon: String,
    private val color: Int,
    private val onClick: () -> Unit
) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 26f; textAlign = Paint.Align.CENTER
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 11f; textAlign = Paint.Align.CENTER
    }
    private var pressed = false

    var label: String = ""

    private fun darken(c: Int, f: Float): Int {
        val r = (Color.red(c) * f).toInt().coerceIn(0, 255)
        val g = (Color.green(c) * f).toInt().coerceIn(0, 255)
        val b = (Color.blue(c) * f).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val pad = 6f
        val rect = RectF(pad, pad, w - pad, h - pad - 16f)
        // Bayangan
        shadowPaint.color = Color.parseColor("#33000000")
        canvas.drawRoundRect(RectF(pad + 2f, pad + 4f, w - pad + 2f, h - pad - 12f), 14f, 14f, shadowPaint)
        // Latar gradient
        bgPaint.shader = LinearGradient(0f, pad, 0f, h - pad, intArrayOf(color, darken(color, 0.72f)),
            null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)
        // Highlight atas (kilau)
        val hl = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#22FFFFFF") }
        canvas.drawRoundRect(RectF(pad + 4f, pad + 4f, w - pad - 4f, pad + (h - pad) * 0.45f), 12f, 12f, hl)
        // Ikon
        val cx = w / 2f
        val cy = (rect.top + rect.bottom) / 2f
        canvas.drawText(icon, cx, cy + 9f, textPaint)
        // Label
        if (label.isNotEmpty()) {
            canvas.drawText(label, cx, h - 4f, labelPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { pressed = true; invalidate() }
            MotionEvent.ACTION_UP -> { pressed = false; invalidate(); onClick() }
            MotionEvent.ACTION_CANCEL -> { pressed = false; invalidate() }
        }
        return true
    }
}
