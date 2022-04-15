package com.example.ezhr.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Attendance(
    var userID: String? = "",
    var date: String? = "",
    var checkInTime: String? = "",
    var checkOutTime: String? = "",
    var status: String? = "",
    var name: String? = "",
    var email: String? = ""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userID" to userID,
            "date" to date,
            "checkInTime" to checkInTime,
            "checkOutTime" to checkOutTime,
            "status" to status,
            "name" to name,
            "email" to email
        )
    }
}




