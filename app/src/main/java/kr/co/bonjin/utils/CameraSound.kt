package kr.co.bonjin.utils

import android.content.Context
import android.media.AudioManager

object CameraSound {
    fun disableShutterSound(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
    }

    fun enableShutterSound(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_MUTE, 0)
    }
}