package com.example.ezhr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ezhr.data.ClaimBalancesResponse
import com.example.ezhr.data.LeaveBalancesResponse
import com.example.ezhr.repository.BalanceRepository

class BalanceViewModel(private val balanceRepo: BalanceRepository) : ViewModel() {
    private val TAG = "BalanceViewModel"

    /**
     * Get the leave balance data
     * @return : LiveData<BalanceResponse>
     */
    fun fetchLeaveBalanceData(): LiveData<LeaveBalancesResponse> {
        return balanceRepo.fetchLeaveBalanceData()
    }

    /**
     * Get the claims balance data
     */
    fun fetchClaimsBalanceData(): LiveData<ClaimBalancesResponse> {
        return balanceRepo.fetchClaimBalanceData()
    }
}


// Boilerplate to create a factory class
class BalanceViewModelFactory(
    private val balanceRepo: BalanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BalanceViewModel::class.java)) {
            return BalanceViewModel(balanceRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
