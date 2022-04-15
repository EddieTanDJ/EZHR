package com.example.ezhr.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.Claim
import com.example.ezhr.repository.ClaimEditRepository

class ClaimEditViewModel(private val claimEditRepo: ClaimEditRepository) : ViewModel() {
    fun getFileName(uri: Uri, context: Context): LiveData<String> {
        return claimEditRepo.getFileName(uri, context)
    }

    fun updateClaim(
        claim: Claim,
        claimID: String,
        uploadedFileURI: Uri?,
        currentUploadedImg: String,
        uploadedFileName: String,
    ): LiveData<Boolean> {
        return claimEditRepo.updateClaim(
            claim,
            claimID,
            uploadedFileURI,
            currentUploadedImg,
            uploadedFileName
        )
    }

    fun getCurrentClaimBalance(claimType: String): LiveData<Double> {
        return claimEditRepo.getCurrentClaimBalance(claimType)
    }
}

// Boilerplate to create a factory class
class ClaimEditViewModelFactory(
    private val claimEditRepo: ClaimEditRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClaimEditViewModel::class.java)) {
            return ClaimEditViewModel(claimEditRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}