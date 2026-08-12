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
import com.altomedia.warkit.data.EmployeeBank
import com.altomedia.warkit.model.Employee

/**
 * Dialog Pegawai (BAB 11): rekrut & lihat pegawai aktif.
 */
class EmployeeDialog(
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
            setBackgroundColor(Color.TRANSPARENT)
        }
        root.addView(TextView(ctx).apply {
            text = "🧑‍💼 PEGAWAI WARUNG"; textSize = 20f
            setTextColor(Color.parseColor("#B8523A")); gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        })
        root.addView(TextView(ctx).apply {
            text = "Pegawai otomatis bekerja: mempercepat transaksi, " +
                "meningkatkan keramahan & efisiensi, dan menambah pendapatan idle.\n" +
                "Gaji harian: Rp${state.dailyWage()}"
            textSize = 12f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 8, 0, 16)
        })

        // Pegawai aktif
        root.addView(TextView(ctx).apply {
            text = "PEGAWAI AKTIF (${state.employees.size})"; textSize = 14f
            setTextColor(Color.parseColor("#43A047")); setPadding(0, 8, 0, 8)
        })
        if (state.employees.isEmpty()) {
            root.addView(TextView(ctx).apply {
                text = "Belum ada pegawai. Rekrut di bawah!"; textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
            })
        }
        for (e in state.employees) {
            root.addView(empCard(e, hired = true))
        }

        // Pegawai tersedia
        root.addView(TextView(ctx).apply {
            text = "PEGAWAI TERSEDIA UNTUK DIREKRUT"; textSize = 14f
            setTextColor(Color.parseColor("#E76F51")); setPadding(0, 24, 0, 8)
        })
        for (e in EmployeeBank.unlocked(state.level)) {
            if (state.employees.none { emp -> emp.id == e.id }) {
                root.addView(empCard(e, hired = false))
            }
        }
        if (EmployeeBank.unlocked(state.level).all { emp ->
                state.employees.any { it.id == emp.id } }) {
            root.addView(TextView(ctx).apply {
                text = "Semua pegawai tersedia sudah direkrut!"; textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
            })
        }

        root.addView(Button(ctx).apply {
            text = "Tutup"; setOnClickListener { dismiss() }
            setPadding(0, 24, 0, 0)
        })
        scroll.addView(root)
        return scroll
    }

    private fun empCard(e: Employee, hired: Boolean): LinearLayout {
        val ctx = context
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#FFF3E0"))
                cornerRadius = 16f
                if (hired) setStroke(4, Color.parseColor("#43A047"))
            }
        }
        card.addView(TextView(ctx).apply {
            text = "${e.emoji} ${e.name} (${e.role.name.lowercase().replaceFirstChar { it.uppercase() }})"
            textSize = 14f; setTextColor(Color.parseColor("#3E2C1C"))
        })
        card.addView(TextView(ctx).apply {
            text = "Kecepatan x${e.workSpeed} • Keramahan +${e.friendliness} • " +
                "Efisiensi ${e.efficiency}% • Gaji Rp${e.dailyWage}/hari"
            textSize = 11f; setTextColor(Color.parseColor("#5D4037")); setPadding(0, 4, 0, 8)
        })
        if (!hired) {
            card.addView(Button(ctx).apply {
                text = "Rekrut -> Rp${e.hireCost}"
                setOnClickListener {
                    if (state.hireEmployee(e)) { onChange(); refresh() }
                }
            })
        } else {
            card.addView(Button(ctx).apply {
                text = "Pecat"
                setOnClickListener {
                    state.fireEmployee(e); onChange(); refresh()
                }
            })
        }
        return card
    }

    private fun refresh() { setContentView(buildView()) }
}
