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
        if (title == null) title = Typeface.createFromAsset(ctx.assets, "fonts/lilita_one.ttf")
        if (button == null) button = Typeface.createFromAsset(ctx.assets, "fonts/fredoka_bold.ttf")
        if (body == null) body = Typeface.createFromAsset(ctx.assets, "fonts/baloo_2.ttf")
    }

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
