package com.altomedia.warkit.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.altomedia.warkit.model.CustomerType

/**
 * Memuat & men-cache aset bitmap dari folder assets:
 *  - Karakter atlas (sprite sheet walk cycle) per tipe pelanggan
 *  - Ikon tombol kartun 2D (transparan)
 *
 * Atlas karakter: satu PNG berisi 8 frame horizontal (6 walk + 2 idle),
 * ukuran frame 96x128.
 */
object AssetLoader {

    private const val FRAME_W = 96
    private const val FRAME_H = 128
    private const val FRAMES = 8
    const val WALK_FRAMES = 6
    const val IDLE_FRAMES = 2

    private val bitmapCache = object : LruCache<String, Bitmap>(48) {}

    private fun loadAsset(context: Context, path: String): Bitmap? {
        bitmapCache.get(path)?.let { return it }
        return try {
            context.assets.open(path).use { ins ->
                val bmp = BitmapFactory.decodeStream(ins)
                if (bmp != null) bitmapCache.put(path, bmp)
                bmp
            }
        } catch (_: Exception) { null }
    }

    /** Atlas karakter untuk tipe pelanggan (atau "kasir"). */
    fun characterAtlas(context: Context, type: CustomerType): Bitmap? {
        val name = when (type) {
            CustomerType.IBU_RUMAH_TANGGA -> "ibu"
            CustomerType.ANAK_SEKOLAH -> "anak"
            CustomerType.OJOL -> "ojol"
            CustomerType.PETANI -> "petani"
            CustomerType.KARYAWAN -> "karyawan"
        }
        return loadAsset(context, "characters/$name.png")
    }

    /** Kasir/seller atlas. */
    fun cashierAtlas(context: Context): Bitmap? = loadAsset(context, "characters/kasir.png")

    /**
     * Ambil satu frame dari atlas karakter.
     * @param phaseIndex 0..WALK_FRAMES-1 = walk, WALK_FRAMES..FRAMES-1 = idle
     */
    fun characterFrame(atlas: Bitmap, phaseIndex: Int): Bitmap {
        val idx = phaseIndex.coerceIn(0, FRAMES - 1)
        return Bitmap.createBitmap(atlas, idx * FRAME_W, 0, FRAME_W, FRAME_H)
    }

    fun frameWidth() = FRAME_W
    fun frameHeight() = FRAME_H
    fun totalFrames() = FRAMES

    /** Ikon tombol kartun (transparan). */
    fun icon(context: Context, name: String): Bitmap? =
        loadAsset(context, "icons/$name.png")

    /** Preload semua aset agar siap dipakai (dipanggil sekali saat startup). */
    fun preload(context: Context) {
        for (t in CustomerType.values()) characterAtlas(context, t)
        cashierAtlas(context)
        val icons = listOf("warehouse", "upgrade", "employees", "decoration", "products",
            "missions", "branches", "operations", "business", "events", "provinces",
            "modern", "achievements", "open")
        for (i in icons) icon(context, i)
    }
}
