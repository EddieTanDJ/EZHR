package com.example.ezhr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.repository.AttendanceRepository

class AttendanceHistoryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceHistoryViewModel::class.java)) {
            return AttendanceHistoryViewModel(AttendanceRepository) as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }

}