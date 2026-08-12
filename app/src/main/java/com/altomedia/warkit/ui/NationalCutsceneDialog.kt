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
import com.altomedia.warkit.core.GameState

/**
 * Cutscene BAB 40: Menuju jaringan bisnis nasional (100 cabang).
 * Dipicu saat pemain mencapai milestone tertentu (level 20+ & >= 3 cabang).
 */
class NationalCutsceneDialog(
    context: Context,
    private val state: GameState,
    private val onContinue: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildView())
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
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#1A237E"))
        }
        root.addView(TextView(ctx).apply {
            text = "🇮🇩 MENUJU JARINGAN NASIONAL"; textSize = 22f
            setTextColor(Color.parseColor("#FFD54F")); gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        })
        root.addView(TextView(ctx).apply {
            text = "Perjalananmu telah berubah dari sekadar menjaga warung keluarga " +
                "menjadi membangun sebuah perusahaan ritel yang berkembang pesat.\n\n" +
                "Kini kamu memiliki:\n" +
                "• ${state.employees.size} pegawai\n" +
                "• ${state.branches.size} cabang\n" +
                "• Gudang distribusi level ${state.distWarehouseLevel}\n" +
                "• Kendaraan ${state.vehicle().displayName}\n" +
                "• ${state.totalCustomersServed} pelanggan dilayani"
            textSize = 14f; setTextColor(Color.parseColor("#E8EAF6")); setPadding(0, 8, 0, 24)
        })
        root.addView(TextView(ctx).apply {
            text = "🗺️ Peta Indonesia menampilkan banyak kota yang masih terkunci..."
            textSize = 13f; setTextColor(Color.parseColor("#90CAF9")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })
        root.addView(TextView(ctx).apply {
            text = "«Masih banyak kota yang menunggu kehadiran warungmu. " +
                "Bangun jaringan ritel terbesar yang menjangkau seluruh Indonesia.»"
            textSize = 14f; setTextColor(Color.parseColor("#FFD54F"))
            setTypeface(typeface, android.graphics.Typeface.BOLD_ITALIC)
            setPadding(16, 16, 16, 24)
        })
        root.addView(TextView(ctx).apply {
            text = "🎯 TARGET BARU: 100 CABANG PERTAMA"
            textSize = 18f; setTextColor(Color.parseColor("#FF8A65")); gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 24)
        })
        root.addView(TextView(ctx).apply {
            text = "Inilah awal perjalanan menuju tingkat nasional, di mana kamu akan " +
                "menghadapi persaingan yang lebih berat, membuka wilayah baru, mengelola " +
                "puluhan cabang sekaligus, dan membangun merek warung yang dikenal di " +
                "seluruh negeri."
            textSize = 13f; setTextColor(Color.parseColor("#E8EAF6")); setPadding(0, 8, 0, 24)
        })
        root.addView(Button(ctx).apply {
            text = "🚀 Lanjutkan Perjalanan"
            setOnClickListener { onContinue(); dismiss() }
            setPadding(0, 16, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }
}
