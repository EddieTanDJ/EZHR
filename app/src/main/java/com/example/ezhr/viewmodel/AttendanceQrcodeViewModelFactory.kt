package com.example.ezhr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.repository.AttendanceRepository

class AttendanceQrcodeViewModelFactory(private val repo: AttendanceRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceQrcodeViewModel::class.java)) {
            return AttendanceQrcodeViewModel(repo) as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}