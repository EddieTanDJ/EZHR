package com.example.ezhr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ezhr.R
import com.example.ezhr.databinding.FragmentLeaveBinding


/**
 * A simple [Fragment] subclass.
 * Use the [LeaveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LeaveFragment : Fragment() {
    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentLeaveBinding? = null

    // NavController variable
    private lateinit var navController: NavController

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaveBinding.inflate(inflater, container, false)
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
        val applyButton: Button = binding.buttonLeaveApply
        val statusBalance: Button = binding.buttonLeaveStatus
        val leaveBalance: Button = binding.buttonLeaveBalance

        navController = Navigation.findNavController(view)

        applyButton.setOnClickListener {
            startApplyLeave()
        }
        statusBalance.setOnClickListener {
            startLeaveStatus()
        }

        leaveBalance.setOnClickListener {
            startLeaveBalance()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Event Handler for apply leave button
     */
    private fun startApplyLeave() {
        navController.navigate(R.id.action_leaveFragment_to_applyLeaveFragment)
    }

    private fun startLeaveStatus() {
        navController.navigate(R.id.action_leaveFragment_to_leaveStatusFragment)
    }

    private fun startLeaveBalance() {
        navController.navigate(R.id.action_leaveFragment_to_balanceLeaveFragment)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        fun newInstance(): LeaveFragment {
            return LeaveFragment()
        }
    }

}