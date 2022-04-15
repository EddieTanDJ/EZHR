package com.example.ezhr.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.ezhr.data.ClaimBalances
import com.example.ezhr.data.ClaimBalancesResponse
import com.example.ezhr.data.LeaveBalances
import com.example.ezhr.data.LeaveBalancesResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BalanceRepository {
    private val TAG: String = "BalanceRepository"
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val leaveBalanceReferences = firebaseDatabase.getReference("leave_balances")
    private val claimsBalanceReferences = firebaseDatabase.getReference("claim_balances")

    /**
     * Read balance for leaves
     * @return MutableLiveData<BalanceResponse> the balamnce data
     */
    fun fetchLeaveBalanceData(): MutableLiveData<LeaveBalancesResponse> {
        val mutableLiveData = MutableLiveData<LeaveBalancesResponse>()
        val user = firebaseAuth.currentUser
        val uid = user?.uid
        uid?.let { uid ->
            // Get the user from the database
            val response = LeaveBalancesResponse()
            leaveBalanceReferences.child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "$uid")
                    val balance = dataSnapshot.getValue(LeaveBalances::class.java)
                    Log.d(TAG, "Leave Balance: $balance")
                    if (balance != null) {
                        Log.d(TAG, "Leave Balance: $balance")
                        response.balance = balance
                    } else {
                        response.exception = "Balance not found"
                    }
                    mutableLiveData.value = response
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    response.exception = "${databaseError.message}"
                    mutableLiveData.value = response
                }
            })
        }
        return mutableLiveData
    }

    /**
     * Read balance for claims
     * @return MutableLiveData<BalanceResponse> the balamnce data
     */
    fun fetchClaimBalanceData(): MutableLiveData<ClaimBalancesResponse> {
        val mutableLiveData = MutableLiveData<ClaimBalancesResponse>()
        val user = firebaseAuth.currentUser
        val uid = user?.uid
        uid?.let { uid ->
            // Get the user from the database
            val response = ClaimBalancesResponse()
            claimsBalanceReferences.child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "$uid")
                    val balance = dataSnapshot.getValue(ClaimBalances::class.java)
                    Log.d(TAG, "Claims Balance: $balance")
                    if (balance != null) {
                        Log.d(TAG, "Claims Balance: $balance")
                        response.balance = balance
                    } else {
                        response.exception = "Balance not found"
                    }
                    mutableLiveData.value = response
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    response.exception = "${databaseError.message}"
                    mutableLiveData.value = response
                }
            })
        }
        return mutableLiveData
    }
}