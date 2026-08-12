package com.altomedia.warkit.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Dialog cerita intro (BAB 1 & README). Tampil sekali di awal.
 */
class StoryIntroDialog(
    context: Context,
    private val onContinue: () -> Unit
) : Dialog(context) {

    private val steps = listOf(
        "Pagi yang cerah menyelimuti sebuah desa kecil di Indonesia. Di pinggir jalan desa berdiri sebuah warung kayu sederhana yang dulu selalu ramai. Warung itu diwariskan oleh kedua orang tuamu.\n\nKarena usia semakin tua dan minimnya modal, warung mulai kehilangan pelanggan. Rak-rak kosong, cat pudar, hanya beberapa barang tersisa.\n\nKini kau mengambil alih warung itu: mengubah warung kecil menjadi jaringan warung terbesar di Indonesia.",
        "KONDISI AWAL\n\n💵 Uang: Rp500.000\n💎 Diamond: 0\n📈 Level: 1\n📦 Rak: 2 buah (Level 1)\n🏪 Gudang: Level 1\n🛒 Produk: 5 jenis (Beras, Mi Instan, Minyak, Air Mineral, Telur)\n👥 Pelanggan: Sangat sedikit",
        "CARA BERMAIN\n\n1. Tekan tombol 🟢 BUKA WARUNG untuk mulai melayani pelanggan.\n2. Pelanggan masuk, ambil barang, antri di kasir, bayar.\n3. Jaga stok: buka 🏪 GUDANG untuk beli stok & isi rak.\n4. Naik level untuk buka produk, rak, pegawai, dekorasi, & cabang baru.\n5. Selesaikan 🎯 MISI untuk hadiah uang, diamond, EXP, booster & chest.\n6. ⬆️ UPGRADE rak & gudang untuk kapasitas lebih besar.\n\nWarung berkembang walau kau offline (pendapatan idle). Selamat berdagang!"
    )

    private var idx = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            setBackgroundColor(Color.parseColor("#FDF6E3"))
        }
        val title = TextView(ctx).apply {
            text = if (idx == 0) "🌅 BAB 1 — AWAL SEBUAH WARUNG"
            else if (idx == 1) "📋 KONDISI AWAL"
            else "🎮 CARA BERMAIN"
            textSize = 20f; setTextColor(Color.parseColor("#B8523A"))
            gravity = Gravity.CENTER; setPadding(0, 8, 0, 16)
        }
        val body = TextView(ctx).apply {
            text = steps[idx]; textSize = 14f
            setTextColor(Color.parseColor("#3E2C1C")); setPadding(0, 8, 0, 24)
        }
        val btn = Button(ctx).apply {
            text = if (idx < steps.lastIndex) "Lanjut ➡️" else "Mulai Berdagang! 🚀"
            setOnClickListener {
                if (idx < steps.lastIndex) {
                    idx++; refresh()
                } else {
                    onContinue(); dismiss()
                }
            }
        }
        root.addView(title); root.addView(body); root.addView(btn)
        scroll.addView(root)
        return scroll
    }

    private fun refresh() { setContentView(buildView()) }
}
