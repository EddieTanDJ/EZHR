package com.example.ezhr.data


import java.io.Serializable


data class LeaveBalances(
    var annual_balance: Double = 0.0,
    var annual_used: Double = 0.0,
    var compassionate_balance: Double = 0.0,
    var maternity_balance: Double = 0.0,
    var medical_balance: Double = 0.0,
) : Serializable

data class ClaimBalances(
    var food_balance: Double = 0.0,
    var medical_balance: Double = 0.0,
    var medical_used: Double = 0.0,
    var other_balance: Double = 0.0,
    var transportation_balance: Double = 0.0
) : Serializable


data class LeaveBalancesResponse(
    var balance: LeaveBalances? = null,
    var exception: String? = null
)

data class ClaimBalancesResponse(
    var balance: ClaimBalances? = null,
    var exception: String? = null
)

