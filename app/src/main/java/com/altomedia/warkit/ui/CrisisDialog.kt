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
 * Dialog Krisis Pasokan (BAB 46). Pemain ambil keputusan cepat.
 */
class CrisisDialog(
    context: Context,
    private val state: GameState,
    private val onChange: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiTheme.applyPopup(this)
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
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#FFEBEE"))
        }
        root.addView(TextView(ctx).apply {
            text = "⚠️ KRISIS PASOKAN!"; textSize = 20f
            setTextColor(Color.parseColor("#C62828")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(TextView(ctx).apply {
            text = "Gangguan distribusi akibat cuaca buruk & keterlambatan pengiriman!\n" +
                "Sisa ${state.supplyCrisis.daysLeft} hari • Severity ${(state.supplyCrisis.severity * 100).toInt()}%\n" +
                "Ambil keputusan cepat agar rak tidak kosong & pendapatan stabil."
            textSize = 12f; setTextColor(Color.parseColor("#B71C1C")); setPadding(0, 8, 0, 16)
        })

        root.addView(Button(ctx).apply {
            text = "1. Beli dari Supplier Alternatif -> Rp${(2_000_000L * (1 + state.supplyCrisis.severity)).toLong()}"
            setOnClickListener { if (state.resolveCrisisAlternativeSupplier()) { onChange(); dismiss() } }
        })
        root.addView(Button(ctx).apply {
            text = "2. Kirim Stok dari Cabang Lain (gratis, butuh cabang)"
            isEnabled = state.branches.isNotEmpty()
            setOnClickListener { if (state.resolveCrisisTransferStock()) { onChange(); dismiss() } }
        })
        root.addView(Button(ctx).apply {
            text = "3. Kurangi Promosi Sementara (gratis)"
            setOnClickListener { state.resolveCrisisReducePromo(); onChange(); dismiss() }
        })
        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 16, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }
}
