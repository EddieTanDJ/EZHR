package com.example.ezhr.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.Claim
import com.example.ezhr.repository.ApplyClaimRepository

class ApplyClaimViewModel(private val applyClaimRepo: ApplyClaimRepository) : ViewModel() {
    fun getFileName(uri: Uri, context: Context): LiveData<String> {
        return applyClaimRepo.getFileName(uri, context)
    }

    fun uploadClaimApplication(
        uploadedFileURI: Uri?,
        claim: Claim,
        uploadedFileName: String,
    ): LiveData<Boolean> {
        return applyClaimRepo.uploadClaimApplication(uploadedFileURI, claim, uploadedFileName)
    }

    fun getCurrentClaimBalance(claimType: String): LiveData<Double> {
        return applyClaimRepo.getCurrentClaimBalance(claimType)
    }
}

// Boilerplate to create a factory class
class ApplyClaimViewModelFactory(
    private val applyClaimRepo: ApplyClaimRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApplyClaimViewModel::class.java)) {
            return ApplyClaimViewModel(applyClaimRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}