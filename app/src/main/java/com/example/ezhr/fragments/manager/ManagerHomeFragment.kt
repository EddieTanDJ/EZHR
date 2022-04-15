package com.example.ezhr.fragments.manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.EZHRApp
import com.example.ezhr.LoginActivity
import com.example.ezhr.R
import com.example.ezhr.databinding.FragmentManagerHomeBinding
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory
import com.example.ezhr.viewmodel.BalanceViewModel
import com.example.ezhr.viewmodel.BalanceViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase

/**
 * Home Fragment
 */
class ManagerHomeFragment : Fragment(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var listIntent: Intent

    // NavController variable
    private lateinit var navController: NavController

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentManagerHomeBinding? = null
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentManagerHomeBinding.inflate(inflater, container, false)
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
        navController = Navigation.findNavController(view)
        fetchName()
        binding.buttonApproveAttendance.setOnClickListener(this)
        binding.buttonApproveLeave.setOnClickListener(this)
        binding.buttonViewEmployeeLeave.setOnClickListener(this)
        binding.buttonApproveClaims.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.buttonApproveAttendance -> startAttendance()
            R.id.buttonApproveLeave -> startApproveLeave()
            R.id.buttonViewEmployeeLeave -> startViewLeave()
            R.id.buttonApproveClaims -> startClaims()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     *
     * Event Handler for attendance button
     */
    private fun startAttendance() {
        navController.navigate(R.id.action_homeManagerFragment_to_attendanceManagerFragment)
    }

    /**
     * Event Handler for leave approval button
     */
    private fun startApproveLeave() {
        navController.navigate(R.id.action_homeManagerFragment_to_leaveManagerFragment)
    }

    /**
     * Event handler for view employee leave button
     */
    private fun startViewLeave() {
        navController.navigate(R.id.action_homeManagerFragment_to_managerViewEmployeeLeaveFragment)
    }

    /**
     * Event Handler for claims button
     */
    private fun startClaims() {
        navController.navigate(R.id.action_homeManagerFragment_to_claimsManagerFragment)
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = ManagerHomeFragment::class.java.simpleName
        fun newInstance(): ManagerHomeFragment {
            return ManagerHomeFragment()
        }
    }

}

