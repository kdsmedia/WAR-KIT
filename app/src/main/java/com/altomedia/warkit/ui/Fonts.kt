package com.altomedia.warkit.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.Button
import android.widget.TextView

/**
 * Typeface tunggal untuk seluruh aplikasi WARKIT.
 *
 * Aturan font:
 *  - Lilita One   -> judul game & angka/koin
 *  - Fredoka Bold -> tombol
 *  - Baloo 2 Reg  -> teks/deskripsi
 */
object Fonts {
    @Volatile private var title: Typeface? = null      // Lilita One
    @Volatile private var button: Typeface? = null     // Fredoka Bold
    @Volatile private var body: Typeface? = null       // Baloo 2 Regular

    fun init(ctx: Context) {
        if (title == null) title = load(ctx, "fonts/lilita_one.ttf", Typeface.DEFAULT)
        if (button == null) button = load(ctx, "fonts/fredoka_bold.ttf", Typeface.DEFAULT_BOLD)
        if (body == null) body = load(ctx, "fonts/baloo_2.ttf", Typeface.DEFAULT)
    }

    private fun load(ctx: Context, path: String, fallback: Typeface): Typeface =
        try { Typeface.createFromAsset(ctx.assets, path) ?: fallback }
        catch (_: Throwable) { fallback }

    /** Lilita One — judul game & angka/koin. */
    fun title(): Typeface = title ?: Typeface.DEFAULT
    /** Fredoka Bold — tombol. */
    fun button(): Typeface = button ?: Typeface.DEFAULT_BOLD
    /** Baloo 2 Regular — teks/deskripsi. */
    fun body(): Typeface = body ?: Typeface.DEFAULT

    fun applyTitle(v: TextView) { v.typeface = title() }
    fun applyButton(v: TextView) { v.typeface = button() }
    fun applyBody(v: TextView) { v.typeface = body() }
    fun applyButton(v: Button) { v.typeface = button() }
}
