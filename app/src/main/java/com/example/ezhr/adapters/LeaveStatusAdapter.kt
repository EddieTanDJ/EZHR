package com.example.ezhr.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.data.Claim
import com.example.ezhr.data.LeaveStatus
import com.example.ezhr.databinding.LeaveDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class LeaveStatusAdapter(
    private var leaveStatusList: ArrayList<LeaveStatus>,
    private var idList: ArrayList<String>,
    private var fileNameList: ArrayList<String>
) :
    RecyclerView.Adapter<LeaveStatusAdapter.ViewHolder>() {
    private val TAG = "LeaveStatusAdapter"

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
        val leaveTypeView = constraintLayout.getChildAt(0) as TextView
        val submissionDateView = constraintLayout.getChildAt(1) as TextView
        val leaveStatusView = constraintLayout.getChildAt(2) as TextView
        val leaveOptionPopup = constraintLayout.getChildAt(3) as ImageButton
        val context = holder.itemView.context
        leaveTypeView.text = leaveStatusList[position].leaveType
        submissionDateView.text = leaveStatusList[position].submissionDate
        leaveStatusView.text = leaveStatusList[position].leaveStatus

        //When user clicks on 3 dot button widget
        leaveOptionPopup.setOnClickListener {
            Log.d(TAG, "onClick: clicked on leave option popup: leaveStatusList[position].leaveStatus = ${leaveStatusList[position]}")
            //User actions for pending leaves
            if (leaveStatusView.text == "Pending") {
                val builder = MaterialAlertDialogBuilder(leaveOptionPopup.context)
                builder.setTitle("User options")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //When user click on view details
                builder.setPositiveButton(
                    "View Details"
                ) { dialog, which -> // do something like...
                    dialog.dismiss()
                    val leaveType = leaveStatusList[position].leaveType.toString()
                    val nod = leaveStatusList[position].numberOfDays.toString()
                    val startLeaveDate = leaveStatusList[position].startLeaveDate.toString()
                    val endLeaveDate = leaveStatusList[position].endLeaveDate.toString()
                    val leaves = leaveStatusList[position]
                    val uri = idList[position]
                    val url = leaveStatusList[position].uploadedImg.toString()
                    Log.d(TAG, "onBindViewHolder Leaves Status: $leaves")
                    Log.d(TAG, "onBindViewHolder Pending uri: $uri")
                    Log.d(TAG, "onBindViewHolder Pending url: $url")
                    showDialog(context, leaveType, nod, startLeaveDate, endLeaveDate, url)

                }

                //When user click on Cancel Leave details
                builder.setNegativeButton(
                    "Cancel Leave"
                ) { dialog, which -> // do something like...
                    dialog.dismiss()

                    val alertDialog = MaterialAlertDialogBuilder(leaveOptionPopup.context)
                    alertDialog.setTitle("Cancel Leave Application")
                    alertDialog.setMessage(
                        "Are you sure you want to cancel this leave application?"
                    )
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                    alertDialog.setPositiveButton("Confirm") { dialogInterface, which ->
                        deleteLeave(idList[position], fileNameList[position])
                        notifyItemRemoved(position)
                    }
                    alertDialog.setNegativeButton("Back") { dialogInterface, which ->
                        val dialog = builder.create()
                        dialog.show()
                    }
                    alertDialog.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
                builder.setNeutralButton("Close Dialog Box", null)
                // create and show the alert dialog
                val dialog = builder.create()
                dialog.show()
            }

            //User actions for approved and rejected leaves
            if (leaveStatusView.text == "Approved" || leaveStatusView.text == "Rejected") {
                val leaveType = leaveStatusList[position].leaveType.toString()
                val nod = leaveStatusList[position].numberOfDays.toString()
                val startLeaveDate = leaveStatusList[position].startLeaveDate.toString()
                val endLeaveDate = leaveStatusList[position].endLeaveDate.toString()
                val url = leaveStatusList[position].uploadedImg.toString()
                showDialog(context, leaveType, nod, startLeaveDate, endLeaveDate, url)
            }

        }

    }

    /**
     * Delete leave from the database
     */
    private fun deleteLeave(leaveID: String?, fileName: String) {
        var database = FirebaseDatabase.getInstance().getReference("leaves")
        if (leaveID != null) {
            database.child(leaveID).removeValue()
        }

        val refStorage = FirebaseStorage.getInstance().reference.child("leaves/$leaveID/$fileName")
        refStorage.delete().addOnSuccessListener {
            Log.d("LeaveStatusFragment", "Image deleted.")
        }.addOnFailureListener {
            Log.d("LeaveStatusFragment", "Failed to delete image.")
        }
    }

    /**
     * Show Leave Status Dialog Box
     */
    private fun showDialog(context: Context, type: String, numberOfDays: String, startLeaveDate: String, endLeaveDate: String, url: String) {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
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



