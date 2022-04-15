package com.example.ezhr.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ezhr.data.Response
import com.example.ezhr.data.User
import com.example.ezhr.repository.AttendanceRepository.Companion.firebaseAuth
import com.example.ezhr.repository.AttendanceRepository.Companion.firebaseDatabase
import com.example.ezhr.repository.AttendanceRepository.Companion.uid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await


class AccountRepository(context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userReferences = firebaseDatabase.getReference("user")
    private var uid = firebaseAuth.currentUser?.uid

    init {
        _response.postValue(fetchUserData2())
    }

    /**
     * Reset password function
     * @param email address : String
     * @return boolean
     */
    suspend fun forgetPassword(email: String): Boolean {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get the user data based on uid
     * @return MutableLiveData<Response> the user data
     */
    fun fetchUserData(): MutableLiveData<Response> {
        val mutableLiveData = MutableLiveData<Response>()
        uid = currentUser()?.uid
        uid?.let { uid ->
            // Get the user from the database
            val response = Response()
            userReferences.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "$uid")
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Log.d(TAG, "$user")
                        response.user = user
                    } else {
                        response.exception = "User not found"
                    }
                    mutableLiveData.value = response
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    response.exception = "${databaseError.message}"
                }
            })
        }
        return mutableLiveData
    }

    fun logout() = firebaseAuth.signOut()

    private fun currentUser() = firebaseAuth.currentUser

    companion object {
        // Constant for naming our DataStore - you can change this if you want
        private const val PREFERENCES = "account_credentials"

        // The usual for debugging
        val TAG: String = "Account Repository"

        // Boilerplate-y code for singleton: the private reference to this self
        @Volatile
        private var INSTANCE: AccountRepository? = null
        private fun currentUser() = firebaseAuth.currentUser
        private val userReferences = firebaseDatabase.getReference("user")

        private val _response = MutableLiveData<Response>()
        val response: LiveData<Response> = _response

        /**
         * Boilerplate code for singleton: to ensure only a single copy is ever present
         * @param context to init the datastore
         */
        fun getInstance(context: Context): AccountRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = AccountRepository(context)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Get the user data based on uid
         * @return MutableLiveData<Response> the user data
         */
        fun fetchUserData2(): Response {
            val response = Response()
            uid = currentUser()?.uid
            uid?.let { uid ->
                // Get the user from the database
                userReferences.child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.d(TAG, "$uid")
                            val user = dataSnapshot.getValue(User::class.java)
                            if (user != null) {
                                Log.d(TAG, "$user")
                                response.user = user

                            } else {
                                response.exception = "User not found"
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            response.exception = "${databaseError.message}"
                        }
                    })
            }
            return response
        }
    }
}