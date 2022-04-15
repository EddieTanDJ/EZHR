package com.example.ezhr.fragments.claims

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import com.example.ezhr.EZHRApp
import com.example.ezhr.databinding.FragmentClaimsBalanceBinding
import com.example.ezhr.viewmodel.ClaimBalanceViewModel
import com.example.ezhr.viewmodel.ClaimBalanceViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [ClaimBalanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimBalanceFragment : Fragment() {
    // NavController variable
    private lateinit var navController: NavController
    private var _binding: FragmentClaimsBalanceBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: ClaimBalanceViewModel by activityViewModels {
        val application = requireActivity().application
        val app = application as EZHRApp
        ClaimBalanceViewModelFactory(app.claimBalanceRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClaimsBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchClaimBalances()
        fetchClaimTotals()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Fetch all claim balances from ClaimBalance viewmodel
     */
    @SuppressLint("SetTextI18n")
    private fun fetchClaimBalances() {
        viewModel.fetchClaimBalances().observe(viewLifecycleOwner) {
            if (it.exception != null) {
                Log.d(TAG, it.exception.toString())
            }
            binding.textViewFoodBalance.text = "$" + it.balance!!.food_balance
            binding.textViewMedicalBalance.text = "$" + it.balance!!.medical_balance
            binding.textViewTransportationBalance.text = "$" + it.balance!!.transportation_balance
            binding.textViewOthersBalance.text = "$" + it.balance!!.others_balance
            Log.d(TAG, "Claim balances successfully read")
        }
    }

    /**
     * Fetch all claim totals from ClaimBalance viewmodel
     */
    @SuppressLint("SetTextI18n")
    private fun fetchClaimTotals() {
        viewModel.fetchClaimTotals().observe(viewLifecycleOwner) {
            if (it.exception != null) {
                Log.d(TAG, it.exception.toString())
            }
            binding.textViewFoodTotal.text = "$" + it.total!!.food_total
            binding.textViewMedicalTotal.text = "$" + it.total!!.medical_total
            binding.textViewTransportationTotal.text = "$" + it.total!!.transportation_total
            binding.textViewOthersTotal.text = "$" + it.total!!.others_total
            Log.d(TAG, "Claim balances successfully read")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.gi
         * @return A new instance of fragment.
         */
        val TAG = ClaimBalanceFragment::class.simpleName
        fun newInstance(): ClaimBalanceFragment {
            return ClaimBalanceFragment()
        }
    }
}