package com.example.ezhr.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.R
import com.example.ezhr.data.Attendance
import com.example.ezhr.viewmodel.ManagerAttendanceViewModel


/*
A RecyclerView adapter handles the binding of the item.xml layout to the RecyclerView.
It also takes in a list of items and displays them to the user.
The code for the RecyclerView adapter is shown below.
 */

class ManagerAttendanceAdapter(var arrayList: List<Attendance>) :
    RecyclerView.Adapter<ManagerAttendanceAdapter.AttendanceViewHolder>() {
    private lateinit var listener: ItemListener
    private val TAG = "MAAdapter"

    interface ItemListener {
        fun onItemClicked(recipient: String, subject: String, message: String)
    }

    fun setListener(listener: ItemListener) {
        this.listener = listener
    }

    private lateinit var viewModel: ManagerAttendanceViewModel

    inner class AttendanceViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {
        private var employeeNameTextView: TextView = binding.findViewById(R.id.employeeName)
        private var dateTextView: TextView = binding.findViewById(R.id.attendanceDate)
        private var statusTextView: TextView = binding.findViewById(R.id.attendanceStatus)
        private var btnEmailEmployee: ImageButton = binding.findViewById(R.id.emailButton)

        fun bind(attendance: Attendance) {
            employeeNameTextView.text = attendance.name
            dateTextView.text = attendance.date
            statusTextView.text = attendance.status
            if (attendance.status == "Late" || attendance.status == "Absent") {
                btnEmailEmployee.visibility = View.VISIBLE
            } else {
                btnEmailEmployee.visibility = View.GONE
            }
        }

        fun emailEmployee(attendance: Attendance) {
            btnEmailEmployee.setOnClickListener {
                Log.d(TAG, "Email: ${attendance.email.toString()}")
                val recipient = attendance.email.toString()
                val subject = "Regarding being ${attendance.status} on ${attendance.date}."
                val message = "Hi ${attendance.name},\n\n" +
                        "I am writing to inform you that you have been ${
                            attendance.status.toString().lowercase()
                        } your attendance on ${attendance.date}.  " +
                        "Please do not be ${attendance.status.toString().lowercase()} again!\n\n" +
                        "Regards,\n" +
                        "Your Manager"
                listener.onItemClicked(recipient, subject, message)
                notifyDataSetChanged()
                Log.d(TAG, "Email button pressed")
            }
        }
    }

    @NonNull
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AttendanceViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_manager_item, parent, false)
        return AttendanceViewHolder(root)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = arrayList[position]
        holder.bind(item)
        holder.emailEmployee(item)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun updateList(newList: List<Attendance>) {
        arrayList = newList
    }

}



