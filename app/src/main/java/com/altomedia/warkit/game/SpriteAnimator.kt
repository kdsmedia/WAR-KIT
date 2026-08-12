package com.altomedia.warkit.game

import com.altomedia.warkit.model.Customer

/**
 * Menghitung frame atlas karakter berdasarkan fase & waktu animasi,
 * sehingga kaki & tangan bergerak natural (walk cycle) saat pelanggan
 * berjalan, dan idle (napas) saat memilih/membayar.
 */
object SpriteAnimator {

    /** FPS animasi langkah. */
    private const val WALK_FPS = 10f
    private const val IDLE_FPS = 3f

    /**
     * Index frame atlas (0..FRAMES-1).
     * - ENTERING / QUEUING / LEAVING -> walk cycle (frame 0..WALK_FRAMES-1)
     * - PICKING / PAYING -> idle (frame WALK_FRAMES..FRAMES-1)
     */
    fun frameIndex(c: Customer, animTickMs: Long): Int {
        return when (c.phase) {
            Customer.Phase.ENTERING, Customer.Phase.QUEUING, Customer.Phase.LEAVING -> {
                val f = ((animTickMs / 1000f * WALK_FPS).toInt() + c.id.toInt()) %
                    AssetLoader.WALK_FRAMES
                f.coerceIn(0, AssetLoader.WALK_FRAMES - 1)
            }
            Customer.Phase.PICKING, Customer.Phase.PAYING -> {
                val base = AssetLoader.WALK_FRAMES
                val f = ((animTickMs / 1000f * IDLE_FPS).toInt() + c.id.toInt()) %
                    AssetLoader.IDLE_FRAMES
                (base + f.coerceIn(0, AssetLoader.IDLE_FRAMES - 1))
            }
            Customer.Phase.DONE -> AssetLoader.WALK_FRAMES
        }
    }
}
