package com.advice.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.advice.core.utils.NotificationHelper
import com.advice.data.datasource.EventsDataSource
import kotlinx.coroutines.flow.first
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val eventsDataSource by inject<EventsDataSource>()
    private val notificationHelper by inject<NotificationHelper>()

    override suspend fun doWork(): Result {
        val conference = inputData.getString(INPUT_CONFERENCE)
        if (conference == null) {
            Timber.e("Could not fetch the current conference.")
            return Result.failure()
        }

        val id = inputData.getLong(INPUT_ID, -1)
        if (id == -1L) {
            Timber.e("Could not get the target id from the inputData.")
            return Result.failure()
        }

        val event = eventsDataSource.get().first().find { it.id == id }
        if (event == null) {
            Timber.e("Could not find the target event.")
            return Result.failure()
        }

        // Event has already happened, skip this notification
        if (event.hasStarted || event.hasFinished) {
            Timber.e("Event has already finished.")
            return Result.success()
        }

        notificationHelper.notifyStartingSoon(context, event)

        return Result.success()
    }

    companion object {
        const val INPUT_CONFERENCE = "INPUT_CONFERENCE"
        const val INPUT_ID = "INPUT_ID"
    }
}