package com.example.ezhr.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.EZHRApp
import com.example.ezhr.LoginActivity
import com.example.ezhr.R
import com.example.ezhr.databinding.FragmentProfileBinding
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory


/**
 * A simple [Fragment] subclass.
 * Use the [AttendanceFragment.newInstance factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var listIntent: Intent

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentProfileBinding? = null
    private lateinit var navController: NavController

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        AccountViewModelFactory(app.accountRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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
        viewModel.fetchUserData().observe(viewLifecycleOwner) {
            if (it.exception != null) {
                Toast.makeText(
                    context,
                    "Error: ${it.exception.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            Log.d(TAG, "Data: ${it.user}")
            val employeeEmail = "Email: ${it.user!!.EmployeeEmail.toString()}"
            val employeeID = "ID: ${it.user!!.EmployeeID.toString()}"
            val employeeName = "Name: ${it.user!!.EmployeeName.toString()}"
            Log.d(TAG, "Data: $employeeEmail")
            binding.tvEmployeeEmail.text = employeeEmail
            binding.tvEmployeeID.text = employeeID
            binding.tvEmployeeName.text = employeeName
        }

        val editPasswordButton: Button = binding.editPasswordButton
        navController = Navigation.findNavController(view)
        editPasswordButton.setOnClickListener(this)

        val logOutButton: Button = binding.logOutButton
        logOutButton.setOnClickListener {
            activity?.let {
                viewModel.logout()
                listIntent = Intent(it, LoginActivity::class.java)
                listIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                it.startActivity(listIntent)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.editPasswordButton -> navController.navigate(R.id.action_profileFragment_to_editPasswordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = ProfileFragment::class.simpleName
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}


