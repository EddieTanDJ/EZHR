package com.example.ezhr.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.ezhr.data.Response
import com.example.ezhr.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(private val accountRepo: AccountRepository) : ViewModel() {
    private val TAG = "AccountViewModel"

    // Get password data
    private val _password = MutableLiveData<Boolean>()
    val passwordStatus: LiveData<Boolean> = _password

    // Spinner status
    private val _progress = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = _progress

    /**
     * Forget Password
     * @param  : String   user email
     */
    fun forgetPassword(email: String) = viewModelScope.launch(Dispatchers.IO) {
        _progress.postValue(true)
        Log.d(TAG, "Email: $email")
        val result = accountRepo.forgetPassword(email)
        Log.d(TAG, "Result: $result")
        _password.postValue(result)
        _progress.postValue(false)
    }

    /**
     *  Fetch user data
     */
    fun fetchUserData(): LiveData<Response> {
        return accountRepo.fetchUserData()
    }

    /***
     * Logout
     */
    fun logout() {
        accountRepo.logout()
    }
}

// Boilerplate to create a factory class
class AccountViewModelFactory(
    private val accountRepo: AccountRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(accountRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}