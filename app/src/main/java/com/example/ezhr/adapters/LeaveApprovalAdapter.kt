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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

class LeaveApprovalAdapter(
    private var leaveStatusList: ArrayList<LeaveStatus>,
    private val idList: ArrayList<String>
) :
    RecyclerView.Adapter<LeaveApprovalAdapter.ViewHolder>() {
    private val TAG = "LeaveApprovalAdapter"
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
        val submissionDateView = constraintLayout.getChildAt(1) as TextView
        val leaveTypeView = constraintLayout.getChildAt(2) as TextView
        val leaveOptionPopup = constraintLayout.getChildAt(3) as ImageButton
        val context = holder.itemView.context
        submissionDateView.text = leaveStatusList[position].submissionDate
        leaveTypeView.text = leaveStatusList[position].leaveType

        val database = FirebaseDatabase.getInstance().getReference("user")
        database.child(leaveStatusList[position].userID.toString()).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            if (user != null) {
                employee.text = user.EmployeeName.toString()
            }
        }

        //When user clicks on 3 dot button widget
        leaveOptionPopup.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(leaveOptionPopup.context)
            builder.setTitle("Manager options")
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
                val url = leaveStatusList[position].uploadedImg.toString()
                showDialog(context, leaveType, nod, startLeaveDate, endLeaveDate, url)
            }
            //When the manager clicks approve leave
            builder.setNegativeButton(
                "Approve Leave"
            ) { dialog, which -> // do something like...
                dialog.dismiss()

                val alertDialog = MaterialAlertDialogBuilder(leaveOptionPopup.context)
                alertDialog.setTitle("Approve Leave Application")
                alertDialog.setMessage(
                    "Are you sure you want to approve this leave application?"
                )
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                alertDialog.setPositiveButton("Confirm") { dialogInterface, which ->
                    //approveLeaveApplication
                    val approveLeave = FirebaseDatabase.getInstance().getReference("leaves")
                    approveLeave.child(idList[position]).child("leaveStatus").setValue("Approved")

                    val leaveBalance = mapOf(
                        "Annual" to "annual_balance",
                        "Medical" to "medical_balance",
                        "Maternity" to "maternity_balance",
                        "Compassionate" to "compassionate_balance"
                    )


                    val userID = leaveStatusList[position].userID.toString()
                    val leaveTypeColumn = leaveBalance[leaveStatusList[position].leaveType]!!
                    val daysNum = leaveStatusList[position].numberOfDays.toString().toInt()


                    val databaseReference2 =
                        FirebaseDatabase.getInstance().getReference("leave_balances")
                    databaseReference2.child(userID)
                        .child(leaveTypeColumn).get()
                        .addOnSuccessListener {
                            val finalAmt = it.value.toString().toInt() - daysNum
                            databaseReference2.child(userID)
                                .child(leaveTypeColumn)
                                .setValue(finalAmt)

                        }.addOnFailureListener {
                            Log.d("firebase", "Error getting data", it)
                        }

                    var usedLeave = 0
                    val databaseReferenceUsedMedical =
                        FirebaseDatabase.getInstance().getReference("leave_balances")
                    databaseReferenceUsedMedical.child(userID).child("annual_used").get()
                        .addOnSuccessListener {
                            usedLeave = it.value.toString().toInt()
                            Log.d("test", usedLeave.toString())
                        }.addOnFailureListener {
                            Log.d("firebase", "Error getting data used medical claim", it)
                        }

                    if (leaveStatusList[position].leaveType == "Annual") {
                        databaseReference2.child(userID).child("annual_used")
                            .setValue(usedLeave + daysNum)
                    }

                }
                alertDialog.setNegativeButton("Back") { dialogInterface, which ->
                    val dialog = builder.create()
                    dialog.show()
                }
                alertDialog.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }

            //When the manager wants to reject leaves
            builder.setNeutralButton(
                "Reject Leave"
            ) { dialog, which -> // do something like...
                dialog.dismiss()

                val alertDialog = MaterialAlertDialogBuilder(leaveOptionPopup.context)
                alertDialog.setTitle("Cancel Leave Application")
                alertDialog.setMessage(
                    "Are you sure you want to reject this leave application?"
                )
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert)

                alertDialog.setPositiveButton("Confirm") { dialogInterface, which ->
                    val approveLeave = FirebaseDatabase.getInstance().getReference("leaves")
                    approveLeave.child(idList[position]).child("leaveStatus").setValue("Rejected")

                }
                alertDialog.setNegativeButton("Back") { dialogInterface, which ->
                    val dialog = builder.create()
                    dialog.show()
                }
                alertDialog.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
            builder.create()
            builder.show()
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