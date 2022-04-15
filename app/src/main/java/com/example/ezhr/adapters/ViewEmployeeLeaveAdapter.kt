package com.example.ezhr.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.R
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.data.User
import com.google.firebase.database.FirebaseDatabase

class ViewEmployeeLeaveAdapter(
    private var leaveStatusList: ArrayList<LeaveStatus>,
    private val idList: ArrayList<String>
) :
    RecyclerView.Adapter<ViewEmployeeLeaveAdapter.ViewHolder>() {

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
            val builder = AlertDialog.Builder(leaveOptionPopup.context)
            builder.setTitle("Leave Application Details")
            builder.setMessage(
                "Leave type : " + leaveStatusList[position].leaveType + "\nStart Date : " + leaveStatusList[position].startLeaveDate + "\nEnd Date : "
                        + leaveStatusList[position].endLeaveDate + "\nNumber of days : " + leaveStatusList[position].numberOfDays
                        + "\nLeave Status : " + leaveStatusList[position].leaveStatus
            )
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setPositiveButton("Done") { dialogInterface, which ->
            }
            builder.create()
            builder.setCancelable(false)
            builder.show()
        }

    }

    override fun getItemCount(): Int {
        return leaveStatusList.size
    }
}