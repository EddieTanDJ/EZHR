package com.example.ezhr.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.LeaveStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagerPendingLeaveViewModel : ViewModel() {
    var leaveApplicationList = MutableLiveData<List<LeaveStatus>>()
    var newLeaveApplicationList = mutableListOf<LeaveStatus>()

    var idList = MutableLiveData<List<String>>()
    var newIdList = mutableListOf<String>()

    init {
        getPendingLeavesData()
    }

    private fun getPendingLeavesData() {
        val database = FirebaseDatabase.getInstance().getReference("leaves")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newLeaveApplicationList.clear()
                newIdList.clear()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child("leaveStatus")
                                .getValue(String::class.java) == "Pending"
                        ) {
                            val leave = userSnapshot.getValue(LeaveStatus::class.java)
                            val claimID = userSnapshot.key.toString()
                            newIdList.add(claimID)
                            newLeaveApplicationList.add(leave!!)
                        }
                    }
                }
                idList.value = newIdList
                leaveApplicationList.value = newLeaveApplicationList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

class ManagerPendingLeaveViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerPendingLeaveViewModel::class.java)) {
            return ManagerPendingLeaveViewModel() as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}