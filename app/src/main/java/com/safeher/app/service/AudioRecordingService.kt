package com.safeher.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.safeher.app.core.audio.AudioRecorderManager
import com.safeher.app.core.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject lateinit var audioRecorderManager: AudioRecorderManager
    @Inject lateinit var notificationHelper: NotificationHelper

    companion object {
        const val ACTION_START_RECORDING = "ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "ACTION_STOP_RECORDING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID_RECORDING,
                    notificationHelper.buildAudioRecordingNotification()
                )
                audioRecorderManager.startRecording()
            }
            ACTION_STOP_RECORDING -> {
                audioRecorderManager.stopRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorderManager.stopRecording()
    }
}
