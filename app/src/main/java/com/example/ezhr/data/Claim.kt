package com.example.ezhr.data

import java.io.Serializable

data class Claim(
    val userID: String? = null,
    val title: String? = null,
    var claimType: String? = null,
    var status: String? = null,
    var dateApplied: String? = null,
    var desc: String? = null,
    var amount: Double? = null,
    var uploadedImg: String? = null,
) : Serializable

data class ClaimBalancesData(
    var food_balance: Double = 0.0,
    var medical_balance: Double = 0.0,
    var others_balance: Double = 0.0,
    var transportation_balance: Double = 0.0,
) : Serializable

data class ClaimTotalsData(
    var food_total: Double = 0.0,
    var medical_total: Double = 0.0,
    var others_total: Double = 0.0,
    var transportation_total: Double = 0.0,
) : Serializable

data class ClaimBalancesDataResponse(
    var balance: ClaimBalancesData? = null,
    var exception: String? = null
)

data class ClaimTotalsDataResponse(
    var total: ClaimTotalsData? = null,
    var exception: String? = null
)

