package com.example.ezhr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.ezhr.data.Attendance
import com.example.ezhr.repository.AttendanceRepository

/*
The MainViewModel will have a mutable livedata item that holds the array list.
It’s vital to use LiveData since it notifies the UI in case of any data change.
The MainViewModel code is shown below.
*/

class AttendanceHistoryViewModel(private val attendanceRepo: AttendanceRepository.Companion) :
    ViewModel() {

    init {
        getUserAttendanceHistory()
    }

    fun getUserAttendanceHistory(): LiveData<ArrayList<Attendance>> {
        // attendanceList.value = arrayListOf(Attendance("abc", null, null, null, "late"))
        return attendanceRepo.getUserOwnAttendance()
    }
}