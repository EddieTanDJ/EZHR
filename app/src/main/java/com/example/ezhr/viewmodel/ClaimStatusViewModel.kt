package com.example.ezhr.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.Claim
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ClaimStatusViewModel : ViewModel() {
    var claimsList = MutableLiveData<List<Claim>>()
    var newClaimsList = mutableListOf<Claim>()

    var idList = MutableLiveData<List<String>>()
    var newIdList = mutableListOf<String>()

    var fileNameList = MutableLiveData<List<String>>()
    var newFileNameList = mutableListOf<String>()

    private lateinit var database: DatabaseReference
    val userID = Firebase.auth.currentUser?.uid

    init {
        loadClaims()
    }

    /**
     * Retrieve and display current user's recent claim applications from firebase database
     */
    private fun loadClaims() {
        database = FirebaseDatabase.getInstance().getReference("claims")
        val firebaseStorageRef = FirebaseStorage.getInstance().reference

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newClaimsList.clear()
                newFileNameList.clear()
                newIdList.clear()

                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child("userID").getValue(String::class.java) == userID) {
                            val claim = userSnapshot.getValue(Claim::class.java)
                            val claimID = userSnapshot.key.toString()
                            newIdList.add(claimID)

                            val fileName = userSnapshot.child("uploadedImg").value
                            newFileNameList.add(fileName.toString())

                            if (claim != null) {
                                firebaseStorageRef.child("claims/$claimID/$fileName").downloadUrl.addOnSuccessListener {
                                    claim.uploadedImg = it.toString()
                                }
                            }
                            newClaimsList.add(claim!!)
                        }
                    }
                    idList.value = newIdList
                    fileNameList.value = newFileNameList
                    claimsList.value = newClaimsList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

class ClaimStatusViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClaimStatusViewModel::class.java)) {
            return ClaimStatusViewModel() as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}