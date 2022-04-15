package com.example.ezhr.repository

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ezhr.data.Attendance
import com.example.ezhr.data.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * The repo layer that manages potentially multiple sources of data.
 * - recommended best practice for SWA
 * - most common use case is to handle an online + offline (cached) DB
 * - only need to pass in the DAO and not the Room DB which is encapsulated away
 */
class AttendanceRepository(context: Context) {

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val attendanceDBReferences = firebaseDatabase.getReference("attendance")

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var uid = firebaseAuth.currentUser?.uid

    // Simple date format for time of check-in/check-out
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("dd/M/yyyy")

    //    private val stf = SimpleDateFormat("HH:mm")
    private val currentDate = sdf.format(Date()).toString()

    private var checkInKey = ""
    var checkInTime = MutableLiveData<String>()
    var checkOutTime = MutableLiveData<String>()

    /**
     * TODO: Write function description
     */
    fun getCheckInStatus(): MutableLiveData<String> {

        attendanceDBReferences.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    Log.d("firebase", "Getting attendance data check-in status")

                    if (userSnapshot.child("userID")
                            .getValue(String::class.java) == uid &&
                        userSnapshot.child("date").getValue(String::class.java) == currentDate
                        && userSnapshot.child("checkInTime")
                            .getValue(String::class.java) != "-"
                    ) {
                        checkInTime.value =
                            userSnapshot.child("checkInTime").getValue(String::class.java)!!
                        Log.d("firebase", "Success in retrieving check-out status")

                    } else
                        checkInTime.value = "Not Checked In"
                    Log.d("firebase", "User has not checked-in")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "Error getting attendance data. DatabaseError")
            }
        })
        return checkInTime
    }

    /**
     * TODO: Write function description
     */
    fun getCheckOutStatus(): MutableLiveData<String> {

        attendanceDBReferences.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        Log.d("firebase", "Getting attendance data check-out status")

                        if (userSnapshot.child("userID")
                                .getValue(String::class.java) == uid &&
                            userSnapshot.child("date").getValue(String::class.java) == currentDate
                            && userSnapshot.child("checkOutTime")
                                .getValue(String::class.java) != "-"
                        ) {
                            checkOutTime.value =
                                userSnapshot.child("checkOutTime").getValue(String::class.java)!!
                            Log.d("firebase", "Success in retrieving check-out status")

                        } else
                            checkOutTime.value = "Not Checked Out"
                        Log.d("firebase", "User has not checked-out")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "Error getting attendance data. DatabaseError")
            }
        })
        return checkOutTime
    }

    /**
     * TODO: Write function description
     */
    fun uploadAttendanceCheckIn(attendance: Attendance) {

        // Write a message to the database
        val databaseAction = attendanceDBReferences.push()
        // Read from the database
        databaseAction.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                checkInKey = dataSnapshot.key.toString()
                Log.d(TAG, "Ref is: $checkInKey")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read key.", error.toException())
            }
        })

        // Success and failure listeners for DB Attendance Inserts
        databaseAction.setValue(attendance).addOnSuccessListener {
            Log.d(TAG, "Upload Attendance check-in SUCCESS")
        }.addOnFailureListener {
            Log.w(TAG, "Upload Attendance check-in FAILED")
        }
    }

    /**
     * TODO: Write function description
     */
    fun uploadAttendanceCheckOut(checkOutTime: String) {
        // Write a message to the database

        val databaseAction = attendanceDBReferences.child(checkInKey).child("checkOutTime")

        // Success and failure listeners for DB Updates
        databaseAction.setValue(checkOutTime).addOnSuccessListener {
            Log.d(TAG, "Upload Attendance check-out SUCCESS")
        }.addOnFailureListener {
            Log.w(TAG, "Upload Attendance check-out FAILED")
        }
    }

    companion object {
        // Boilerplate-y code for singleton: the private reference to this self
        @Volatile
        private var INSTANCE: AttendanceRepository? = null
        val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        private val attendanceDBReferences = firebaseDatabase.getReference("attendance")

        var uid = firebaseAuth.currentUser?.uid
        private var checkInKey = ""

        @SuppressLint("SimpleDateFormat")
        private val sdf = SimpleDateFormat("dd/M/yyyy")
        private val currentDate = sdf.format(Date()).toString()

        /**
         * Boilerplate code for singleton: to ensure only a single copy is ever present
         * @param context to init the datastore
         */
        fun getInstance(context: Context): AttendanceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }
                val instance = AttendanceRepository(context)
                INSTANCE = instance
                instance
            }
        }


        /**
         * Get the user data based on uid
         * @return val newlist arrayListOf<Attendance> An arraylist of Attendance objects
         */
        fun getUserOwnAttendance(): MutableLiveData<ArrayList<Attendance>> {

            val attendanceDatesList = MutableLiveData<ArrayList<Attendance>>()
            val newlist = arrayListOf<Attendance>()

            attendanceDBReferences.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {

                            if (userSnapshot.child("userID")
                                    .getValue(String::class.java) == uid
                            ) {
                                Log.d("firebase", "Getting attendance data")

                                val attendance = userSnapshot.getValue(Attendance::class.java)
                                Log.i("firebase", "Got value $attendance")
                                newlist.add(attendance!!)

                                attendanceDatesList.value = newlist
                                Log.d("firebase", "Success in retrieving attendance data")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "Error getting attendance data. DatabaseError")
                }
            })
            return attendanceDatesList
        }


        /**
         * Get the user data based on uid
         * @return val newlist arrayListOf<Attendance> An arraylist of Attendance objects
         */
        fun getUserAttendance(): MutableLiveData<ArrayList<Attendance>> {

            val attendanceDatesList = MutableLiveData<ArrayList<Attendance>>()
            val newlist = arrayListOf<Attendance>()

            attendanceDBReferences.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            Log.d("firebase", "Getting attendance data")

                            val attendance = userSnapshot.getValue(Attendance::class.java)
                            Log.i("firebase", "Got value $attendance")
                            newlist.add(attendance!!)

                            attendanceDatesList.value = newlist
                            Log.d("firebase", "Success in retrieving attendance data")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "Error getting attendance data. DatabaseError")
                }
            })
            return attendanceDatesList
        }

        /**
         * TODO: Write function description
         */
        fun uploadAttendanceAbsent() {

            // Write a message to the database
            val databaseAction = attendanceDBReferences.push()
            // Read from the database
            databaseAction.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    checkInKey = dataSnapshot.key.toString()
                    Log.d(TAG, "Ref is: $checkInKey")
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read key.", error.toException())
                }
            })

            val employeeName = fetchUserAccountDetails().value?.user?.EmployeeName
            Log.d(TAG, "Fetched employeeName from user table: $employeeName")

            val employeeEmail = fetchUserAccountDetails().value?.user?.EmployeeEmail
            Log.d(TAG, "Fetched employeeEmail from user table: $employeeEmail")

            val attendance = Attendance(
                uid,
                currentDate,
                "-",
                "-",
                "Absent",
                "$employeeName",
                "$employeeEmail"
            )

            // Success and failure listeners for DB Attendance Inserts
            databaseAction.setValue(attendance).addOnSuccessListener {
                Log.d(TAG, "Upload Attendance Absent status SUCCESS")
            }.addOnFailureListener {
                Log.w(TAG, "Upload Attendance Absent status FAILURE")
            }
        }

        //TODO: Add in function description
        private fun fetchUserAccountDetails(): LiveData<Response> {
            return AccountRepository.response
        }
    }
}