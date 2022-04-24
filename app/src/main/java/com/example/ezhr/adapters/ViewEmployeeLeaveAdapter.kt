package com.example.ezhr.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.data.User
import com.google.firebase.database.FirebaseDatabase

class ViewEmployeeLeaveAdapter(
    private var leaveStatusList: ArrayList<LeaveStatus>
) :
    RecyclerView.Adapter<ViewEmployeeLeaveAdapter.ViewHolder>() {
    private val TAG = "ViewELA"
    class ViewHolder(val constraintLayout: ConstraintLayout) :
        RecyclerView.ViewHolder(constraintLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val constraintLayout = LayoutInflater.from(parent.context).inflate(
            R.layout.leave_status_item, parent, false
        ) as ConstraintLayout

        return ViewHolder((constraintLayout))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val constraintLayout = holder.constraintLayout

        val employee = constraintLayout.getChildAt(0) as TextView
        val startDateView = constraintLayout.getChildAt(1) as TextView
        val endDateView = constraintLayout.getChildAt(2) as TextView
        val leaveOptionPopup = constraintLayout.getChildAt(3) as ImageButton
        val context = holder.itemView.context
        startDateView.text = leaveStatusList[position].startLeaveDate
        endDateView.text = leaveStatusList[position].endLeaveDate

        val database = FirebaseDatabase.getInstance().getReference("user")
        database.child(leaveStatusList[position].userID.toString()).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            if (user != null) {
                employee.text = user.EmployeeName.toString()
            }
        }

        //When user clicks on 3 dot button widget
        leaveOptionPopup.setOnClickListener {
            val leaveType = leaveStatusList[position].leaveType.toString()
            val nod = leaveStatusList[position].numberOfDays.toString()
            val startLeaveDate = leaveStatusList[position].startLeaveDate.toString()
            val endLeaveDate = leaveStatusList[position].endLeaveDate.toString()
            val url = leaveStatusList[position].uploadedImg.toString()
            showDialog(context, leaveType, nod, startLeaveDate, endLeaveDate, url)
        }

    }

    /**
     * Show Leave Status Dialog Box
     */
    private fun showDialog(context: Context, type: String, numberOfDays: String, startLeaveDate: String, endLeaveDate: String, url: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
        val view = LayoutInflater.from(context).inflate(R.layout.leave_detail, null)
        val button =view.findViewById<Button>(R.id.buttonClose)
        val leaveType =  view.findViewById<TextView>(R.id.textViewLT)
        val nod =  view.findViewById<TextView>(R.id.textViewNOD)
        val startDate =  view.findViewById<TextView>(R.id.textViewStartDate)
        val endDate =  view.findViewById<TextView>(R.id.textViewEndDate)
        val uploadedImg =  view.findViewById<ImageView>(R.id.imageViewDocument)
        Log.d(TAG, "showDialog: Context: $context")
        Log.d(TAG, "showDialog: type: $type")
        Log.d(TAG, "showDialog: numberOfDays: $numberOfDays")
        Log.d(TAG, "showDialog: startLeaveDate: $startLeaveDate")
        Log.d(TAG, "showDialog: endLeaveDate: $endLeaveDate")
        Log.d(TAG, "showDialog: url: $url")
        Glide.with(context).load(url).into(uploadedImg)
        leaveType.text = type
        nod.text = "No. of Day/s: $numberOfDays"
        startDate.text = "Start Date: $startLeaveDate"
        endDate.text = "End Date: $endLeaveDate"
        builder.setView(view)
        button.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()

    }

    override fun getItemCount(): Int {
        return leaveStatusList.size
    }
}