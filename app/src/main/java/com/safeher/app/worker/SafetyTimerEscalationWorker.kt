package com.safeher.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safeher.app.core.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SafetyTimerEscalationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showTimerEscalationWarningNotification(30)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
