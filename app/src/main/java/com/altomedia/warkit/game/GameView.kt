package com.altomedia.warkit.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.altomedia.warkit.core.GameState
import com.altomedia.warkit.data.ProductCatalog
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.Satisfaction

/**
 * View Canvas 2D yang menggambar scene warung + pelanggan + HUD.
 * Game loop berjalan via invalidasi berulang (onDraw -> invalidate).
 */
class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var state: GameState? = null
    var engine: GameEngine? = null

    private var lastTime = 0L
    private val frameMs = 1000L / 60

    // Paints (dibuat sekali, di-reuse)
    private val bgPaint = Paint()
    private val floorPaint = Paint()
    private val wallPaint = Paint()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textSmall = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textBold = Paint(Paint.ANTI_ALIAS_FLAG)
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shelfPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shelfWood = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shelfMetal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val custPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val patiencePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var hudBg: Shader? = null

    init {
        bgPaint.color = Color.parseColor("#BBDEFB")
        floorPaint.color = Color.parseColor("#8D6E63")
        wallPaint.color = Color.parseColor("#FFF8E1")
        textPaint.color = Color.parseColor("#3E2C1C")
        textPaint.textSize = 28f
        textSmall.color = Color.parseColor("#5D4037")
        textSmall.textSize = 18f
        textBold.color = Color.WHITE
        textBold.textSize = 26f
        textBold.isFakeBoldText = true
        panelPaint.color = Color.parseColor("#FFF3E0")
        panelPaint.alpha = 220
        shelfWood.color = Color.parseColor("#A1887F")
        shelfMetal.color = Color.parseColor("#90A4AE")
        shelfPaint.color = Color.parseColor("#6D4C41")
        custPaint.color = Color.parseColor("#42A5F5")
        patiencePaint.color = Color.parseColor("#66BB6A")
        doorPaint.color = Color.parseColor("#5D4037")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        engine?.configure(w.toFloat(), h.toFloat())
        hudBg = LinearGradient(0f, 0f, 0f, 90f, intArrayOf(
            Color.parseColor("#CC3E2C1C"), Color.parseColor("#003E2C1C")),
            null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.elapsedRealtime()
        val dt = if (lastTime == 0L) 0.016f else (now - lastTime) / 1000f
        lastTime = now

        engine?.update(dt.coerceAtMost(0.1f))

        val s = state ?: return
        drawScene(canvas, s)
        drawCustomers(canvas, s)
        drawHUD(canvas, s)

        // Loop berikutnya
        postInvalidateDelayed(frameMs)
    }

    private fun drawScene(canvas: Canvas, s: GameState) {
        val w = width.toFloat(); val h = height.toFloat()
        // Langit
        canvas.drawRect(0f, 0f, w, h * 0.5f, bgPaint)
        // Dinding warung
        canvas.drawRect(0f, h * 0.25f, w, h * 0.7f, wallPaint)
        // Lantai
        canvas.drawRect(0f, h * 0.7f, w, h, floorPaint)
        // Garis lantai
        val lineP = Paint().apply { color = Color.parseColor("#6D4C41"); strokeWidth = 3f }
        canvas.drawLine(0f, h * 0.7f, w, h * 0.7f, lineP)

        // Rolling door (kanan belakang sebagai pintu masuk kiri? di kiri)
        val doorW = w * 0.12f
        canvas.drawRect(20f, h * 0.35f, 20f + doorW, h * 0.7f, doorPaint)
        // Label pintu
        textSmall.color = Color.WHITE
        textSmall.textSize = 16f
        if (s.shopOpen) {
            canvas.drawText("🟢 BUKA", 26f, h * 0.35f - 6f, textSmall)
        } else {
            canvas.drawText("🔴 TUTUP", 26f, h * 0.35f - 6f, textSmall)
        }
        textSmall.color = Color.parseColor("#5D4037")

        // Kasir (kanan)
        val cashierX = w * 0.72f
        val cashierY = h * 0.62f
        val cashierP = Paint().apply { color = Color.parseColor("#8D6E63") }
        canvas.drawRect(cashierX, cashierY, cashierX + 160f, cashierY + 70f, cashierP)
        // Meja kasir
        val deskP = Paint().apply { color = Color.parseColor("#A1887F") }
        canvas.drawRect(cashierX - 20f, cashierY + 40f, cashierX + 200f, cashierY + 60f, deskP)
        textSmall.color = Color.WHITE; textSmall.textSize = 16f
        canvas.drawText("💳 KASIR", cashierX + 20f, cashierY + 30f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Rak (kiri-atas sampai tengah)
        drawShelves(canvas, s, w, h)
    }

    private fun drawShelves(canvas: Canvas, s: GameState, w: Float, h: Float) {
        val isMetal = s.shelfLevel >= 2
        val rackPaint = if (isMetal) shelfMetal else shelfWood
        val shelfTopY = h * 0.30f
        val rackW = w * 0.45f
        var rackX = w * 0.15f

        s.shelves.forEachIndexed { shelfIdx, shelf ->
            // Bingkai rak
            canvas.drawRect(rackX, shelfTopY, rackX + rackW, shelfTopY + 70f, rackPaint)
            // Garis rak
            canvas.drawRect(rackX, shelfTopY + 34f, rackX + rackW, shelfTopY + 38f, shelfPaint)

            // Label rak
            textSmall.color = Color.WHITE; textSmall.textSize = 14f
            canvas.drawText("Rak ${shelfIdx + 1} (Lv.${s.shelfLevel})", rackX + 6f, shelfTopY - 4f, textSmall)
            textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

            // Slot produk di rak
            val slotW = rackW / 3f
            shelf.forEachIndexed { slotIdx, item ->
                val product = ProductCatalog.byId(item.productId)
                val sx = rackX + slotIdx * slotW
                // Stok bar
                val fillFrac = item.stock.toFloat() / item.capacity
                val barPaint = Paint().apply {
                    color = when {
                        fillFrac > 0.5f -> Color.parseColor("#66BB6A")
                        fillFrac > 0.2f -> Color.parseColor("#FFB300")
                        else -> Color.parseColor("#EF5350")
                    }
                }
                canvas.drawRect(sx + 4f, shelfTopY + 40f, sx + slotW - 4f, shelfTopY + 66f, shelfPaint)
                canvas.drawRect(sx + 6f, shelfTopY + 42f, sx + (slotW - 6f) * fillFrac.coerceAtMost(1f), shelfTopY + 64f, barPaint)

                // Emoji produk
                textPaint.textSize = 22f
                canvas.drawText(product.emoji, sx + 8f, shelfTopY + 30f, textPaint)
                // Stok angka
                textSmall.textSize = 12f; textSmall.color = Color.WHITE
                canvas.drawText("${item.stock}/${item.capacity}", sx + 6f, shelfTopY + 56f, textSmall)
                textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
            }
            rackX += rackW + 20f
        }
    }

    private fun drawCustomers(canvas: Canvas, s: GameState) {
        for (c in s.customers) {
            // Badan (lingkaran)
            custPaint.color = when (c.type) {
                com.altomedia.warkit.model.CustomerType.IBU_RUMAH_TANGGA -> Color.parseColor("#EC407A")
                com.altomedia.warkit.model.CustomerType.ANAK_SEKOLAH -> Color.parseColor("#7E57C2")
                com.altomedia.warkit.model.CustomerType.OJOL -> Color.parseColor("#26A69A")
                com.altomedia.warkit.model.CustomerType.PETANI -> Color.parseColor("#8D6E63")
                com.altomedia.warkit.model.CustomerType.KARYAWAN -> Color.parseColor("#5C6BC0")
            }
            // BAB 16: VIP lebih besar + badge bintang
            val radius = if (c.isVip) 28f else 22f
            canvas.drawCircle(c.x, c.y, radius, custPaint)
            // Emoji kepala
            textPaint.textSize = if (c.isVip) 30f else 26f
            canvas.drawText(c.type.emoji, c.x - 13f, c.y - 18f, textPaint)
            // Badge VIP
            if (c.isVip) {
                textPaint.textSize = 18f
                canvas.drawText("⭐", c.x + 14f, c.y - 18f, textPaint)
            }

            // Bar kesabaran
            val patienceFrac = (1f - (c.waited / c.patience)).coerceIn(0f, 1f)
            patiencePaint.color = when (c.satisfaction) {
                Satisfaction.SANGAT_PUAS -> Color.parseColor("#66BB6A")
                Satisfaction.PUAS -> Color.parseColor("#9CCC65")
                Satisfaction.NETRAL -> Color.parseColor("#FFB300")
                Satisfaction.KECEWA -> Color.parseColor("#FB8C00")
                Satisfaction.MARAH -> Color.parseColor("#E53935")
            }
            canvas.drawRect(c.x - 20f, c.y - 38f, c.x - 20f + 40f * patienceFrac, c.y - 32f, patiencePaint)
            // Bingkai bar
            canvas.drawRect(c.x - 20f, c.y - 38f, c.x + 20f, c.y - 32f, Paint().apply {
                color = Color.TRANSPARENT; style = Paint.Style.STROKE; strokeWidth = 1.5f
            })

            // Daftar belanja (kecil di atas kepala saat PICKING)
            if (c.phase == Customer.Phase.PICKING) {
                textSmall.textSize = 14f; textSmall.color = Color.parseColor("#3E2C1C")
                val items = c.shoppingList.joinToString("") { ProductCatalog.byId(it).emoji }
                canvas.drawText(items, c.x - items.length * 5f, c.y - 44f, textSmall)
                textSmall.textSize = 18f
            }
        }
    }

    private fun drawHUD(canvas: Canvas, s: GameState) {
        val w = width.toFloat()
        // Background HUD atas
        val hudPaint = Paint().apply { shader = hudBg }
        canvas.drawRect(0f, 0f, w, 90f, hudPaint)

        textBold.textSize = 22f; textBold.color = Color.WHITE
        // Uang
        canvas.drawText("💵 Rp${formatMoney(s.money)}", 16f, 36f, textBold)
        // Diamond
        canvas.drawText("💎 ${s.diamond}", 16f, 64f, textBold)
        // Level + EXP bar
        val lvX = 240f
        canvas.drawText("Lv.${s.level}", lvX, 36f, textBold)
        val expFrac = s.exp.toFloat() / s.expToNext().coerceAtLeast(1)
        val expPaint = Paint().apply { color = Color.parseColor("#37474F") }
        val expFill = Paint().apply { color = Color.parseColor("#FFD54F") }
        canvas.drawRect(lvX, 44f, lvX + 120f, 58f, expPaint)
        canvas.drawRect(lvX, 44f, lvX + 120f * expFrac.coerceIn(0f,1f), 58f, expFill)
        textSmall.color = Color.WHITE; textSmall.textSize = 12f
        canvas.drawText("EXP ${s.exp}/${s.expToNext()}", lvX + 4f, 54f, textSmall)
        textSmall.textSize = 18f; textSmall.color = Color.parseColor("#5D4037")

        // Reputasi + tier (BAB 17)
        val tier = s.reputationTier()
        canvas.drawText("⭐ ${s.reputation + s.decorationReputation()}", lvX + 140f, 36f, textBold)
        textSmall.color = Color.parseColor("#FFD54F"); textSmall.textSize = 12f
        canvas.drawText("${tier.emoji} ${tier.displayName}", lvX + 140f, 52f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
        // Hari
        canvas.drawText("📅 Hari ${s.day}", lvX + 280f, 36f, textBold)
        // Waktu & cuaca (BAB 18 & 19)
        textSmall.color = Color.WHITE; textSmall.textSize = 12f
        canvas.drawText("${s.timeOfDay.emoji} ${s.timeOfDay.displayName}", lvX + 280f, 52f, textSmall)
        canvas.drawText("${s.weather.emoji} ${s.weather.displayName}", lvX + 280f, 66f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
        // Pendapatan sesi
        canvas.drawText("📈 Rp${formatMoney(s.sessionIncome)}", w - 260f, 36f, textBold)
        // Pelanggan dilayani + pegawai
        canvas.drawText("👥 ${s.totalCustomersServed} | 🧑‍💼 ${s.employees.size}", w - 260f, 64f, textBold)
        // Status warung
        val status = if (s.shopOpen) "WARUNG BUKA" else "WARUNG TUTUP"
        textBold.color = if (s.shopOpen) Color.parseColor("#A5D6A7") else Color.parseColor("#EF9A9A")
        canvas.drawText(status, w - 120f, 50f, textBold)
        textBold.color = Color.WHITE
    }

    private fun formatMoney(v: Long): String {
        return when {
            v >= 1_000_000_000 -> String.format("%.2fB", v / 1_000_000_000.0)
            v >= 1_000_000 -> String.format("%.2fM", v / 1_000_000.0)
            v >= 1_000 -> String.format("%.1fK", v / 1_000.0)
            else -> v.toString()
        }
    }

    fun resetTimer() { lastTime = 0L }
}
