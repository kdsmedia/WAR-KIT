package com.altomedia.warkit.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

/**
 * Tombol bulat dengan emoji/icon yang bisa di-tap. Dipakai di overlay UI.
 */
class CircleButton(
    context: Context,
    private val icon: String,
    private val color: Int,
    private val onClick: () -> Unit
) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 28f; textAlign = Paint.Align.CENTER
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 12f; textAlign = Paint.Align.CENTER
    }

    var label: String = ""

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f; val cy = height / 2f
        val r = minOf(width, height) / 2f - 6f
        bgPaint.color = color
        canvas.drawCircle(cx, cy, r, bgPaint)
        canvas.drawText(icon, cx, cy + 10f, textPaint)
        if (label.isNotEmpty()) {
            canvas.drawText(label, cx, height - 4f, labelPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            onClick()
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }
}
