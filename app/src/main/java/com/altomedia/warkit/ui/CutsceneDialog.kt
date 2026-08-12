package com.altomedia.warkit.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Cutscene BAB 20: warung menjadi pusat belanja desa, transisi ke ekspansi
 * cabang ke kota-kota Indonesia (fitur cabang akan datang di BAB 21+).
 */
class CutsceneDialog(
    context: Context,
    private val onContinue: () -> Unit
) : Dialog(context) {

    private val steps = listOf(
        "Setelah beberapa minggu berkembang, warung kini dikenal hampir seluruh warga desa. Pelanggan mulai datang dari desa tetangga. Pendapatan meningkat berkali-kali lipat dibanding saat pertama memulai.",
        "Seorang pengusaha berkata:\n\n«Warungmu berkembang sangat cepat. Jika ingin menjadi lebih besar, kamu harus membuka cabang di kota lain. Persaingan akan semakin sulit, tetapi keuntungan juga jauh lebih besar.»",
        "🗺️ PETA INDONESIA\n\nLayar menampilkan peta Indonesia dengan beberapa kota yang masih terkunci:\n\n📍 Jakarta (terkunci)\n📍 Bandung (terkunci)\n📍 Surabaya (terkunci)\n📍 Medan (terkunci)\n📍 Makassar (terkunci)\n\n«Perjalananmu baru saja dimulai. Bangun jaringan warung terbesar di Indonesia.»",
        "BAB 11-20 SELESAI!\n\nFitur yang sudah terbuka:\n• Pegawai (kasir, stocker, cleaner, manajer)\n• Supplier (Desa, Kota, Pabrik)\n• Produk kategori baru (Deterjen, Sikat/Pasta Gigi, Biskuit, Permen, Saus)\n• Dekorasi (pot bunga, banner, lampu, cat, neon, kanopi)\n• Pelanggan VIP\n• Reputasi bertingkat (Pemula → Legendaris)\n• Waktu Siang/Malam\n• Cuaca dinamis\n\nBab berikutnya akan membuka fitur CABANG & ekspansi bisnis."
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
        root.addView(TextView(ctx).apply {
            text = "🌟 BAB 20 — WARUNGMU MENJADI PUSAT BELANJA DESA"
            textSize = 20f; setTextColor(Color.parseColor("#B8523A"))
            gravity = Gravity.CENTER; setPadding(0, 8, 0, 16)
        })
        root.addView(TextView(ctx).apply {
            text = steps[idx]; textSize = 14f
            setTextColor(Color.parseColor("#3E2C1C")); setPadding(0, 8, 0, 24)
        })
        root.addView(Button(ctx).apply {
            text = if (idx < steps.lastIndex) "Lanjut ➡️" else "Lanjutkan Petualangan! 🚀"
            setOnClickListener {
                if (idx < steps.lastIndex) { idx++; refresh() }
                else { onContinue(); dismiss() }
            }
        })
        scroll.addView(root)
        return scroll
    }

    private fun refresh() { setContentView(buildView()) }
}
