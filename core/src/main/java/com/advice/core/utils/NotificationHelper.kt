package com.advice.core.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.advice.core.local.Event
import com.shortstack.core.R
import timber.log.Timber

class NotificationHelper(private val context: Context)  {

    private val manager = NotificationManagerCompat.from(context)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_UPDATES,
                "Schedule Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    description = "Notifications about changes within the events"
                    enableLights(true)
                    lightColor = Color.MAGENTA
                }

            manager.createNotificationChannel(channel)
        }
    }

    private fun getStartingSoonNotification(item: Event): Notification {
        val builder = notificationBuilder

        builder.setContentTitle(item.title)
        builder.setContentText(
            String.format(
                context.getString(R.string.notification_text),
                item.location.name
            )
        )

//        setItemPendingIntent(builder, item)

        return builder.build()
    }

    private fun getUpdatedEventNotification(item: Event): Notification {
        val builder = notificationBuilder

        builder.setContentTitle(item.title)
        builder.setContentText(context.getString(R.string.notification_updated))

//        setItemPendingIntent(builder, item)

        return builder.build()
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val color = ContextCompat.getColor(context, R.color.colorPrimary)

            val builder = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            builder.setSound(soundUri)
            builder.setVibrate(longArrayOf(0, 250, 500, 250))
            builder.setLights(Color.MAGENTA, 3000, 1000)

            builder.setSmallIcon(R.drawable.skull)
            builder.color = color
            builder.setAutoCancel(true)

            return builder
        }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setItemPendingIntent(builder: NotificationCompat.Builder, item: Event? = null) {
//        val intent = Intent(context, MainActivity::class.java)
//
//        if (item != null) {
//            val bundle = Bundle()
//            bundle.putLong("target", item.id)
//            intent.putExtras(bundle)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
//
//        builder.setContentIntent(pendingIntent)
    }


    private fun notify(id: Int, notification: Notification) {
        manager.notify(id, notification)
    }

    fun notifyStartingSoon(context: Context, event: Event) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        manager.notify(event.id.toInt(), getStartingSoonNotification(event))
    }

    fun updatedBookmarks(updatedBookmarks: List<Event>) {
        updatedBookmarks.forEach {
            notify(it.id.toInt(), getUpdatedEventNotification(it))
        }
    }

    companion object {
        private const val CHANNEL_UPDATES = "updates_channel"

    }
}
