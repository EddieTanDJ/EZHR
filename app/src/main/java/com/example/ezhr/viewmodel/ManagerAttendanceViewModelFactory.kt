package com.example.ezhr.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.repository.AttendanceRepository

class ManagerAttendanceViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerAttendanceViewModel::class.java)) {
            return ManagerAttendanceViewModel(Application(), AttendanceRepository) as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }

}