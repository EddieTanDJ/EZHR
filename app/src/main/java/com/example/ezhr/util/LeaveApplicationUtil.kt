package com.example.ezhr.util

object LeaveApplicationUtil {

    private val leaveTypes = listOf("Annual","Medical","Compassionate","Maternity")
    private val leaveStatuses = listOf("Pending","Approved","Rejected")

    fun validateLeaveApplicationInput(
        startDate : String,
        endDate : String,
        leaveType : String,
        leaveStatus : String,
    ):Boolean {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            return false
        }
        if (leaveType !in leaveTypes) {
            return false
        }
        if (leaveStatus !in leaveStatuses ) {
            return false
        }
        return true

    }
}

