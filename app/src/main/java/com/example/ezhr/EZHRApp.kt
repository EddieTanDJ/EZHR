package com.example.ezhr

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.example.ezhr.repository.*
import java.util.*


class EZHRApp : Application() {
    private val TAG = "EZHRApp"
    val accountRepo by lazy { AccountRepository(this) }
    val balanceRepo by lazy { BalanceRepository() }
    val loginRepo by lazy { LoginRepository(this) }
    val attendanceRepo by lazy { AttendanceRepository(this) }
    val claimBalanceRepo by lazy { ClaimBalanceRepository() }
    val applyClaimRepo by lazy { ApplyClaimRepository() }
    val claimEditRepo by lazy { ClaimEditRepository() }


    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleNotification()
    }

    /**
     * Creates a notification channel, for OREO and higher.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: RUNNING")
        val name = "Reminder Channel"
        val desc = "A channel for reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Schedules a notification to appear in the app.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleNotification() {
        Log.d(TAG, "scheduleNotification: RUNNING")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 55)
        calendar.set(Calendar.SECOND, 0)

        val calendarActivity: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 55)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(applicationContext, Notification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarActivity.timeInMillis,
            AlarmManager.INTERVAL_DAY,// 1 day
            pendingIntent
        )
    }
}