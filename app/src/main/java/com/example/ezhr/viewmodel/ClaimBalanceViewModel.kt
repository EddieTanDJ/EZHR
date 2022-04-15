package com.example.ezhr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.ClaimBalancesDataResponse
import com.example.ezhr.data.ClaimTotalsDataResponse
import com.example.ezhr.repository.ClaimBalanceRepository

class ClaimBalanceViewModel(private val claimBalanceRepo: ClaimBalanceRepository) : ViewModel() {
    /**
     * Get the claim totals data
     */
    fun fetchClaimBalances(): LiveData<ClaimBalancesDataResponse> {
        return claimBalanceRepo.fetchClaimBalances()
    }

    /**
     * Get the claim balances data
     */
    fun fetchClaimTotals(): LiveData<ClaimTotalsDataResponse> {
        return claimBalanceRepo.fetchClaimTotals()
    }
}

class ClaimBalanceViewModelFactory(
    private val claimBalanceRepo: ClaimBalanceRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClaimBalanceViewModel::class.java)) {
            return ClaimBalanceViewModel(claimBalanceRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}