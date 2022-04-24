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
import com.example.ezhr.adapters.ViewEmployeeLeaveAdapter
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.databinding.FragmentManagerViewEmployeeLeaveBinding
import com.example.ezhr.viewmodel.ManagerApprovedLeaveViewModel
import com.example.ezhr.viewmodel.ManagerApprovedLeaveViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ManagerViewEmployeeLeaveFragment : Fragment() {
    private var leaveApplicationList = ArrayList<LeaveStatus>()
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var recyclerAdapter: ViewEmployeeLeaveAdapter
    private lateinit var recyclerLayoutManager: RecyclerView.LayoutManager
    var userID = Firebase.auth.currentUser?.uid

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentManagerViewEmployeeLeaveBinding? = null

    // with the backing property of the kotlin we extract
    // the non null value of the _binding
    private val binding get() = _binding!!

    private val viewModel: ManagerApprovedLeaveViewModel by activityViewModels {
        ManagerApprovedLeaveViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagerViewEmployeeLeaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getApprovedLeavesData()

        todoRecyclerView = binding.todoItemRecyclerView

        recyclerLayoutManager = LinearLayoutManager(context)
        recyclerAdapter = ViewEmployeeLeaveAdapter(leaveApplicationList)
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

    private fun getApprovedLeavesData() {
        viewModel.leaveApplicationList.observe(viewLifecycleOwner) {
            Log.d(TAG , it.toString())
            leaveApplicationList = it as ArrayList<LeaveStatus>
            todoRecyclerView.adapter = ViewEmployeeLeaveAdapter(
                leaveApplicationList
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private const val TAG = "ManagerVELF"
        fun newInstance(): ManagerViewEmployeeLeaveFragment {
            return ManagerViewEmployeeLeaveFragment()
        }
    }
}


