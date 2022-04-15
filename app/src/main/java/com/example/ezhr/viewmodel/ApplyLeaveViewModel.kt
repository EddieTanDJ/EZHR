package com.example.ezhr.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.LeaveStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ApplyLeaveViewModel : ViewModel() {
    private lateinit var database: DatabaseReference

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _currentLeaveBalance = MutableLiveData<Int>()
    val currentLeaveBalance: LiveData<Int> = _currentLeaveBalance

    private val _fileName = MutableLiveData<String>()
    val fileName: LiveData<String> = _fileName

    private val TAG = "ApplyLeaveViewModel"
    var userID = Firebase.auth.currentUser?.uid

    /**
     * Upload selected image to firebase storage
     */
    private fun uploadImageToFirebase(fileUri: Uri, leaveID: String, uploadedFileName: String) {
        val refStorage =
            FirebaseStorage.getInstance().reference.child("leaves/$leaveID/$uploadedFileName")
        refStorage.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val imageUrl = it.toString()
                }
            }
            .addOnFailureListener { e ->
                print(e.message)
            }
    }

    fun uploadLeaveApplication(
        uploadedFileURI: Uri?,
        leaveStatus: LeaveStatus,
        uploadedFileName: String
    ) {
        database = FirebaseDatabase.getInstance().getReference("Leaves")
        var newDatabaseRef = database.push()

        val currentLeaveID = newDatabaseRef.key.toString()

        if (uploadedFileURI != null) {
            uploadImageToFirebase(uploadedFileURI, currentLeaveID, uploadedFileName)
        }

        newDatabaseRef.setValue(leaveStatus).addOnSuccessListener {
            _uploadSuccess.postValue(true)
        }.addOnFailureListener {
            _uploadSuccess.postValue(false)
        }
    }

    fun getCurrentLeaveBalance(leaveType: String) {
        val leaveBalance = mapOf(
            "Annual" to "annual_balance",
            "Medical" to "medical_balance",
            "Maternity" to "maternity_balance",
            "Compassionate" to "compassionate_balance"
        )

        val databaseReference = FirebaseDatabase.getInstance().getReference("leave_balances")
        databaseReference.child(userID.toString()).child(leaveBalance[leaveType]!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _currentLeaveBalance.postValue(dataSnapshot.value.toString().toInt())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, databaseError.message)
                }
            })
    }

    /**
     * Get file name based on provided Uri
     */
    @SuppressLint("Range")
    fun getFileName(uri: Uri, context: Context) {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        var result: String? = null
        if (uri.scheme == "content") {
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        _fileName.postValue(result!!)
    }
}

class ApplyLeaveViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApplyLeaveViewModel::class.java)) {
            return ApplyLeaveViewModel() as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}