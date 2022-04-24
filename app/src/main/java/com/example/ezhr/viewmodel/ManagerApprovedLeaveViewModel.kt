package com.example.ezhr.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.LeaveStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ManagerApprovedLeaveViewModel : ViewModel() {
    var leaveApplicationList = MutableLiveData<List<LeaveStatus>>()
    var newLeaveApplicationList = mutableListOf<LeaveStatus>()

    var idList = MutableLiveData<List<String>>()
    var newIdList = mutableListOf<String>()

    var fileNameList = MutableLiveData<List<String>>()
    var newFileNameList = mutableListOf<String>()

    init {
        getApprovedLeavesData()
    }

    private fun getApprovedLeavesData() {
        val database = FirebaseDatabase.getInstance().getReference("leaves")
        val firebaseStorageRef = FirebaseStorage.getInstance().reference

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newLeaveApplicationList.clear()
                newIdList.clear()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child("leaveStatus")
                                .getValue(String::class.java) == "Approved"
                        ) {
                            val leave = userSnapshot.getValue(LeaveStatus::class.java)
                            val leaveID = userSnapshot.key.toString()
                            newIdList.add(leaveID)
                            val fileName = userSnapshot.child("uploadedImg").value
                            newFileNameList.add(fileName.toString())
                            if (leave != null) {
                                firebaseStorageRef.child("leaves/$leaveID/$fileName").downloadUrl.addOnSuccessListener {
                                    leave.uploadedImg = it.toString()
                                }
                            }
                            newLeaveApplicationList.add(leave!!)
                        }
                    }
                }
                idList.value = newIdList
                fileNameList.value = newFileNameList
                leaveApplicationList.value = newLeaveApplicationList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

class ManagerApprovedLeaveViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerApprovedLeaveViewModel::class.java)) {
            return ManagerApprovedLeaveViewModel() as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}

