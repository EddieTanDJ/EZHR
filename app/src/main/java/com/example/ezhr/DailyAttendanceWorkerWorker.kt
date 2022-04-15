package com.example.ezhr

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.ezhr.repository.AttendanceRepository

class DailyAttendanceWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        AttendanceRepository.uploadAttendanceAbsent()
        Log.d(UserAttendanceManager.TAG, "Daily Attendance Worker ran successfully")

        return Result.success()
    }
}