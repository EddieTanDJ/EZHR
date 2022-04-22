package com.example.ezhr.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.ezhr.data.Claim
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ApplyClaimRepository {
    private lateinit var database: DatabaseReference
    var userID = Firebase.auth.currentUser?.uid
    private val TAG = "ApplyClaimRepository"

    /**
     * Get file name based on provided Uri
     */
    @SuppressLint("Range")
    fun getFileName(uri: Uri, context: Context): MutableLiveData<String> {
        val mutableLiveData = MutableLiveData<String>()
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
        mutableLiveData.value = result!!
        return mutableLiveData
    }

    /**
     * Upload claim data object to firebase database
     */
    fun uploadClaimApplication(
        uploadedFileURI: Uri?,
        claim: Claim,
        uploadedFileName: String,
    ): MutableLiveData<Boolean> {
        val mutableLiveData = MutableLiveData<Boolean>()

        database = FirebaseDatabase.getInstance().getReference("claims")
        // Save claim data to firebase database
        var newDatabaseRef = database.push()
        // Get the current key in firebase database for the new claims
        val currentClaimID = newDatabaseRef.key.toString()

        if (uploadedFileURI != null) {
            uploadImageToFirebase(uploadedFileURI, currentClaimID, uploadedFileName)
        }

        newDatabaseRef.setValue(claim).addOnSuccessListener {
            mutableLiveData.value = true
        }.addOnFailureListener {
            mutableLiveData.value = false
        }
        return mutableLiveData
    }

    /**
     * Upload selected image to firebase storage
     */
    private fun uploadImageToFirebase(fileUri: Uri, claimID: String, uploadedFileName: String) {
        val refStorage =
            FirebaseStorage.getInstance().reference.child("claims/$claimID/$uploadedFileName")
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

    /**
     * Retrieve current claim balance based on claim type selected
     */
    fun getCurrentClaimBalance(claimType: String): MutableLiveData<Double> {
        val mutableLiveData = MutableLiveData<Double>()

        /**
         * Returns a new read-only map with the specified contents,
         * given as a list of pairs where the first value is the key and the second is the value.
         */
        val claimBalance = mapOf(
            "Food" to "food_balance",
            "Medical" to "medical_balance",
            "Others" to "others_balance",
            "Transportation" to "transportation_balance"
        )

        val databaseReference = FirebaseDatabase.getInstance().getReference("claim_balances")
        databaseReference.child(userID.toString()).child(claimBalance[claimType]!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mutableLiveData.value = dataSnapshot.value.toString().toDouble()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, databaseError.message)
                }
            })
        return mutableLiveData
    }
}
