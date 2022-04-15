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
import com.example.ezhr.databinding.FragmentClaimsBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ClaimsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimsFragment : Fragment() {

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentClaimsBinding? = null

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
        _binding = FragmentClaimsBinding.inflate(inflater, container, false)
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
        val applyButton: Button = binding.buttonApply
        val balanceButton: Button = binding.buttonBalance
        val claimsStatusButton: Button = binding.buttonClaimsStatus

        navController = Navigation.findNavController(view)

        applyButton.setOnClickListener {
            startApplyClaims()
        }
        balanceButton.setOnClickListener {
            activity?.let {
                startClaimsBalance()
            }
        }
        claimsStatusButton.setOnClickListener {
            activity?.let {
                startClaimsStatus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Event Handler for apply claims button
     */
    private fun startApplyClaims() {
        navController.navigate(R.id.action_claimsFragment_to_applyClaimFragment)
    }

    /**
     * Event Handler for claims status button
     */
    private fun startClaimsStatus() {
        navController.navigate(R.id.action_claimsFragment_to_claimsStatusFragment)
    }

    /**
     * Event handler for claims balance button
     */
    private fun startClaimsBalance() {
        navController.navigate(R.id.action_claimsFragment_to_claimsBalanceFragment)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        fun newInstance(): ClaimsFragment {
            return ClaimsFragment()
        }
    }
}