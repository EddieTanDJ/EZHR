package com.example.ezhr.fragments

import androidx.fragment.app.Fragment
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.EZHRApp
import com.example.ezhr.LoginActivity
import com.example.ezhr.R
import com.example.ezhr.adapters.ItemAdapter
import com.example.ezhr.databinding.FragmentHomeBinding
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory
import com.example.ezhr.viewmodel.BalanceViewModel
import com.example.ezhr.viewmodel.BalanceViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Home Fragment
 */
class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var listIntent: Intent

    // NavController variable
    private lateinit var navController: NavController

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentHomeBinding? = null
    private var uid = ""

    // view model
    private val viewModel: BalanceViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        BalanceViewModelFactory(app.balanceRepo)
    }

    private val viewModelAccount: AccountViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        AccountViewModelFactory(app.accountRepo)
    }

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!

    // Initialize Recycle View
    private val itemAdapter by lazy {
        ItemAdapter { position: Int, _: ItemAdapter.ChartItem ->
            binding.itemList.smoothScrollToPosition(position)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            com.example.ezhr.databinding.FragmentHomeBinding.inflate(inflater, container, false)
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
        val user = Firebase.auth.currentUser
        Log.d(TAG, "User: $user")
        if (user == null) {
            activity?.let {
                listIntent = Intent(it, LoginActivity::class.java)
                listIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                Toast.makeText(context, "Please kindly login", Toast.LENGTH_LONG).show()
                it.startActivity(listIntent)
            }
        } else {
            uid = user.uid
            Log.d(TAG, "UserID : $uid")
        }
        fetchBalances()
        fetchName()
        binding.itemList.initialize(itemAdapter)
        navController = Navigation.findNavController(view)
        binding.buttonAttendance.setOnClickListener(this)
        binding.buttonLeave.setOnClickListener(this)
        binding.buttonClaims.setOnClickListener(this)


    }

    /**
     * Fetches balance data for the chart to plot the pie chart using recycle view based on the live data
     * @param : Null
     * @return : Null
     */
    private fun fetchBalances() {
        val items = mutableListOf<ItemAdapter.ChartItem>()
        // Extract Claims Balance
        viewModel.fetchClaimsBalanceData().observe(viewLifecycleOwner) { claimsBalance ->
            Log.d(TAG, "Balance: $claimsBalance")
            if (claimsBalance.exception != null) {
                Toast.makeText(
                    context,
                    "Error: ${claimsBalance.exception.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            Log.d(TAG, "Error: ${claimsBalance.exception.toString()}")
            Log.d(TAG, "Data: $claimsBalance")

            val titleForClaimsBalance =
                "Medical Claims Balances:\n ${claimsBalance.balance!!.medical_balance} / 100.0"

            items.add(
                ItemAdapter.ChartItem(
                    id = 1,
                    title = titleForClaimsBalance,
                    available = claimsBalance.balance!!.medical_balance,
                    used = claimsBalance.balance!!.medical_used
                )
            )
            itemAdapter.setItems(items)
        }

        // Extract leave Balances
        viewModel.fetchLeaveBalanceData().observe(viewLifecycleOwner) { leaveBalance ->
            Log.d(TAG, "Leave Balance: $leaveBalance")
            if (leaveBalance.exception != null) {
                Toast.makeText(
                    context,
                    "Error: ${leaveBalance.exception.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            Log.d(TAG, "Error: ${leaveBalance.exception.toString()}")
            Log.d(TAG, "Data: $leaveBalance")
            val titleForLeavesBalance =
                "Annual Leaves Balance \n ${leaveBalance.balance!!.annual_balance} / 21.0"
            items.add(
                ItemAdapter.ChartItem(
                    id = 0,
                    title = titleForLeavesBalance,
                    available = leaveBalance.balance!!.annual_balance,
                    used = leaveBalance.balance!!.annual_used
                )
            )
        }
    }

    /**
     * Fetch Employee Name
     * @param : Null
     * @return : Null
     */
    private fun fetchName() {
        viewModelAccount.fetchUserData().observe(viewLifecycleOwner) {
            Log.d(TAG, "User: $it")
            if (it.exception != null) {
                Toast.makeText(
                    context,
                    "Error: ${it.exception.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            Log.d(TAG, "Data: ${it.user}")
            val welcome = "Hello ${it.user!!.EmployeeName}"
            // Set the welcome message
            binding.welcome.text = welcome
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.buttonAttendance -> showPopupAttendance(v, R.menu.attenadnce_popup_menu)
            R.id.buttonLeave -> showPopupLeave(v, R.menu.leave_popup_menu)
            R.id.buttonClaims -> showPopupClaims(v, R.menu.claims_popup_menu)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Show Popup Menu for Attendance
     * @param view : View
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPopupAttendance(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setForceShowIcon(true)
        // Set the popup to match parent for width
        popup.setOnMenuItemClickListener()
        { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.clockInClockOutButton -> {
                    Log.d(TAG, "${item.title} pressed")
                    startAttendanceMap()

                }
                R.id.attendanceHistoryButton -> {
                    Log.d(TAG, "${item.title} pressed")
                    startAttendanceHistory()
                }
            }
            true
        }
        // Show the popup menu.
        popup.show()
    }

    /**
     * Show Popup Menu for leave
     * @param view : View
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPopupLeave(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setForceShowIcon(true)
        // Set the popup to match parent for width
        popup.setOnMenuItemClickListener()
        { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.applyLeaveButton -> {
                    Log.d(TAG, "${item.title} pressed")
                    startLeaveAttendance()
                }
                R.id.leaveStatusButton -> {
                    Log.d(TAG, "${item.title} pressed")
                    startLeaveStatus()
                }
            }
            true
        }
        popup.show()
    }

    /**
     * Show Popup Menu for Claims
     * @param view : View
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPopupClaims(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setForceShowIcon(true)
        // Set the popup to match parent for width
        popup.setOnMenuItemClickListener()
        { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.applyClaimsButton -> {
                    Log.d(TAG, "${item.title} pressed")
                    startClaimsAttendance()
                }
                R.id.claimsStatusButton -> {
                    Log.d(TAG, "${item.title} pressed")
                    startClaimsStatus()
                }
            }
            true
        }
        popup.show()
    }


    /**
     * Event Handler for clock in/out button
     */
    private fun startAttendanceMap() {
        navController.navigate(R.id.action_homeFragment_to_attendanceMapFragment)
    }

    /**
     * Event Handler for clock in/out button
     */
    private fun startAttendanceHistory() {
        navController.navigate(R.id.action_homeFragment_to_attendanceHistoryFragment)
    }


    /**
     * Event Handler for apply leave button
     */
    private fun startLeaveAttendance() {
        navController.navigate(R.id.action_homeFragment_to_applyLeaveFragment)
    }

    /**
     * Event Handler for leave button
     */
    private fun startLeaveStatus() {
        navController.navigate(R.id.action_homeFragment_to_leaveStatusFragment)
    }

    /**
     * Event Handler for claims Attendance Button
     */
    private fun startClaimsAttendance() {
        navController.navigate(R.id.action_homeFragment_to_applyClaimFragment)
    }

    /**
     * Event Handler for claims Status Button
     */
    private fun startClaimsStatus() {
        navController.navigate(R.id.action_homeFragment_to_claimsStatusFragment)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = HomeFragment::class.simpleName
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}

