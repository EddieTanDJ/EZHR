package com.example.ezhr.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ezhr.data.Credentials
import com.example.ezhr.util.Resource
import com.example.ezhr.util.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Class that handles saving and retrieving user preferences, utilizing Preferences DataStore. This
 * class may be utilized in either the ViewModel or an Activity, depending on what preferences are
 * being saved.
 */
class LoginRepository(context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "PREFERENCES")
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
    }

    private var text = ""
    private var user = currentUser()
    private var uid = firebaseAuth.currentUser?.uid


    /**
     * Retrieve the user credentials from the DataStore.
     */
    @SuppressLint("LongLogTag")
    val credentialsFlow: Flow<Credentials> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading user preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // By default is null
            val email = preferences[PreferencesKeys.EMAIL]
            val password = preferences[PreferencesKeys.PASSWORD]
            Log.d(TAG, "Email Retrieved: $email")
            Log.d(TAG, "Password Retrieved: $password")
            Credentials(email, password)
        }

    /**
     * Save the latest user credentials when user login via password
     */
    suspend fun editCredentials(email: String, password: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.PASSWORD] = password
        }
    }


    /**
     * To clear all the data in the preferences data store
     */
    suspend fun clearData() {
        dataStore.edit {
            it.clear()
        }
    }

    /**
     * login function
     * @param email address : String
     * @return boolean
     */
    suspend fun login(email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                Resource.Success(result)
            }
        }
    }

    private fun currentUser() = firebaseAuth.currentUser


    /**
     * Check Current User in the database using coroutine
     */
    suspend fun authentication(currentPassword: String): Boolean {
        try {
            val user = currentUser()
            Log.d(TAG, "Current User: Email: ${user?.email}, Current Password: $currentPassword")
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                Log.d(TAG, "Credentials: $credential")
                // Re-Authenticate user when user want to change the password
                user.reauthenticate(credential).await()
                Log.d(TAG, "Re-authenticate successfully")
                return true
            }
            return false
        } catch (e: Exception) {
            Log.d(TAG, "Error Message: ${e.message}")
            return false
        }
    }

    /**
     * Change password
     */
    suspend fun changePassword(newPassword: String): Boolean {
        try {
            val user = currentUser()
            Log.d(TAG, "Current User Email: ${user?.email} , new Password: $newPassword")
            if (user != null && user.email != null) {
                user.updatePassword(newPassword).await()
                Log.d(TAG, "Password change successful")
                return true
            }
            return false
        } catch (e: Exception) {
            Log.d(TAG, "Unable to change the password")
            return false
        }
    }


    companion object {
        // Constant for naming our DataStore - you can change this if you want
        private const val PREFERENCES = "account_credentials"

        // The usual for debugging
        private val TAG: String = "LoginRepository"

        // Boilerplate-y code for singleton: the private reference to this self
        @Volatile
        private var INSTANCE: LoginRepository? = null

        /**
         * Boilerplate code for singleton: to ensure only a single copy is ever present
         * @param context to init the datastore
         */
        fun getInstance(context: Context): LoginRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = LoginRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
