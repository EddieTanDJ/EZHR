package com.example.ezhr.repository

import androidx.lifecycle.MutableLiveData
import com.example.ezhr.data.ClaimBalancesData
import com.example.ezhr.data.ClaimBalancesDataResponse
import com.example.ezhr.data.ClaimTotalsData
import com.example.ezhr.data.ClaimTotalsDataResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class ClaimBalanceRepository {
    private lateinit var databaseReference: DatabaseReference
    val userID = Firebase.auth.currentUser?.uid

    /**
     * Retrieve claim balances from firebase database
     */
    fun fetchClaimBalances(): MutableLiveData<ClaimBalancesDataResponse> {
        val mutableLiveData = MutableLiveData<ClaimBalancesDataResponse>()
        val response = ClaimBalancesDataResponse()

        databaseReference = FirebaseDatabase.getInstance().getReference("claim_balances")
        databaseReference.child(userID.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val balance = snapshot.getValue(ClaimBalancesData::class.java)

                    if (balance != null) {
                        response.balance = balance
                    } else {
                        response.exception = "Claim balances not found"
                    }
                    mutableLiveData.value = response
                }

                override fun onCancelled(error: DatabaseError) {
                    response.exception = error.message
                    mutableLiveData.value = response
                }
            })

        return mutableLiveData
    }

    /**
     * Retrieve user's claim totals from firebase database
     */
    fun fetchClaimTotals(): MutableLiveData<ClaimTotalsDataResponse> {
        val mutableLiveData = MutableLiveData<ClaimTotalsDataResponse>()
        val response = ClaimTotalsDataResponse()

        databaseReference = FirebaseDatabase.getInstance().getReference("claim_totals")
        databaseReference.child("0")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totals = snapshot.getValue(ClaimTotalsData::class.java)

                    if (totals != null) {
                        response.total = totals
                    } else {
                        response.exception = "Claim totals not found"
                    }
                    mutableLiveData.value = response
                }

                override fun onCancelled(error: DatabaseError) {
                    response.exception = error.message
                    mutableLiveData.value = response
                }
            })

        return mutableLiveData
    }
}