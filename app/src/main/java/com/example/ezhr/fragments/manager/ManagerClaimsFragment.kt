package com.example.ezhr.fragments.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.adapters.AdminClaimsAdapter
import com.example.ezhr.data.Claim
import com.example.ezhr.databinding.FragmentManagerClaimsBinding
import com.example.ezhr.viewmodel.ManagerClaimsViewModel
import com.example.ezhr.viewmodel.ManagerClaimsViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [ManagerClaimsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManagerClaimsFragment : Fragment() {
    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentManagerClaimsBinding? = null
    private val binding get() = _binding!!
    var claimList: List<Claim> = listOf()
    var idList: List<String> = listOf()
    var fileNameList: List<String> = listOf()
    private lateinit var adminClaimsAdapter: AdminClaimsAdapter
    private lateinit var managerClaimsViewModel: ManagerClaimsViewModel

    private lateinit var recyclerView: RecyclerView
    val userID = Firebase.auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagerClaimsBinding.inflate(inflater, container, false)
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
        Log.d(TAG, "onViewCreated: ManagerClaimsFragment")

        managerClaimsViewModel = ViewModelProvider(this, ManagerClaimsViewModelFactory())
            .get(ManagerClaimsViewModel::class.java)

        recyclerView = binding.recylerView
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        observeData()

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                adminClaimsAdapter = AdminClaimsAdapter(claimList, idList)

                val userinput = s.lowercase()
                val newList = mutableListOf<Claim>()
                for (claim in claimList) {
                    if (claim.title!!.lowercase()
                            .contains(userinput) || claim.dateApplied!!.lowercase()
                            .contains(userinput)
                    ) {
                        newList.add(claim)
                    }
                }
                adminClaimsAdapter.updateList(newList)
                recyclerView.adapter = adminClaimsAdapter

                return true
            }
        })
    }

    private fun observeData() {
        managerClaimsViewModel.idList.observe(viewLifecycleOwner) {
            Log.d(TAG, "idList: $it")
            idList = it
        }

        managerClaimsViewModel.fileNameList.observe(viewLifecycleOwner) {
            Log.d(TAG, "fileList: $it")
            fileNameList = it
        }

        managerClaimsViewModel.claimsList.observe(viewLifecycleOwner) {
            Log.d(TAG, "claimsList: $it")
            claimList = it
            recyclerView.adapter = AdminClaimsAdapter(it, idList)
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
        private val TAG = ManagerClaimsFragment::class.simpleName
        fun newInstance(): ManagerClaimsFragment {
            return ManagerClaimsFragment()
        }
    }
}