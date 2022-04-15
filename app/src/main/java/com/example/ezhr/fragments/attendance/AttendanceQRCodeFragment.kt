package com.example.ezhr.fragments.attendance

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.ezhr.BottomActivity
import com.example.ezhr.DailyAttendanceWorker
import com.example.ezhr.EZHRApp
import com.example.ezhr.UserAttendanceManager
import com.example.ezhr.data.Attendance
import com.example.ezhr.databinding.FragmentAttendanceQrCodeBinding
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory
import com.example.ezhr.viewmodel.AttendanceQrcodeViewModel
import com.example.ezhr.viewmodel.AttendanceQrcodeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [AttendanceQRCodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AttendanceQRCodeFragment : Fragment() {
    private var _binding: FragmentAttendanceQrCodeBinding? = null
    private val binding get() = _binding!!

    // NavController variable
    private lateinit var navController: NavController

    private val viewModel: AttendanceQrcodeViewModel by activityViewModels {
        val app = requireActivity().application as EZHRApp
        AttendanceQrcodeViewModelFactory(app.attendanceRepo)
    }

    private val viewModelUser: AccountViewModel by activityViewModels {
        val application = requireActivity().application as EZHRApp
        val app = application
        AccountViewModelFactory(app.accountRepo)
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private fun currentUser() = firebaseAuth.currentUser?.uid

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("dd/M/yyyy")

    @SuppressLint("SimpleDateFormat")
    private val stf = SimpleDateFormat("h:mm a")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAttendanceQrCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "AttendanceQRFragment Created")
        // Get data from datastore preference
        navController = Navigation.findNavController(view)
        val attendanceManager = UserAttendanceManager(requireContext())
        attendanceManager.attendanceStatusFlow.asLiveData().observe(viewLifecycleOwner) {

            // QR Code image button function
            binding.qrImageButton.setOnClickListener {
                lauchQrCodeScanner(activity as BottomActivity)
            }
        }

        viewModel.getCheckInStatus().observe(viewLifecycleOwner) {
            binding.checkInStatusText.text = it.toString()
        }

        viewModel.getCheckOutStatus().observe(viewLifecycleOwner) {
            binding.checkOutStatusText.text = it.toString()
        }

        // Runs periodic workmanager to clear attendance Datastore at 12am everyday
        // Attendance DataStore contains preferences to check if the user has checked-in
        // Runs a check if the user has checked-in for work.
        // If the user did not, an absent status will be recorded
        attendanceWorkerRunner()
    }

    // Launches the QR Code scanner
    private fun lauchQrCodeScanner(view: BottomActivity) {

        //QR Code scanner options:
        val options = ScanOptions()
        options.setOrientationLocked(false)
        options.setBeepEnabled(false)
        barcodeLauncher.launch(options)
    }

    // QR Code function. Registers the launcher and result handler
    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(context, "No QR code detected", Toast.LENGTH_LONG).show()

            } else if (result.contents == "AttendanceKey") {

                val attendanceManager = UserAttendanceManager(requireContext())
                val attendanceTaken: Boolean
                // Check if the user has already taken attendance
                runBlocking(Dispatchers.IO)
                {
                    attendanceTaken = attendanceManager.attendanceStatusFlow.first()
                }

                val attendanceTime = stf.format(Date()).toString()

                if (attendanceTaken) {
                    viewModel.uploadAttendanceCheckOut(
                        attendanceTime
                    )
                    // Check out Successful
                    Toast.makeText(
                        context,
                        "Check-Out Successful @ $attendanceTime",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "HIT")

                    //TODO: Decide whether to keep or remove
                    //Clears Preference Datastore when clock out.
                    CoroutineScope(Dispatchers.IO).launch { attendanceManager.clearData() }
                } else if (result.contents == "AttendanceKey") {
                    val checkoutStatus = "-"
                    CoroutineScope(Dispatchers.IO).launch { attendanceManager.clearData() }
                } else if (result.contents == "AttendanceKey") {
                    val checkoutStatus = "     -"
                    val user = viewModelUser.fetchUserData().observe(this) {
                        Log.d(
                            TAG, " User: " +
                                    "${it.user}"
                        )
                        viewModel.uploadAttendanceCheckIn(
                            Attendance(
                                currentUser(), //UserID
                                sdf.format(Date()).toString(), //Current Date
                                stf.format(Date()).toString(), // Current Time
                                checkoutStatus, // 'Null' since, user is just checking in
                                checkTimings(), // Checks if user is on-time or late, returns status
                                "${it.user!!.EmployeeName}", // Gets the employee name
                                "${it.user!!.EmployeeEmail}" // Gets the employee email
                            )
                        )
                    }

                    // Check in Successful
                    Toast.makeText(
                        context,
                        "Check-In Successful @ $attendanceTime",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Save data into datastore preference
                    CoroutineScope(Dispatchers.IO).launch {
                        attendanceManager.storeAttendanceStatus(
                            true
                        )
                    }
                }

            } else {
                Toast.makeText(context, "Invalid QR code.", Toast.LENGTH_LONG).show()
            }
        }

    // Clears the DataStore cache at 12am everyday to allow for users to check in for the new day
    // Adds a absent status to their attendance record if the user has not checked-in during the day
    private fun attendanceWorkerRunner() {

        Log.d(UserAttendanceManager.TAG, "Attendance Worker Runner")
        val hourOfTheDay = 12 // Runs the job at 12am, change this 24hr format
        val repeatInterval = 1 // Runs the job everyday, dont change

        val flexTime: Long = calculateFlex(hourOfTheDay, repeatInterval)

        val workRequest = PeriodicWorkRequest.Builder(
            DailyAttendanceWorker::class.java,
            repeatInterval.toLong(), TimeUnit.DAYS,
            flexTime, TimeUnit.MILLISECONDS
        )
            .build()
        context?.let {

            WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                "clearDataStore",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    private fun calculateFlex(hourOfTheDay: Int, periodInDays: Int): Long {

        // Initialize the calendar with today and the preferred time to run the job.
        val cal1 = Calendar.getInstance()
        cal1[Calendar.HOUR_OF_DAY] = hourOfTheDay
        cal1[Calendar.MINUTE] = 0 // Time of when the job should be ran, change this
        cal1[Calendar.SECOND] = 0

        // Initialize a calendar with now.
        val cal2 = Calendar.getInstance()
        if (cal2.timeInMillis < cal1.timeInMillis) {

            // Add the worker periodicity.
            cal2.timeInMillis = cal2.timeInMillis + TimeUnit.DAYS.toMillis(periodInDays.toLong())
        }
        val delta = cal2.timeInMillis - cal1.timeInMillis
        return if (delta > PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS) delta else PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
    }

    private fun checkTimings(): String {
        val timeToMatch = Calendar.getInstance()
        val currentTime = Calendar.getInstance()

        // Setting the timing for being on time to be 0900
        timeToMatch.set(Calendar.HOUR_OF_DAY, 9)
        timeToMatch.set(Calendar.MINUTE, 0)

        // If currentTime is less than or equals to timeToMatch, they are on time
        if (currentTime < timeToMatch || currentTime == timeToMatch) {
            return "On-Time"
        }

        // If currentTime is after timeToMatch, they are late
        else if (currentTime > timeToMatch) {
            return "Late"
        }
        return "null"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        private val TAG = AttendanceQRCodeFragment::class.simpleName
        fun newInstance(): AttendanceQRCodeFragment {
            return AttendanceQRCodeFragment()
        }
    }
}