package com.example.ezhr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LeaveStatusViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaveStatusViewModel::class.java)) {
            return LeaveStatusViewModel() as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}