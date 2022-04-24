package com.example.ezhr.fragments.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.adapters.LeaveApprovalAdapter
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.databinding.FragmentManagerLeaveBinding
import com.example.ezhr.viewmodel.ManagerPendingLeaveViewModel
import com.example.ezhr.viewmodel.ManagerPendingLeaveViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


/**
 * A simple [Fragment] subclass.
 * Use the [ManagerLeaveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManagerLeaveFragment : Fragment() {
    private var leaveApplicationList = ArrayList<LeaveStatus>()
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var recyclerAdapter: LeaveApprovalAdapter
    private lateinit var recyclerLayoutManager: RecyclerView.LayoutManager
    private var idList = ArrayList<String>()
    var userID = Firebase.auth.currentUser?.uid

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentManagerLeaveBinding? = null

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!

    private val viewModel: ManagerPendingLeaveViewModel by activityViewModels {
        ManagerPendingLeaveViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagerLeaveBinding.inflate(inflater, container, false)
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
        getPendingLeavesData()

        todoRecyclerView = binding.todoItemRecyclerView

        recyclerLayoutManager = LinearLayoutManager(context)
        recyclerAdapter = LeaveApprovalAdapter(leaveApplicationList, idList)

        todoRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = recyclerAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPendingLeavesData() {
        viewModel.idList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            idList = it as ArrayList<String>
        }

        viewModel.leaveApplicationList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            leaveApplicationList = it as ArrayList<LeaveStatus>
            todoRecyclerView.adapter = LeaveApprovalAdapter(
                leaveApplicationList,
                idList
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */

        fun newInstance(): ManagerLeaveFragment {
            return ManagerLeaveFragment()
        }
    }
}