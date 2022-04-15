package com.example.ezhr.data

import java.io.Serializable

data class LeaveStatus(
    val leaveType: String? = null,
    val submissionDate: String? = null,
    var leaveStatus: String? = null,
    val startLeaveDate: String? = null,
    val endLeaveDate: String? = null,
    val numberOfDays: Int? = null,
    val userID: String? = null,
    var uploadedImg: String? = null
) : Serializable

