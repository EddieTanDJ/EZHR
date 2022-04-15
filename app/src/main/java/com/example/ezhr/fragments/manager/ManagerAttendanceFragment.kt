package com.example.ezhr.fragments.manager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.EZHRApp
import com.example.ezhr.adapters.ManagerAttendanceAdapter
import com.example.ezhr.data.Attendance
import com.example.ezhr.databinding.FragmentManagerAttendanceBinding
import com.example.ezhr.viewmodel.ManagerAttendanceViewModel
import com.example.ezhr.viewmodel.ManagerAttendanceViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [ManagerAttendanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManagerAttendanceFragment : Fragment(), ManagerAttendanceAdapter.ItemListener {

    var attendanceList: List<Attendance> = listOf()

    private lateinit var managerStatusAdapter: ManagerAttendanceAdapter
    private lateinit var managerAttendanceHistoryViewModel: ManagerAttendanceViewModel
    private lateinit var attendanceRecyclerView: RecyclerView

    // NavController variable
    private lateinit var navController: NavController

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentManagerAttendanceBinding? = null

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!

    private val viewModel: ManagerAttendanceViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        ManagerAttendanceViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentManagerAttendanceBinding.inflate(inflater, container, false)
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
        managerAttendanceHistoryViewModel =
            ViewModelProvider(this, ManagerAttendanceViewModelFactory())
                .get(ManagerAttendanceViewModel::class.java)
        navController = Navigation.findNavController(view)

        val layoutManager = LinearLayoutManager(context)
        attendanceRecyclerView = binding.recyclerView
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        attendanceRecyclerView.layoutManager = layoutManager
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        managerAttendanceHistoryViewModel.getUserAttendance().observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            attendanceList = it
            var adapter = ManagerAttendanceAdapter(it)
            attendanceRecyclerView.adapter = adapter
            adapter.setListener(this)
        }
        binding.employeeName.text.toString() // Gets employee id

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                managerStatusAdapter = ManagerAttendanceAdapter(attendanceList)

                val userinput = s.lowercase()
                val newList = mutableListOf<Attendance>()
                for (attendance in attendanceList) {
                    if (attendance.name!!.lowercase()
                            .contains(userinput) || attendance.status!!.lowercase()
                            .contains(userinput) || attendance.date!!.lowercase()
                            .contains(userinput)
                    ) {
                        newList.add(attendance)
                    }
                }
                managerStatusAdapter.updateList(newList)
                attendanceRecyclerView.adapter = managerStatusAdapter
                managerStatusAdapter.setListener(this@ManagerAttendanceFragment)
                return true
            }
        })
    }

    fun sendEmail(recipient: String, subject: String, message: String) {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SENDTO)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        // put recipient email in intent
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        } catch (e: Exception) {
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onItemClicked(recipient: String, subject: String, message: String) {
        sendEmail(recipient, subject, message)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
//        private val TAG = ManagerAttendanceFragment::class.simpleName
        fun newInstance(): ManagerAttendanceFragment {
            return ManagerAttendanceFragment()
        }
    }
}