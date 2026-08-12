package com.altomedia.warkit.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.altomedia.warkit.R
import com.altomedia.warkit.core.GameState
import com.altomedia.warkit.core.SaveManager
import com.altomedia.warkit.game.CircleButton
import com.altomedia.warkit.game.GameEngine
import com.altomedia.warkit.game.GameView

/**
 * Activity utama. Menggabungkan GameView (Canvas 2D) + overlay tombol aksi
 * + HUD. Mengatur save/load, intro cerita, pemilihan karakter, pendapatan
 * idle offline, dan transisi hari.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var state: GameState
    private lateinit var engine: GameEngine
    private lateinit var gameView: GameView
    private lateinit var save: SaveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        state = GameState()
        save = SaveManager(this)

        gameView = GameView(this)
        engine = GameEngine(state)
        gameView.state = state
        gameView.engine = engine

        state.onLevelUp = { lvl ->
            runOnUiThread {
                Toast.makeText(this, "🎉 Level Up! Lv.$lvl — fitur baru terbuka!", Toast.LENGTH_LONG).show()
            }
        }

        setContentView(buildLayout())

        // Load save & apply offline income (BAB 5)
        val savedAt = save.load(state)
        if (savedAt <= 0L || state.seller == null) {
            // Baru: tampilkan intro + pilih karakter
            showIntroThenPick()
        } else {
            applyOfflineIncome(savedAt)
            state.openShop()
            gameView.resetTimer()
        }

        // Day timer: maju hari tiap [dayLength] detik gameplay (BAB 18/20)
        dayHandler.postDelayed(dayRunnable, (dayLength * 1000).toLong())
    }

    private val dayLength = 120f  // 120 detik gameplay = 1 hari
    private val dayHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val dayRunnable = object : Runnable {
        override fun run() {
            if (state.shopOpen) {
                state.advanceDay()
                maybeShowCutscene()
            }
            dayHandler.postDelayed(this, (dayLength * 1000).toLong())
        }
    }

    private fun buildLayout(): FrameLayout {
        val root = FrameLayout(this)

        // Lapisan game
        root.addView(gameView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        // Lapisan tombol aksi (bawah-kiri)
        val btnBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }
        val btnSize = 140
        btnBar.addView(actionBtn("🏪", "Gudang", 0xFFE76F51.toInt()) { openWarehouse() })
        btnBar.addView(actionBtn("⬆️", "Upgrade", 0xFF6D4C41.toInt()) { openUpgrade() })
        btnBar.addView(actionBtn("🧑‍💼", "Pegawai", 0xFF1E88E5.toInt()) { openEmployees() })
        btnBar.addView(actionBtn("🎨", "Dekorasi", 0xFF8E24AA.toInt()) { openDecorations() })
        btnBar.addView(actionBtn("🛒", "Produk", 0xFFF4A261.toInt()) { openProducts() })
        btnBar.addView(actionBtn("🎯", "Misi", 0xFF43A047.toInt()) { openMissions() })
        btnBar.addView(actionBtn("🗺️", "Cabang", 0xFF00897B.toInt()) { openBranches() })
        btnBar.addView(actionBtn("⚙️", "Operasional", 0xFF5C6BC0.toInt()) { openOperations() })
        btnBar.addView(actionBtn("💼", "Bisnis", 0xFF8D6E63.toInt()) { openBusiness() })
        btnBar.addView(actionBtn("📅", "Event", 0xFFE53935.toInt()) { openEvents() })
        btnBar.addView(actionBtn("🇮🇩", "Provinsi", 0xFF00ACC1.toInt()) { openProvinces() })
        btnBar.addView(actionBtn("💳", "Modern", 0xFFAB47BC.toInt()) { openModern() })
        btnBar.addView(actionBtn("🏆", "Prestasi", 0xFFFFA000.toInt()) { openAchievements() })
        val btnLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            btnSize + 24
        ).apply { gravity = Gravity.BOTTOM or Gravity.START }
        root.addView(btnBar, btnLp)

        // Tombol Buka/Tutup Warung (kanan-bawah) - BAB 2
        val openBtn = actionBtn("🟢", "Buka Warung", 0xFF43A047.toInt()) {
            if (state.shopOpen) { state.closeShop() } else { state.openShop(); gameView.resetTimer() }
            refreshOpenBtnLabel()
        }.apply { id = View.generateViewId() }
        val openLp = FrameLayout.LayoutParams(160, 160).apply {
            gravity = Gravity.BOTTOM or Gravity.END; rightMargin = 24; bottomMargin = 24
        }
        root.addView(openBtn, openLp)

        return root
    }

    private var openBtnRef: View? = null
    private fun actionBtn(
        icon: String, label: String, color: Int, onClick: () -> Unit
    ): CircleButton {
        val b = CircleButton(this, icon, color) { onClick() }
        b.label = label
        val lp = LinearLayout.LayoutParams(0, 140, 1f).apply {
            gravity = Gravity.CENTER; rightMargin = 12; leftMargin = 12
        }
        b.layoutParams = lp
        if (label == "Buka Warung") openBtnRef = b
        return b
    }

    private fun refreshOpenBtnLabel() {
        // refresh label tombol buka/tutup
        (openBtnRef as? CircleButton)?.let {
            it.label = if (state.shopOpen) "Tutup Warung" else "Buka Warung"
            it.invalidate()
        }
    }

    private fun showIntroThenPick() {
        StoryIntroDialog(this) {
            CharacterSelectDialog(this, state) { ch ->
                Toast.makeText(this, "Karakter dipilih: ${ch.displayName} ${ch.emoji}", Toast.LENGTH_SHORT).show()
                state.openShop()
                gameView.resetTimer()
                save.save(state)
            }.show()
        }.show()
    }

    private fun applyOfflineIncome(savedAt: Long) {
        val now = System.currentTimeMillis()
        val awaySec = ((now - savedAt) / 1000).coerceIn(0L, 24 * 3600L) // max 24 jam
        if (awaySec > 5) {
            val income = state.applyOfflineIncome(awaySec)
            if (income > 0) {
                Toast.makeText(this,
                    "💰 Pendapatan offline (${awaySec/60}m): Rp$income",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    // === Dialog handlers ===
    private fun openWarehouse() {
        WarehouseDialog(this, state) { save.save(state) }.show()
    }
    private fun openUpgrade() {
        UpgradeDialog(this, state) { save.save(state) }.show()
    }
    private fun openProducts() {
        ProductDialog(this, state).show()
    }
    private fun openMissions() {
        MissionDialog(this, state) { save.save(state) }.show()
    }
    private fun openEmployees() {
        EmployeeDialog(this, state) { save.save(state) }.show()
    }
    private fun openDecorations() {
        DecorationDialog(this, state) { save.save(state) }.show()
    }
    private fun openBranches() {
        BranchDialog(this, state) { save.save(state) }.show()
    }
    private fun openOperations() {
        OperationsDialog(this, state) { save.save(state) }.show()
    }
    private fun openEvents() {
        EventDialog(this, state) { save.save(state) }.show()
    }
    private fun openBusiness() {
        BusinessDialog(this, state) { save.save(state) }.show()
    }
    private fun openProvinces() {
        ProvinceDialog(this, state) { save.save(state) }.show()
    }
    private fun openModern() {
        ModernDialog(this, state) { save.save(state) }.show()
    }
    private fun openAchievements() {
        AchievementDialog(this, state) { save.save(state) }.show()
    }

    /** Trigger cutscene BAB 20 saat reputasi mencapai tier tertentu (sekali). */
    private var cutsceneShown = false
    private var nationalCutsceneShown = false
    private var nationalAwardShown = false
    private var rajaWarungShown = false
    private fun maybeShowCutscene() {
        if (!cutsceneShown && state.reputation >= 700) {  // Warung Terkenal
            cutsceneShown = true
            CutsceneDialog(this) {
                save.save(state)
            }.show()
        }
        // BAB 40: cutscene jaringan nasional saat level 20+ & >= 3 cabang
        if (!nationalCutsceneShown && state.level >= 20 && state.branches.size >= 3) {
            nationalCutsceneShown = true
            NationalCutsceneDialog(this, state) {
                save.save(state)
            }.show()
        }
        // BAB 47: penghargaan nasional
        if (!nationalAwardShown && state.checkNationalAward()) {
            nationalAwardShown = true
            AwardCutsceneDialog(this, state, isRajaWarung = false) {
                save.save(state)
            }.show()
        }
        // BAB 49: gelar Raja Warung
        if (!rajaWarungShown && state.checkRajaWarung()) {
            rajaWarungShown = true
            AwardCutsceneDialog(this, state, isRajaWarung = true) {
                save.save(state)
            }.show()
        }
        // BAB 46: krisis pasokan — tampilkan dialog
        if (state.supplyCrisis.active && !crisisDialogShown) {
            crisisDialogShown = true
            CrisisDialog(this, state) {
                save.save(state); crisisDialogShown = state.supplyCrisis.active
            }.show()
        } else if (!state.supplyCrisis.active) {
            crisisDialogShown = false
        }
    }
    private var crisisDialogShown = false

    override fun onPause() {
        super.onPause()
        state.closeShop()
        save.save(state)
    }

    override fun onDestroy() {
        super.onDestroy()
        dayHandler.removeCallbacks(dayRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (state.seller != null) {
            state.openShop()
            gameView.resetTimer()
        }
    }
}
