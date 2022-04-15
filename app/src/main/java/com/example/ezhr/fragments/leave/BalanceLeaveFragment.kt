package com.example.ezhr.fragments.leave

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.databinding.FragmentBalanceLeaveBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

/**
 * Home Fragment
 */
class BalanceLeaveFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var listIntent: Intent

    // NavController variable
    private lateinit var navController: NavController

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentBalanceLeaveBinding? = null
    val userID = Firebase.auth.currentUser?.uid

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBalanceLeaveBinding.inflate(inflater, container, false)
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
        readDataBalance()
        showGif()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun readDataBalance() {
        database = FirebaseDatabase.getInstance().getReference("leave_balances")
        if (userID != null) {
            database.child(userID).get().addOnSuccessListener {
                if (it.exists()) {
                    val annualLeaveBalance = it.child("annual_balance").value
                    val compassionateLeaveBalance = it.child("compassionate_balance").value
                    val maternityLeaveBalance = it.child("maternity_balance").value
                    val medicalLeaveBalance = it.child("medical_balance").value

                    Log.d(TAG, "Leave Balances Successfully Read")
                    binding.annualLeaveBalance.text = annualLeaveBalance.toString()
                    binding.compassionateLeaveBalance.text = compassionateLeaveBalance.toString()
                    binding.medicalLeaveBalance.text = maternityLeaveBalance.toString()
                    binding.maternityLeaveBalance.text = medicalLeaveBalance.toString()
                } else {
                    Log.d(TAG, "User Doesn't Exist")
                }
            }.addOnFailureListener {
                Log.d(TAG, "Failed to Read Leave Balances")
            }
        }
    }

    private fun showGif() {
        val imageView = binding.leaveBalanceImage
        Glide.with(this).load(R.drawable.vacation).into(imageView)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = BalanceLeaveFragment::class.java.simpleName
        fun newInstance(): BalanceLeaveFragment {
            return BalanceLeaveFragment()
        }
    }
}