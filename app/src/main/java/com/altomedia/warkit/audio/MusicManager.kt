package com.altomedia.warkit.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.altomedia.warkit.R

/**
 * Manajer musik latar (BGM) game. Memutar loop bgm.wav secara berulang
 * dengan volume lembut. Dijeda saat activity pause, dilanjutkan saat resume.
 */
class MusicManager(context: Context) {

    private var player: MediaPlayer? = null
    private var prepared = false
    private var wantPlaying = false
    private var vol = 0.35f

    init {
        try {
            val mp = MediaPlayer.create(context, R.raw.bgm)
            if (mp != null) {
                mp.isLooping = true
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mp.setVolume(vol, vol)
                mp.setOnPreparedListener {
                    prepared = true
                    mp.setVolume(vol, vol)
                    if (wantPlaying) mp.start()
                }
                player = mp
            }
        } catch (_: Exception) {
            player = null
        }
    }

    fun start() {
        wantPlaying = true
        player?.let { mp ->
            try {
                if (prepared && !mp.isPlaying) {
                    mp.setVolume(vol, vol)
                    mp.start()
                }
            } catch (_: Exception) { }
        }
    }

    fun pause() {
        wantPlaying = false
        player?.let { mp ->
            try { if (mp.isPlaying) mp.pause() } catch (_: Exception) { }
        }
    }

    fun setVolume(v: Float) {
        vol = v.coerceIn(0f, 1f)
        try { player?.setVolume(vol, vol) } catch (_: Exception) { }
    }

    fun release() {
        wantPlaying = false
        player?.let { mp ->
            try { if (mp.isPlaying) mp.stop() } catch (_: Exception) { }
            try { mp.release() } catch (_: Exception) { }
        }
        player = null
        prepared = false
    }
}
