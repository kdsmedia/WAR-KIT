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
 * Cutscene BAB 47: Penghargaan Nasional & BAB 49: Menjadi jaringan warung terbesar.
 */
class AwardCutsceneDialog(
    context: Context,
    private val state: GameState,
    private val isRajaWarung: Boolean,
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
        if (!isRajaWarung) {
            // BAB 47: Penghargaan Nasional
            root.addView(TextView(ctx).apply {
                text = "🏅 PENGHARGAAN NASIONAL"; textSize = 22f
                setTextColor(Color.parseColor("#FFD54F")); gravity = Gravity.CENTER
                setPadding(0, 16, 0, 24)
            })
            root.addView(TextView(ctx).apply {
                text = "Karena pelayanan konsisten & reputasi tinggi, perusahaanmu " +
                    "memperoleh penghargaan sebagai salah satu jaringan warung terbaik " +
                    "di Indonesia!\n\nLogo perusahaanmu kini muncul di berbagai media."
                textSize = 14f; setTextColor(Color.parseColor("#E8EAF6")); setPadding(0, 8, 0, 24)
            })
            root.addView(TextView(ctx).apply {
                text = "🎁 Reward:\n• Reputasi Nasional (+100)\n• Bonus pelanggan permanen\n" +
                    "• Skin bangunan eksklusif\n• Dekorasi piala penghargaan"
                textSize = 13f; setTextColor(Color.parseColor("#FFD54F")); setPadding(16, 8, 16, 24)
            })
        } else {
            // BAB 49: Raja Warung
            root.addView(TextView(ctx).apply {
                text = "👑 RAJA WARUNG INDONESIA"; textSize = 22f
                setTextColor(Color.parseColor("#FFD54F")); gravity = Gravity.CENTER
                setPadding(0, 16, 0, 24)
            })
            root.addView(TextView(ctx).apply {
                text = "Setelah bertahun-tahun berkembang, perusahaanmu kini memiliki:\n" +
                    "• ${state.branches.size} cabang\n" +
                    "• ${state.totalEmployeesHired} pegawai\n" +
                    "• ${state.totalCustomersServed} pelanggan dilayani\n" +
                    "• Sistem distribusi modern\n\n" +
                    "Namamu dikenal di seluruh Indonesia sebagai pengusaha yang berhasil " +
                    "membangun usaha dari sebuah warung kecil."
                textSize = 14f; setTextColor(Color.parseColor("#E8EAF6")); setPadding(0, 8, 0, 24)
            })
            root.addView(TextView(ctx).apply {
                text = "🎁 Reward:\n• Gelar Raja Warung Indonesia\n• Skin bangunan emas\n" +
                    "• Patung maskot perusahaan\n• Bingkai profil eksklusif\n" +
                    "• Efek konfeti saat level naik"
                textSize = 13f; setTextColor(Color.parseColor("#FFD54F")); setPadding(16, 8, 16, 24)
            })
        }
        root.addView(Button(ctx).apply {
            text = "🚀 Lanjutkan"
            setOnClickListener { onContinue(); dismiss() }
        })
        scroll.addView(root)
        return scroll
    }
}
