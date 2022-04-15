package com.example.ezhr

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("attendancePreferences")

class UserAttendanceManager(val context: Context) {

    suspend fun storeAttendanceStatus(attendanceStatus: Boolean) {
        context.dataStore.edit {
            Log.d(
                TAG,
                "storeAttendanceStatus: AttendanceManager ran. Current attendance status: $attendanceStatus"
            )
            it[ATTENDANCE_STATUS] = attendanceStatus
        }
    }

    // Create a status flow to retrieve status from the preferences
    // flow comes from the kotlin coroutine
    val attendanceStatusFlow: Flow<Boolean> = context.dataStore.data.map {
        it[ATTENDANCE_STATUS] ?: false
    }

    /**
     * To clear all the data in the preferences data store
     */
    suspend fun clearData() {
        context.dataStore.edit {
            Log.d(TAG, "clearData called to remove attendance status")
            it.remove(ATTENDANCE_STATUS)
        }
    }

    companion object {
        const val TAG = "UserAttendanceManager"
        val ATTENDANCE_STATUS = booleanPreferencesKey("ATTENDANCE_STATUS")
    }

}
