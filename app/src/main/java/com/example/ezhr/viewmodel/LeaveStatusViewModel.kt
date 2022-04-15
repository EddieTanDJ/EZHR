package com.example.ezhr.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ezhr.data.LeaveStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class LeaveStatusViewModel : ViewModel() {
    var leaveList = MutableLiveData<List<LeaveStatus>>()
    var newLeavesList = mutableListOf<LeaveStatus>()

    var idList = MutableLiveData<List<String>>()
    var newIdList = mutableListOf<String>()

    var fileNameList = MutableLiveData<List<String>>()
    var newFileNameList = mutableListOf<String>()

    private lateinit var database: DatabaseReference
    val userID = Firebase.auth.currentUser?.uid

    init {
        loadLeaves()
    }

    /**
     * Retrieve and display current user's recent claim applications from firebase database
     */
    private fun loadLeaves() {
        database = FirebaseDatabase.getInstance().getReference("Leaves")
        val firebaseStorageRef = FirebaseStorage.getInstance().reference

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newLeavesList.clear()
                newFileNameList.clear()
                newIdList.clear()

                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child("userID").getValue(String::class.java) == userID) {
                            val leave = userSnapshot.getValue(LeaveStatus::class.java)
                            val leaveID = userSnapshot.key.toString()
                            newIdList.add(leaveID)

                            val fileName = userSnapshot.child("uploadedImg").value
                            newFileNameList.add(fileName.toString())

                            if (leave != null) {
                                firebaseStorageRef.child("Leaves/$leaveID/$fileName").downloadUrl.addOnSuccessListener {
                                    leave.uploadedImg = it.toString()
                                }
                            }
                            newLeavesList.add(leave!!)
                        }
                    }
                    idList.value = newIdList
                    fileNameList.value = newFileNameList
                    leaveList.value = newLeavesList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}