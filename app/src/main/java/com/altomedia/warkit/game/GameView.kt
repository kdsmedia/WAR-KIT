package com.altomedia.warkit.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.altomedia.warkit.core.GameState
import com.altomedia.warkit.data.ProductCatalog
import com.altomedia.warkit.model.Customer
import com.altomedia.warkit.model.Satisfaction
import kotlin.math.abs
import kotlin.math.sin

/**
 * View Canvas 2D yang menggambar scene warung portrait + pelanggan animasi
 * + HUD modern. Game loop berjalan via invalidasi berulang (onDraw -> invalidate).
 */
class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var state: GameState? = null
    var engine: GameEngine? = null

    private var lastTime = 0L
    private val frameMs = 1000L / 60
    private var animTick = 0L

    // Paints (dibuat sekali, di-reuse)
    private val skyPaint = Paint()
    private var skyShader: Shader? = null
    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val floorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var floorShader: Shader? = null
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
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var hudShader: Shader? = null

    init {
        wallPaint.color = Color.parseColor("#FFF3E0")
        textPaint.color = Color.parseColor("#3E2C1C")
        textPaint.textSize = 28f
        textSmall.color = Color.parseColor("#5D4037")
        textSmall.textSize = 18f
        textBold.color = Color.WHITE
        textBold.textSize = 26f
        textBold.isFakeBoldText = true
        panelPaint.color = Color.parseColor("#FFF8E7")
        panelPaint.alpha = 235
        shelfWood.color = Color.parseColor("#A1887F")
        shelfMetal.color = Color.parseColor("#90A4AE")
        shelfPaint.color = Color.parseColor("#6D4C41")
        custPaint.color = Color.parseColor("#42A5F5")
        patiencePaint.color = Color.parseColor("#66BB6A")
        doorPaint.color = Color.parseColor("#5D4037")
        accentPaint.color = Color.parseColor("#E76F51")
        cardPaint.color = Color.parseColor("#FFFFFF")
        shadowPaint.color = Color.parseColor("#22000000")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        engine?.configure(w.toFloat(), h.toFloat())
        // Langit gradien hangat (modern)
        skyShader = LinearGradient(0f, 0f, 0f, h * 0.45f,
            intArrayOf(Color.parseColor("#FFE0B2"), Color.parseColor("#FFCCBC"), Color.parseColor("#FDF6E3")),
            null, Shader.TileMode.CLAMP)
        // Lantai gradien kayu
        floorShader = LinearGradient(0f, h * 0.62f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#BCAAA4"), Color.parseColor("#8D6E63")),
            null, Shader.TileMode.CLAMP)
        // HUD gradien atas (modern gelap-transparan)
        hudShader = LinearGradient(0f, 0f, 0f, 100f,
            intArrayOf(Color.parseColor("#D03E2C1C"), Color.parseColor("#003E2C1C")),
            null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.elapsedRealtime()
        val dt = if (lastTime == 0L) 0.016f else (now - lastTime) / 1000f
        lastTime = now
        animTick = now

        engine?.update(dt.coerceAtMost(0.1f))

        val s = state ?: return
        drawScene(canvas, s)
        drawShelves(canvas, s)
        drawCustomers(canvas, s)
        drawHUD(canvas, s)

        // Loop berikutnya
        postInvalidateDelayed(frameMs)
    }

    private fun drawScene(canvas: Canvas, s: GameState) {
        val w = width.toFloat(); val h = height.toFloat()
        // Langit gradien
        skyPaint.shader = skyShader
        canvas.drawRect(0f, 0f, w, h * 0.62f, skyPaint)
        // Awan sederhana (modern flat)
        val cloudP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#33FFFFFF") }
        drawCloud(canvas, w * 0.18f, h * 0.10f, 46f, cloudP)
        drawCloud(canvas, w * 0.78f, h * 0.16f, 38f, cloudP)
        // Dinding warung (atas)
        wallPaint.color = Color.parseColor("#FFF3E0")
        canvas.drawRect(0f, h * 0.30f, w, h * 0.62f, wallPaint)
        // Garis dinding aksen modern (strip horizontal)
        val stripP = Paint().apply { color = Color.parseColor("#FFE0B2"); strokeWidth = 4f }
        canvas.drawLine(0f, h * 0.46f, w, h * 0.46f, stripP)

        // Lantai gradien
        floorPaint.shader = floorShader
        canvas.drawRect(0f, h * 0.62f, w, h, floorPaint)
        // Garis lantai aksen
        val lineP = Paint().apply { color = Color.parseColor("#6D4C41"); strokeWidth = 3f }
        canvas.drawLine(0f, h * 0.62f, w, h * 0.62f, lineP)
        // Ubin lantai (garis vertikal halus)
        val tileP = Paint().apply { color = Color.parseColor("#33FFFFFF"); strokeWidth = 2f }
        for (i in 1 until 6) {
            val x = w * i / 6f
            canvas.drawLine(x, h * 0.62f, x - w * 0.05f, h, tileP)
        }

        // Pintu masuk (tengah-bawah) — modern glass door
        val doorW = w * 0.18f
        val doorX = w * 0.50f - doorW / 2f
        val doorY = h * 0.62f
        doorPaint.color = Color.parseColor("#5D4037")
        canvas.drawRect(doorX, doorY, doorX + doorW, h * 0.86f, doorPaint)
        // Kaca pintu
        val glassP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B3E1FF") }
        canvas.drawRect(doorX + 6f, doorY + 6f, doorX + doorW - 6f, h * 0.86f - 6f, glassP)
        textSmall.color = Color.WHITE; textSmall.textSize = 14f
        if (s.shopOpen) {
            canvas.drawText("🟢 BUKA", doorX + 8f, doorY - 8f, textSmall)
        } else {
            canvas.drawText("🔴 TUTUP", doorX + 8f, doorY - 8f, textSmall)
        }
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Kasir (kiri-tengah) — meja kasir modern
        val cashierX = w * 0.18f
        val cashierY = h * 0.60f
        // Bayangan
        canvas.drawRect(cashierX - 8f, cashierY + 70f, cashierX + 150f + 8f, cashierY + 82f, shadowPaint)
        // Meja
        val deskP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8D6E63") }
        canvas.drawRect(cashierX, cashierY, cashierX + 150f, cashierY + 70f, deskP)
        // Etalase kaca di atas meja
        val caseP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#A5D6A7") }
        canvas.drawRect(cashierX + 10f, cashierY - 30f, cashierX + 140f, cashierY, caseP)
        // Mesin kasir
        val regP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#37474F") }
        canvas.drawRect(cashierX + 100f, cashierY - 24f, cashierX + 134f, cashierY - 2f, regP)
        textSmall.color = Color.WHITE; textSmall.textSize = 14f
        canvas.drawText("💳 KASIR", cashierX + 12f, cashierY + 42f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Karakter kasir/seller di belakang meja (sprite atlas)
        val kasirAtlas = AssetLoader.cashierAtlas(context)
        if (kasirAtlas != null) {
            val idx = AssetLoader.WALK_FRAMES + ((animTick / 600).toInt() % AssetLoader.IDLE_FRAMES)
            val frame = AssetLoader.characterFrame(kasirAtlas, idx)
            val fw = AssetLoader.frameWidth(); val fh = AssetLoader.frameHeight()
            val scale = 80f / fh
            val dw = fw * scale; val dh = fh * scale
            val kx = cashierX + 75f
            val ky = cashierY - dh + 8f
            val dst = RectF(kx - dw / 2f, ky, kx + dw / 2f, ky + dh)
            canvas.drawBitmap(frame, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
        }
    }

    private fun drawCloud(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        canvas.drawCircle(cx, cy, r, p)
        canvas.drawCircle(cx + r * 0.8f, cy + r * 0.2f, r * 0.8f, p)
        canvas.drawCircle(cx - r * 0.8f, cy + r * 0.2f, r * 0.7f, p)
        canvas.drawCircle(cx + r * 0.3f, cy - r * 0.5f, r * 0.7f, p)
    }

    private fun drawShelves(canvas: Canvas, s: GameState, ) {
        val w = width.toFloat(); val h = height.toFloat()
        val isMetal = s.shelfLevel >= 2
        val rackPaint = if (isMetal) shelfMetal else shelfWood
        // Rak diletakkan horizontal di bagian atas dinding (portrait)
        val rackTopY = h * 0.34f
        val rackW = w * 0.92f
        var rackX = w * 0.04f
        val rackGap = 8f
        val rackH = 56f

        s.shelves.forEachIndexed { shelfIdx, shelf ->
            if (shelfIdx != 0) return@forEachIndexed  // hanya gambar rak pertama sebagai baris utama
            // Bingkai rak dengan sudut membulat (modern)
            val rect = RectF(rackX, rackTopY, rackX + rackW, rackTopY + rackH)
            canvas.drawRoundRect(rect, 10f, 10f, rackPaint)
            // Garis rak tengah
            canvas.drawRect(rackX + 4f, rackTopY + rackH / 2f - 2f, rackX + rackW - 4f,
                rackTopY + rackH / 2f + 2f, shelfPaint)

            // Label rak (di ujung kiri, latar kecil)
            val lblBg = Paint().apply { color = Color.parseColor("#CC3E2C1C") }
            canvas.drawRoundRect(RectF(rackX + 4f, rackTopY - 22f, rackX + 96f, rackTopY - 2f), 6f, 6f, lblBg)
            textSmall.color = Color.WHITE; textSmall.textSize = 11f
            canvas.drawText("Rak ${shelfIdx + 1} • Lv.${s.shelfLevel}", rackX + 8f, rackTopY - 8f, textSmall)
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
                canvas.drawRect(sx + 4f, rackTopY + rackH - 16f, sx + slotW - 4f, rackTopY + rackH - 4f, shelfPaint)
                canvas.drawRect(sx + 6f, rackTopY + rackH - 14f,
                    sx + 6f + (slotW - 12f) * fillFrac.coerceIn(0f, 1f),
                    rackTopY + rackH - 6f, barPaint)

                // Emoji produk
                textPaint.textSize = 24f
                canvas.drawText(product.emoji, sx + 10f, rackTopY + rackH / 2f + 4f, textPaint)
                // Stok angka
                textSmall.textSize = 11f; textSmall.color = Color.WHITE
                canvas.drawText("${item.stock}/${item.capacity}", sx + 6f, rackTopY + rackH - 18f, textSmall)
                textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
            }
        }

        // Rak tambahan (sederhana) di bawah jika lebih dari 1 shelf
        if (s.shelves.size > 1) {
            val r2Y = h * 0.50f
            canvas.drawRoundRect(RectF(rackX, r2Y, rackX + rackW, r2Y + rackH), 10f, 10f, rackPaint)
            canvas.drawRect(rackX + 4f, r2Y + rackH / 2f - 2f, rackX + rackW - 4f,
                r2Y + rackH / 2f + 2f, shelfPaint)
            textSmall.color = Color.WHITE; textSmall.textSize = 11f
            val lblBg = Paint().apply { color = Color.parseColor("#CC3E2C1C") }
            canvas.drawRoundRect(RectF(rackX + 4f, r2Y - 22f, rackX + 110f, r2Y - 2f), 6f, 6f, lblBg)
            canvas.drawText("Rak 2 • Lv.${s.shelfLevel}", rackX + 8f, r2Y - 8f, textSmall)
            textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
            // slot produk rak 2
            val sh2 = s.shelves.getOrNull(1) ?: return
            val slotW = rackW / 3f
            sh2.forEachIndexed { slotIdx, item ->
                val product = ProductCatalog.byId(item.productId)
                val sx = rackX + slotIdx * slotW
                val fillFrac = item.stock.toFloat() / item.capacity
                val barPaint = Paint().apply {
                    color = when {
                        fillFrac > 0.5f -> Color.parseColor("#66BB6A")
                        fillFrac > 0.2f -> Color.parseColor("#FFB300")
                        else -> Color.parseColor("#EF5350")
                    }
                }
                canvas.drawRect(sx + 4f, r2Y + rackH - 16f, sx + slotW - 4f, r2Y + rackH - 4f, shelfPaint)
                canvas.drawRect(sx + 6f, r2Y + rackH - 14f,
                    sx + 6f + (slotW - 12f) * fillFrac.coerceIn(0f, 1f), r2Y + rackH - 6f, barPaint)
                textPaint.textSize = 24f
                canvas.drawText(product.emoji, sx + 10f, r2Y + rackH / 2f + 4f, textPaint)
                textSmall.textSize = 11f; textSmall.color = Color.WHITE
                canvas.drawText("${item.stock}/${item.capacity}", sx + 6f, r2Y + rackH - 18f, textSmall)
                textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
            }
        }
    }

    private fun drawCustomers(canvas: Canvas, s: GameState) {
        val bob = (sin(animTick / 150.0) * 3f).toFloat()
        for (c in s.customers) {
            val bobY = if (c.phase == Customer.Phase.ENTERING || c.phase == Customer.Phase.QUEUING ||
                c.phase == Customer.Phase.LEAVING) bob else 0f
            // Bayangan
            shadowPaint.color = Color.parseColor("#22000000")
            canvas.drawOval(c.x - 22f, c.y + 30f, c.x + 22f, c.y + 42f, shadowPaint)

            // Karakter atlas sprite (animasi kaki & tangan)
            val atlas = AssetLoader.characterAtlas(context, c.type)
            if (atlas != null) {
                val idx = SpriteAnimator.frameIndex(c, animTick)
                val frame = AssetLoader.characterFrame(atlas, idx)
                val fw = AssetLoader.frameWidth()
                val fh = AssetLoader.frameHeight()
                // Skala agar karakter ~84px tinggi, pusat di (c.x, c.y)
                val scale = 84f / fh
                val dw = fw * scale
                val dh = fh * scale
                val left = c.x - dw / 2f
                val top = c.y - dh + 30f + bobY  // kaki menyentuh c.y+30 (di atas bayangan)
                val dst = RectF(left, top, left + dw, top + dh)
                val bmpPaint = Paint(Paint.FILTER_BITMAP_FLAG)
                if (c.isVip) {
                    val auraP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#55FFD54F")
                    }
                    canvas.drawOval(c.x - dw / 2f - 4f, top + dh * 0.3f,
                        c.x + dw / 2f + 4f, top + dh + 6f, auraP)
                }
                canvas.drawBitmap(frame, null, dst, bmpPaint)
            } else {
                custPaint.color = when (c.type) {
                    com.altomedia.warkit.model.CustomerType.IBU_RUMAH_TANGGA -> Color.parseColor("#EC407A")
                    com.altomedia.warkit.model.CustomerType.ANAK_SEKOLAH -> Color.parseColor("#7E57C2")
                    com.altomedia.warkit.model.CustomerType.OJOL -> Color.parseColor("#26A69A")
                    com.altomedia.warkit.model.CustomerType.PETANI -> Color.parseColor("#66BB6A")
                    com.altomedia.warkit.model.CustomerType.KARYAWAN -> Color.parseColor("#5C6BC0")
                }
                if (c.isVip) custPaint.color = Color.parseColor("#FFD54F")
                canvas.drawRoundRect(RectF(c.x - 14f, c.y - 14f + bobY, c.x + 14f, c.y + 26f + bobY), 8f, 8f, custPaint)
            }

            // Emoji tipe kecil di atas kepala
            textPaint.textSize = 14f
            canvas.drawText(c.type.emoji, c.x - 8f, c.y - 70f + bobY, textPaint)

            // Bar kesabaran
            val patienceFrac = (1f - (c.waited / c.patience)).coerceIn(0f, 1f)
            patiencePaint.color = when (c.satisfaction) {
                Satisfaction.SANGAT_PUAS -> Color.parseColor("#66BB6A")
                Satisfaction.PUAS -> Color.parseColor("#9CCC65")
                Satisfaction.NETRAL -> Color.parseColor("#FFB300")
                Satisfaction.KECEWA -> Color.parseColor("#FB8C00")
                Satisfaction.MARAH -> Color.parseColor("#E53935")
            }
            val barBg = Paint().apply { color = Color.parseColor("#443E2C1C") }
            canvas.drawRoundRect(RectF(c.x - 22f, c.y - 82f + bobY, c.x + 22f, c.y - 76f + bobY), 3f, 3f, barBg)
            canvas.drawRoundRect(RectF(c.x - 22f, c.y - 82f + bobY,
                c.x - 22f + 44f * patienceFrac, c.y - 76f + bobY), 3f, 3f, patiencePaint)

            // Daftar belanja (kecil di atas kepala saat PICKING)
            if (c.phase == Customer.Phase.PICKING) {
                textSmall.textSize = 12f; textSmall.color = Color.parseColor("#3E2C1C")
                val items = c.shoppingList.joinToString("") { ProductCatalog.byId(it).emoji }
                val lblBg = Paint().apply { color = Color.parseColor("#CCFFFFFF") }
                canvas.drawRoundRect(RectF(c.x - items.length * 5f - 4f, c.y - 100f + bobY,
                    c.x + items.length * 5f + 4f, c.y - 86f + bobY), 5f, 5f, lblBg)
                canvas.drawText(items, c.x - items.length * 5f, c.y - 88f + bobY, textSmall)
                textSmall.textSize = 18f
            }
            // Badge VIP
            if (c.isVip) {
                val vipP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFD54F") }
                canvas.drawCircle(c.x + 16f, c.y - 64f + bobY, 6f, vipP)
                textSmall.textSize = 10f; textSmall.color = Color.parseColor("#5D4037")
                canvas.drawText("★", c.x + 13f, c.y - 61f + bobY, textSmall)
                textSmall.textSize = 18f; textSmall.color = Color.parseColor("#5D4037")
            }
        }
    }

    private fun drawHUD(canvas: Canvas, s: GameState) {
        val w = width.toFloat()
        val h = height.toFloat()
        // Background HUD atas (modern gradient)
        val hudPaint = Paint().apply { shader = hudShader }
        canvas.drawRect(0f, 0f, w, 100f, hudPaint)
        // Kartu HUD (modern card style)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#CCFFFFFF") }
        // Kartu uang
        canvas.drawRoundRect(RectF(12f, 12f, 200f, 56f), 12f, 12f, cardPaint)
        textBold.textSize = 20f; textBold.color = Color.parseColor("#3E2C1C")
        canvas.drawText("Rp${formatMoney(s.money)}", 24f, 38f, textBold)
        textSmall.color = Color.parseColor("#FFB300"); textSmall.textSize = 12f
        canvas.drawText("💎 ${s.diamond}", 24f, 52f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Kartu level + EXP
        val lvX = 212f
        canvas.drawRoundRect(RectF(lvX, 12f, lvX + 150f, 56f), 12f, 12f, cardPaint)
        textBold.color = Color.parseColor("#3E2C1C"); textBold.textSize = 18f
        canvas.drawText("Lv.${s.level}", lvX + 10f, 36f, textBold)
        val expFrac = s.exp.toFloat() / s.expToNext().coerceAtLeast(1)
        val expPaint = Paint().apply { color = Color.parseColor("#CFD8DC") }
        val expFill = Paint().apply { color = Color.parseColor("#FFD54F") }
        canvas.drawRoundRect(RectF(lvX + 56f, 30f, lvX + 142f, 42f), 5f, 5f, expPaint)
        canvas.drawRoundRect(RectF(lvX + 56f, 30f, lvX + 56f + 86f * expFrac.coerceIn(0f, 1f), 42f), 5f, 5f, expFill)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 10f
        canvas.drawText("EXP ${s.exp}/${s.expToNext()}", lvX + 60f, 52f, textSmall)
        textSmall.textSize = 18f

        // Kartu reputasi
        val repX = w - 158f
        canvas.drawRoundRect(RectF(repX, 12f, repX + 146f, 56f), 12f, 12f, cardPaint)
        val tier = s.reputationTier()
        textBold.textSize = 18f; textBold.color = Color.parseColor("#3E2C1C")
        canvas.drawText("⭐ ${s.reputation + s.decorationReputation()}", repX + 10f, 34f, textBold)
        textSmall.color = Color.parseColor("#FFB300"); textSmall.textSize = 11f
        canvas.drawText("${tier.emoji} ${tier.displayName}", repX + 10f, 50f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Kartu hari/waktu/cuaca
        val dayX = w - 158f
        canvas.drawRoundRect(RectF(dayX, 62f, dayX + 146f, 96f), 10f, 10f, cardPaint)
        textSmall.color = Color.parseColor("#3E2C1C"); textSmall.textSize = 13f
        canvas.drawText("📅 Hari ${s.day}  ${s.timeOfDay.emoji}${s.weather.emoji}", dayX + 8f, 84f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Pendapatan sesi & pelanggan (kartu kiri-bawah HUD)
        val stX = 212f
        canvas.drawRoundRect(RectF(stX, 62f, stX + 150f, 96f), 10f, 10f, cardPaint)
        textSmall.color = Color.parseColor("#1565C0"); textSmall.textSize = 13f
        canvas.drawText("📈 Rp${formatMoney(s.sessionIncome)}", stX + 8f, 80f, textSmall)
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 11f
        canvas.drawText("👥 ${s.totalCustomersServed} | 🧑‍💼 ${s.employees.size}", stX + 8f, 92f, textSmall)
        textSmall.textSize = 18f

        // Status warung (kanan atas-tengah)
        val status = if (s.shopOpen) "BUKA" else "TUTUP"
        val stP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (s.shopOpen) Color.parseColor("#A5D6A7") else Color.parseColor("#EF9A9A")
        }
        canvas.drawRoundRect(RectF(w - 60f, 62f, w - 12f, 96f), 10f, 10f, stP)
        textBold.color = Color.parseColor("#1B5E20"); textBold.textSize = 14f
        if (!s.shopOpen) textBold.color = Color.parseColor("#B71C1C")
        canvas.drawText(status, w - 50f, 84f, textBold)
        textBold.color = Color.WHITE

        // Panel info kiri-bawah (bangunan, investor, grosir, provinsi, raja warung)
        val infoX = 12f
        var infoY = h - 220f
        val infoP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#CCFFFFFF") }
        canvas.drawRoundRect(RectF(infoX, infoY, infoX + 230f, h - 20f), 10f, 10f, infoP)
        textSmall.textSize = 12f; textSmall.color = Color.parseColor("#6D4C41")
        canvas.drawText("${s.building().emoji} ${s.building().displayName}", infoX + 10f, infoY + 20f, textSmall)
        infoY += 20f
        if (s.investorActive) {
            textSmall.color = Color.parseColor("#F9A825")
            canvas.drawText("🤝 Investor: ${s.investorDaysElapsed}/30 (${s.investorDaysMet}✓)", infoX + 10f, infoY + 16f, textSmall)
            infoY += 16f
        } else if (s.investorDealAccepted) {
            textSmall.color = Color.parseColor("#43A047")
            canvas.drawText("🤝 Deal aktif x${s.investorIncomeMult}", infoX + 10f, infoY + 16f, textSmall)
            infoY += 16f
        }
        if (s.grosirUnlocked) {
            textSmall.color = Color.parseColor("#1565C0")
            canvas.drawText("📦 Grosir +Rp${s.grosirDailyIncome()}/hari", infoX + 10f, infoY + 16f, textSmall)
            infoY += 16f
        }
        if (s.provinces.isNotEmpty()) {
            textSmall.color = Color.parseColor("#00838F")
            canvas.drawText("🇮🇩 ${s.provinces.size} prov • x${s.provinceCustomerMult()}", infoX + 10f, infoY + 16f, textSmall)
            infoY += 16f
        }
        if (s.membershipTier != com.altomedia.warkit.model.MembershipTier.NONE || s.paymentLevel > 0) {
            textSmall.color = Color.parseColor("#7B1FA2")
            canvas.drawText("${s.membershipTier.emoji} ${s.paymentMethod().emoji}", infoX + 10f, infoY + 16f, textSmall)
            infoY += 16f
        }
        if (s.rajaWarungTitle) {
            textSmall.color = Color.parseColor("#FFD54F"); textSmall.textSize = 13f
            canvas.drawText("👑 Raja Warung Indonesia", infoX + 10f, infoY + 18f, textSmall)
            textSmall.textSize = 12f
            infoY += 18f
        }
        textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f

        // Event & promosi (kartu kanan-bawah)
        if (s.seasonalEvent != com.altomedia.warkit.model.SeasonalEvent.NONE ||
            s.promotion != com.altomedia.warkit.model.Promotion.TIDAK_ADA) {
            var ey = h - 90f
            canvas.drawRoundRect(RectF(w - 232f, ey, w - 12f, ey + 70f), 10f, 10f, infoP)
            if (s.seasonalEvent != com.altomedia.warkit.model.SeasonalEvent.NONE) {
                textSmall.color = Color.parseColor("#E53935"); textSmall.textSize = 12f
                canvas.drawText("${s.seasonalEvent.emoji} ${s.seasonalEvent.displayName} (${s.eventDaysLeft}h)", w - 224f, ey + 22f, textSmall)
                ey += 20f
            }
            if (s.promotion != com.altomedia.warkit.model.Promotion.TIDAK_ADA) {
                textSmall.color = Color.parseColor("#1565C0")
                canvas.drawText("${s.promotion.emoji} ${s.promotion.displayName} (${s.promotionDaysLeft}h)", w - 224f, ey + 22f, textSmall)
            }
            textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
        }
        // BAB 46: krisis pasokan (banner tengah atas)
        if (s.supplyCrisis.active) {
            val crP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C62828") }
            canvas.drawRoundRect(RectF(w / 2 - 110f, 6f, w / 2 + 110f, 30f), 6f, 6f, crP)
            textSmall.color = Color.WHITE; textSmall.textSize = 13f
            canvas.drawText("⚠️ KRISIS PASOKAN (${s.supplyCrisis.daysLeft}h)", w / 2 - 100f, 22f, textSmall)
            textSmall.color = Color.parseColor("#5D4037"); textSmall.textSize = 18f
        }
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
