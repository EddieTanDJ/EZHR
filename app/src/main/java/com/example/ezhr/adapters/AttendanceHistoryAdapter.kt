package com.example.ezhr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.R
import com.example.ezhr.data.Attendance


class AttendanceHistoryAdapter(
    var arrayList: List<Attendance>
) : RecyclerView.Adapter<AttendanceHistoryAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {
        private var userIdTextView: TextView = binding.findViewById(R.id.attendanceNameTextView)
        private var dateTextView: TextView = binding.findViewById(R.id.attendanceNameTextView)
        private var checkInTimeTextView: TextView = binding.findViewById(R.id.checkInTimeTextView)
        private var checkOutTimeTextView: TextView = binding.findViewById(R.id.checkOutTimeTextView)
        private var statusTextView: TextView = binding.findViewById(R.id.statusTextView)

        fun bind(attendance: Attendance) {
            userIdTextView.text = attendance.userID
            dateTextView.text = attendance.date
            checkInTimeTextView.text = attendance.checkInTime
            checkOutTimeTextView.text = attendance.checkOutTime
            statusTextView.text = attendance.status
        }
    }

    @NonNull
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AttendanceViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_history_item, parent, false)
        return AttendanceViewHolder(root)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = arrayList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun updateList(newList: List<Attendance>) {
        arrayList = newList
    }
}