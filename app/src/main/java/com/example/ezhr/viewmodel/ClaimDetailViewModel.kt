package com.example.ezhr.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.ezhr.data.Claim
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClaimDetailViewModel : ViewModel() {
    private val _claimID = MutableLiveData<String>()
    val claimID: LiveData<String> = _claimID

    private val _uploadImage = MutableLiveData<String>()
    val uploadImage: LiveData<String> = _uploadImage

    private val _claims = MutableLiveData<Claim>()
    val claims: LiveData<Claim> = _claims

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    private val TAG = "ClaimDetailViewModel"

    /**
     * Set the value of the claimID property
     */
    fun setClaimID(claimID: String) = viewModelScope.launch(Dispatchers.IO) {
        _claimID.postValue(claimID)
    }

    /**
     * Set the value of the uploadImage property
     */
    fun setUploadImage(uploadImage: String) = viewModelScope.launch(Dispatchers.IO) {
        _uploadImage.postValue(uploadImage)
    }

    /**
     * Set the value of the claims property
     */
    fun setClaims(claims: Claim) = viewModelScope.launch(Dispatchers.IO) {
        _claims.postValue(claims)
    }

    /**
     * Delete claim application from firebase database
     */
    fun deleteClaim(claimID: String, uploadedImg: String) {
        var database = FirebaseDatabase.getInstance().getReference("claims")
        database.child(claimID).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val refStorage =
                        FirebaseStorage.getInstance().reference.child("claims/$claimID/$uploadedImg")
                    refStorage.delete().addOnSuccessListener {
                        Log.d(TAG, "Image deleted.")
                    }.addOnFailureListener {
                        Log.d(TAG, "Failed to delete image.")
                    }
                    _deleteSuccess.postValue(true)
                } else {
                    _deleteSuccess.postValue(false)
                }
            }
    }
}

// Boilerplate to create a factory class
class ClaimDetailViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClaimDetailViewModel::class.java)) {
            return ClaimDetailViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}