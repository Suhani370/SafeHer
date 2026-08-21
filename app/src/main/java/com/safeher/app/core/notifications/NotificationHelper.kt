package com.safeher.app.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.safeher.app.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_SOS = "safeher_sos_channel"
        const val CHANNEL_SAFETY_TIMER = "safeher_timer_channel"
        const val CHANNEL_LOCATION = "safeher_location_channel"
        const val CHANNEL_GENERAL = "safeher_general_channel"

        const val NOTIFICATION_ID_SOS = 1001
        const val NOTIFICATION_ID_TIMER = 1002
        const val NOTIFICATION_ID_LOCATION = 1003
        const val NOTIFICATION_ID_RECORDING = 1004
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sosChannel = NotificationChannel(
                CHANNEL_SOS,
                "Emergency SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical emergency notifications when SOS is triggered"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val timerChannel = NotificationChannel(
                CHANNEL_SAFETY_TIMER,
                "Safety Timer Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for safety timer expiration and check-ins"
                enableVibration(true)
            }

            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION,
                "Live Location & Journey",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service indicators for active location sharing"
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Offline synchronization and system notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(sosChannel, timerChannel, locationChannel, generalChannel)
            )
        }
    }

    fun buildSosForegroundNotification(message: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_SOS)
            .setSmallIcon(com.safeher.app.R.drawable.ic_notification_shield)
            .setContentTitle("EMERGENCY SOS ACTIVE")
            .setContentText(message)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun buildLocationForegroundNotification(title: String, message: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_LOCATION)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText(message)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun buildAudioRecordingNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Incident Recording in Progress")
            .setContentText("Audio evidence is being recorded securely")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun showTimerEscalationWarningNotification(secondsRemaining: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SAFETY_TIMER)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Safety Timer Expired!")
            .setContentText("Confirm you are safe within $secondsRemaining seconds before SOS triggers!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TIMER, notification)
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }
}
