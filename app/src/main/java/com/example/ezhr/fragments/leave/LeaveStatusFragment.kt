package com.example.ezhr.fragments.leave

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.adapters.LeaveStatusAdapter
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.databinding.FragmentLeaveStatusBinding
import com.example.ezhr.viewmodel.LeaveStatusViewModel
import com.example.ezhr.viewmodel.LeaveStatusViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

val leaveStatusList = ArrayList<LeaveStatus>()

/*
* This fragment is used to display the leave status of the user
 */
class LeaveStatusFragment : Fragment() {

    lateinit var todoRecyclerView: RecyclerView
    lateinit var recyclerLayoutManager: RecyclerView.LayoutManager
    var leaveList = ArrayList<LeaveStatus>()
    private var idList = ArrayList<String>()
    private var fileNameList = ArrayList<String>()
    private lateinit var leaveStatusViewModel: LeaveStatusViewModel
    var userID = Firebase.auth.currentUser?.uid

    // assign the _binding variable initially to null and
    // also when the view is destroyed again it has to be set to null
    private var _binding: FragmentLeaveStatusBinding? = null
    var uid = Firebase.auth.currentUser?.uid
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeaveStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaveStatusViewModel = ViewModelProvider(this, LeaveStatusViewModelFactory())
            .get(LeaveStatusViewModel::class.java)
        observeData()
        todoRecyclerView = binding.todoItemRecyclerView
        recyclerLayoutManager = LinearLayoutManager(context)
        showGif()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        leaveStatusViewModel.idList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            idList = it as ArrayList<String>
        }

        leaveStatusViewModel.fileNameList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            fileNameList = it as ArrayList<String>
        }

        leaveStatusViewModel.leaveList.observe(viewLifecycleOwner) {
            Log.i("data", it.toString())
            leaveList = it as ArrayList<LeaveStatus>
            var recyclerAdapter = LeaveStatusAdapter(it, idList, fileNameList)
            todoRecyclerView.apply {
                setHasFixedSize(true)
                layoutManager = recyclerLayoutManager
                adapter = recyclerAdapter
            }
        }
    }

    private fun showGif() {
        val imageView = binding.leaveBalanceImage
        Glide.with(this).load(R.drawable.beach).into(imageView)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment.
         */
        private val TAG = LeaveStatusFragment::class.java.simpleName
        fun newInstance(): LeaveStatusFragment {
            return LeaveStatusFragment()
        }
    }


}

