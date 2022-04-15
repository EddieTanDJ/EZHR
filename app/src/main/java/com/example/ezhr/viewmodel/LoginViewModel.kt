package com.example.ezhr.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.ezhr.data.Credentials
import com.example.ezhr.repository.LoginRepository
import com.example.ezhr.util.Resource
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepo: LoginRepository) : ViewModel() {
    private val TAG = "Login ViewModel"

    // Sign in result
    private val _signIn = MutableLiveData<Resource<AuthResult>>()
    val signInStatus: LiveData<Resource<AuthResult>> = _signIn

    // Get login credentials
    private val _credentialsData = MutableLiveData<Credentials>()
    val credentialsData: LiveData<Credentials> = _credentialsData

    // Status of authentication
    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    // Status of change password
    private val _changePasswordStatus = MutableLiveData<Boolean>()
    val changePasswordStatus: LiveData<Boolean> = _changePasswordStatus


    // Spinner status
    private val _progress = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = _progress

    /**
     * Login the user
     * @param  : String  : String, user email and user password
     */
    fun signInUser(userEmailAddress: String, userLoginPassword: String) {
        if (userEmailAddress.isEmpty() || userLoginPassword.isEmpty()) {
            _signIn.postValue(Resource.Error("Empty Strings"))

        } else {
            _signIn.postValue(Resource.Loading())
            viewModelScope.launch(Dispatchers.Main) {
                val loginResult = loginRepo.login(userEmailAddress, userLoginPassword)
                _signIn.postValue(loginResult)
            }
        }
    }


    /**
     * Get the credentials that is in the data store
     */
    fun getCredentials() = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Getting  data from data store")
        loginRepo.credentialsFlow.collect {
            _credentialsData.postValue(it)
        }
    }

    /**
     * Update the credentials
     * @param email : String , password: String
     */
    fun updateCredentials(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Update credentials: $email , $password")
        loginRepo.editCredentials(email, password)
    }

    /**
     * Clear the preferences datastore
     */
    fun clearData() = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "Clear credentials from preference data store")
        loginRepo.clearData()
    }

    /**
     * Re-authenticate user before changing password.
     */
    fun authentication(currentPassword: String) = viewModelScope.launch(Dispatchers.IO) {
        _progress.postValue(true)
        Log.d(TAG, "Current Password: $currentPassword")
        val result = loginRepo.authentication(currentPassword)
        Log.d(TAG, "Authentication Result: $result")
        _isAuthenticated.postValue(result)
        _progress.postValue(false)
    }


    /**
     * Change user password
     */
    fun changePassword(newPassword: String) = viewModelScope.launch(Dispatchers.IO) {
        _progress.postValue(true)
        Log.d(TAG, "New Password: $newPassword")
        val result = loginRepo.changePassword(newPassword)
        Log.d(TAG, "Password Result: $result")
        _changePasswordStatus.postValue(result)
        _progress.postValue(false)
    }

}


/**
 * A factory to create the ViewModel properly.
 * Very boilerplate code...
 * NOTE This is due to the fact that we have ctor params.
 *   ViewModelProviders manage the lifecycle of VMs and we cannot create VMs by ourselves.
 *   So we need to provide a Factory to ViewModelProviders so that it knows how to create for us
 *   whenever we need an instance of it.
 */
class LoginViewModelFactory(
    private val loginRepo: LoginRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}