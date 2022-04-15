package com.example.ezhr.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ezhr.data.Attendance
import com.example.ezhr.repository.AttendanceRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat

class AttendanceQrcodeViewModel(private val attendanceRepo: AttendanceRepository) : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private fun currentUser() = firebaseAuth.currentUser?.uid

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("dd/M/yyyy")

    @SuppressLint("SimpleDateFormat")
    private val stf = SimpleDateFormat("h:mm a")

    init {
        getCheckInStatus()
        getCheckOutStatus()
    }

    fun getCheckInStatus(): MutableLiveData<String> {
        return attendanceRepo.getCheckInStatus()
    }

    fun getCheckOutStatus(): MutableLiveData<String> {
        return attendanceRepo.getCheckOutStatus()
    }

    fun uploadAttendanceCheckIn(attendance: Attendance) {
        return attendanceRepo.uploadAttendanceCheckIn(attendance)
    }

    fun uploadAttendanceCheckOut(checkOutTime: String) {
        return attendanceRepo.uploadAttendanceCheckOut(checkOutTime)
    }


}