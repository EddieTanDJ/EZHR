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

class ClaimEditRepository {
    private lateinit var database: DatabaseReference
    private val TAG = "ClaimEditRepository"
    var userID = Firebase.auth.currentUser?.uid

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
     * Upload selected image to firebase storage
     */
    private fun uploadImageToFirebase(
        fileUri: Uri,
        claimID: String,
        uploadedFileName: String,
        currentUploadedImg: String,
    ) {
        val refStorage =
            FirebaseStorage.getInstance().reference.child("claims/$claimID/$uploadedFileName")

        refStorage.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val imageUrl = it.toString()
                    deleteCurrentImage(claimID, currentUploadedImg)
                }
            }
            .addOnFailureListener { e ->
                print(e.message)
            }
    }

    /**
     * Delete current uploaded image on firebase storage
     */
    private fun deleteCurrentImage(claimID: String, currentUploadedImg: String) {
        val originalRefStorage =
            FirebaseStorage.getInstance().reference.child("claims/$claimID/$currentUploadedImg")
        originalRefStorage.delete().addOnSuccessListener {
            Log.d(TAG, "Current uploaded image deleted.")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to delete image.")
        }
    }

    /**
     * Update current claim data object in firebase database
     */
    fun updateClaim(
        claim: Claim,
        claimID: String,
        uploadedFileURI: Uri?,
        currentUploadedImg: String,
        uploadedFileName: String,
    ): MutableLiveData<Boolean> {
        val mutableLiveData = MutableLiveData<Boolean>()
        database = FirebaseDatabase.getInstance().getReference("claims")

        val newClaim: MutableMap<String, Any> = HashMap()
        newClaim["amount"] = claim.amount.toString().toDouble()
        newClaim["claimType"] = claim.claimType.toString()
        newClaim["dateApplied"] = claim.dateApplied.toString()
        newClaim["desc"] = claim.desc.toString()
        newClaim["title"] = claim.title.toString()
        newClaim["uploadedImg"] = claim.uploadedImg.toString()

        if (claim.uploadedImg == "") {
            deleteCurrentImage(claimID, currentUploadedImg)
        }
        if (uploadedFileURI != null) {
            uploadImageToFirebase(uploadedFileURI, claimID, uploadedFileName, currentUploadedImg)
        }
        database.child(claimID).updateChildren(newClaim).addOnSuccessListener {
            mutableLiveData.value = true
        }.addOnFailureListener {
            mutableLiveData.value = false
        }
        return mutableLiveData
    }

    /**
     * Retrieve current claim balance based on claim type selected
     */
    fun getCurrentClaimBalance(claimType: String): MutableLiveData<Double> {
        val mutableLiveData = MutableLiveData<Double>()
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