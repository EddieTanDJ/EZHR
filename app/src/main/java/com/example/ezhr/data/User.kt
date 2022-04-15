package com.example.ezhr.data

import java.io.Serializable

data class User(
    var EmployeeEmail: String? = null,
    var EmployeeID: String? = null,
    var EmployeeName: String? = null,
    var Role: String? = null
) : Serializable

data class Response(
    var user: User? = null,
    var exception: String? = null
)

/**
 * This data class holds the user credentials that are saved in the DataStore. It is exposed via the Flow interface.
 * It is use for login the user that uses biometric.
 */
data class Credentials(
    val email: String? = null,
    val password: String? = null
)
