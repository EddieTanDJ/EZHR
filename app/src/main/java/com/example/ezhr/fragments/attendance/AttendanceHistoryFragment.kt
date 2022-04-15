package com.example.ezhr.fragments.attendance

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.adapters.AttendanceHistoryAdapter
import com.example.ezhr.data.Attendance
import com.example.ezhr.databinding.FragmentAttendanceHistoryBinding
import com.example.ezhr.viewmodel.AttendanceHistoryViewModel
import com.example.ezhr.viewmodel.AttendanceHistoryViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [AttendanceHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@RestrictTo(RestrictTo.Scope.TESTS)
class AttendanceHistoryFragment : Fragment() {

    var attendanceHistoryList: List<Attendance> = listOf()

    private lateinit var attendanceHistoryAdapter: AttendanceHistoryAdapter
    private lateinit var attendanceHistoryViewModel: AttendanceHistoryViewModel
    private lateinit var attendanceRecyclerView: RecyclerView

    // NavController variable
    private lateinit var navController: NavController

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentAttendanceHistoryBinding? = null

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAttendanceHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attendanceHistoryViewModel = ViewModelProvider(this, AttendanceHistoryViewModelFactory())
            .get(AttendanceHistoryViewModel::class.java)
        navController = Navigation.findNavController(view)

        val layoutManager = LinearLayoutManager(context)
        attendanceRecyclerView = binding.calendarRecyclerView
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        attendanceRecyclerView.layoutManager = layoutManager

        observeData()
        showGif()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        attendanceHistoryViewModel.getUserAttendanceHistory().observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            attendanceHistoryList = it
            val adapter = AttendanceHistoryAdapter(it)
            attendanceRecyclerView.adapter = adapter
        }

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                attendanceHistoryAdapter = AttendanceHistoryAdapter(attendanceHistoryList)

                val userinput = s.lowercase()
                val newList = mutableListOf<Attendance>()
                for (attendance in attendanceHistoryList) {
                    if (attendance.date!!.lowercase()
                            .contains(userinput) || attendance.checkInTime!!.lowercase()
                            .contains(userinput) || attendance.checkOutTime!!.lowercase()
                            .contains(userinput) || attendance.status!!.lowercase()
                            .contains(userinput)
                    ) {
                        newList.add(attendance)
                    }
                }
                attendanceHistoryAdapter.updateList(newList)
                attendanceRecyclerView.adapter = attendanceHistoryAdapter
                return true
            }
        })
    }

    private fun showGif() {
        val imageView = binding.attendanceHistoryImage
        Glide.with(this).load(R.drawable.employee_attendance_history).into(imageView)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance(): AttendanceHistoryFragment {
            return AttendanceHistoryFragment()
        }

    }
}