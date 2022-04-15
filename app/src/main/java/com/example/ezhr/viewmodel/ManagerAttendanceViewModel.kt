package com.example.ezhr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.ezhr.data.Attendance
import com.example.ezhr.repository.AttendanceRepository

/*
The MainViewModel will have a mutable livedata item that holds the array list.
It’s vital to use LiveData since it notifies the UI in case of any data change.
The MainViewModel code is shown below.
*/

class ManagerAttendanceViewModel(
    application: Application,
    private val attendanceRepo: AttendanceRepository.Companion
) : AndroidViewModel(application) {

    init {
        getUserAttendance()
    }

    fun getUserAttendance(): LiveData<ArrayList<Attendance>> {
        // attendanceList.value = arrayListOf(Attendance("abc", null, null, null, "late"))
        return attendanceRepo.getUserAttendance()
    }


}