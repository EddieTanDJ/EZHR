package com.example.ezhr


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

const val notificationID = 1
const val channelID = "REMINDER BORADCAST RECEIVER"
const val TITLE = "Remember to clock in"

class Notification : BroadcastReceiver() {
    val TAG: String = "Notification"


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: called")
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_baseline_access_time)
            .setContentTitle(TITLE)
            .setContentText("Please remember to clock in for your shift.")
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, LoginActivity::class.java),
                    PendingIntent.FLAG_MUTABLE
                )
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }
}