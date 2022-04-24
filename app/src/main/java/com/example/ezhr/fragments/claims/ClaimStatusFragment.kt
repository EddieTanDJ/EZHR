package com.example.ezhr.fragments.claims

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.R
import com.example.ezhr.adapters.ClaimStatusAdapter
import com.example.ezhr.data.Claim
import com.example.ezhr.databinding.FragmentClaimsStatusBinding
import com.example.ezhr.viewmodel.ClaimDetailViewModel
import com.example.ezhr.viewmodel.ClaimDetailViewModelFactory
import com.example.ezhr.viewmodel.ClaimStatusViewModel
import com.example.ezhr.viewmodel.ClaimStatusViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [ClaimStatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimStatusFragment : Fragment(), ClaimStatusAdapter.ItemListener {
    var claimList: List<Claim> = listOf()
    var idList: List<String> = listOf()
    var fileNameList: List<String> = listOf()

    private lateinit var claimStatusAdapter: ClaimStatusAdapter
    private lateinit var claimStatusViewModel: ClaimStatusViewModel

    private lateinit var recyclerView: RecyclerView
    val userID = Firebase.auth.currentUser?.uid

    // NavController variable
    private lateinit var navController: NavController
    private var _binding: FragmentClaimsStatusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClaimDetailViewModel by activityViewModels {
        ClaimDetailViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClaimsStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        claimStatusViewModel =
            ViewModelProvider(this, ClaimStatusViewModelFactory())[ClaimStatusViewModel::class.java]
        navController = Navigation.findNavController(view)
        recyclerView = binding.recylerView

        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        observeData()

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                claimStatusAdapter = ClaimStatusAdapter(claimList, idList, fileNameList)

                val userinput = s.lowercase()
                val newList = mutableListOf<Claim>()
                for (claim in claimList) {
                    if (claim.title!!.lowercase().contains(userinput) || claim.status!!.lowercase()
                            .contains(userinput) || claim.dateApplied!!.lowercase()
                            .contains(userinput)
                    ) {
                        newList.add(claim)
                    }
                }
                claimStatusAdapter.updateList(newList)
                recyclerView.adapter = claimStatusAdapter
                claimStatusAdapter.setListener(this@ClaimStatusFragment)
                return true
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Retrieve claim, id, filename lists from ClaimDetail viewmodel
     */
    private fun observeData() {
        claimStatusViewModel.idList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            idList = it
        }

        claimStatusViewModel.fileNameList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            fileNameList = it
        }

        claimStatusViewModel.claimsList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            claimList = it
            var adapter = ClaimStatusAdapter(it, idList, fileNameList)
            recyclerView.adapter = adapter
            adapter.setListener(this)
        }
    }

    override fun onItemClicked(claim: Claim, claimID: String, uploadedImg: String, position: Int) {
        Log.d(TAG, "Updating claim data to ViewModel")
        Log.d(TAG, "Claim ID: $claimID")
        Log.d(TAG, "Uploaded Image: $uploadedImg")
        Log.d(TAG, "Claim: $claim")
        viewModel.setClaims(claim)
        viewModel.setClaimID(claimID)
        viewModel.setUploadImage(uploadedImg)
        navController.navigate(R.id.action_claimsStatusFragment_to_claimsDetailFragment)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = ClaimStatusFragment::class.simpleName
        fun newInstance(): ClaimStatusFragment {
            return ClaimStatusFragment()
        }
    }
}