package com.example.ezhr.adapters

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.R
import com.example.ezhr.data.LeaveStatus
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class LeaveStatusAdapter(
    private var leaveStatusList: ArrayList<LeaveStatus>,
    private val idList: ArrayList<String>,
    private val fileNameList: ArrayList<String>
) :
    RecyclerView.Adapter<LeaveStatusAdapter.ViewHolder>() {

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
        leaveTypeView.text = leaveStatusList[position].leaveType
        submissionDateView.text = leaveStatusList[position].submissionDate
        leaveStatusView.text = leaveStatusList[position].leaveStatus

        //When user clicks on 3 dot button widget
        leaveOptionPopup.setOnClickListener {

            //User actions for pending leaves
            if (leaveStatusView.text == "Pending") {
                val builder = AlertDialog.Builder(leaveOptionPopup.context)
                builder.setTitle("User options")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //When user click on view details
                builder.setPositiveButton(
                    "View Details"
                ) { dialog, which -> // do something like...
                    dialog.dismiss()

                    val alertDialog = AlertDialog.Builder(leaveOptionPopup.context)
                    alertDialog.setTitle("Leave Application Details")
                    alertDialog.setMessage(
                        "Leave type : " + leaveTypeView.text + "\nStart Date : " + leaveStatusList[position].startLeaveDate + "\nEnd Date : "
                                + leaveStatusList[position].endLeaveDate + "\nNumber of days : " + leaveStatusList[position].numberOfDays
                    )
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                    alertDialog.setPositiveButton("Done") { dialogInterface, which ->
                    }
                    alertDialog.setNegativeButton("Back") { dialogInterface, which ->
                        val dialog = builder.create()
                        dialog.show()
                    }
                    alertDialog.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }

                //When user click on Cancel Leave details
                builder.setNegativeButton(
                    "Cancel Leave"
                ) { dialog, which -> // do something like...
                    dialog.dismiss()

                    val alertDialog = AlertDialog.Builder(leaveOptionPopup.context)
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

                //builder.setNeutralButton("Close dialog box", null)
                // create and show the alert dialog
                val dialog = builder.create()
                dialog.show()
            }

            //User actions for approved leaves
            if (leaveStatusView.text == "Approved") {
                val builder = AlertDialog.Builder(leaveOptionPopup.context)

                builder.setTitle("Leave Application Details")
                builder.setMessage(
                    "\nLeave type : " + leaveTypeView.text + "\nStart Date : " + leaveStatusList[position].startLeaveDate + "\nEnd Date : "
                            + leaveStatusList[position].endLeaveDate + "\nNumber of days : " + leaveStatusList[position].numberOfDays
                )
                builder.setPositiveButton("Done", null)

                // create and show the alert dialog
                builder.create()
                builder.show()
            }

            //User actions for rejected leaves
            //User actions for approved leaves
            if (leaveStatusView.text == "Rejected") {
                val builder = AlertDialog.Builder(leaveOptionPopup.context)

                builder.setTitle("Leave Application Details")
                builder.setMessage(
                    "\nLeave type : " + leaveTypeView.text + "\nStart Date : " + leaveStatusList[position].startLeaveDate + "\nEnd Date : "
                            + leaveStatusList[position].endLeaveDate + "\nNumber of days : " + leaveStatusList[position].numberOfDays
                )
                builder.setPositiveButton("Done", null)

                // create and show the alert dialog
                builder.create()
                builder.show()
            }
        }

    }

    private fun deleteLeave(leaveID: String?, fileName: String) {
        var database = FirebaseDatabase.getInstance().getReference("Leaves")
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

    override fun getItemCount(): Int {
        return leaveStatusList.size
    }

}



