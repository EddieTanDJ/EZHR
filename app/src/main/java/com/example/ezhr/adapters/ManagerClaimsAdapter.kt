package com.example.ezhr.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ezhr.R
import com.example.ezhr.data.Claim
import com.example.ezhr.data.User
import com.example.ezhr.databinding.AdminClaimListItemBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private lateinit var database: DatabaseReference

internal class AdminClaimsAdapter(
    private var claimList: List<Claim>,
    private var idList: List<String>,
) :
    RecyclerView.Adapter<AdminClaimsAdapter.MyViewHolder>() {
    private val TAG = "ManagerClaimsAdapter"
    internal inner class MyViewHolder(val binding: AdminClaimListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            AdminClaimListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val context = holder.itemView.context

        val item = claimList[position]
        holder.binding.textViewDate.text = item.dateApplied

        val claimID = idList[position]

        database = FirebaseDatabase.getInstance().getReference("user")
        database.child(item.userID.toString()).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            if (user != null) {
                holder.binding.textViewTextName.text = user.EmployeeName
            }
        }.addOnFailureListener {
            Log.d("firebase", "Error getting data", it)
        }

        holder.binding.textViewTitle.text = item.title
        holder.itemView.setOnClickListener {
            Log.d(TAG, "onClick: clicked on: $item")
            val title = item.title
            val date = item.dateApplied
            val amtAndType = "$${item.amount} (${item.claimType})"
            val desc = item.desc
            val url = item.uploadedImg

            showBox(
                context,
                title.toString(),
                date.toString(),
                amtAndType,
                desc.toString(),
                url.toString()
            )
        }

        val builder = AlertDialog.Builder(context)

        holder.binding.imageButtonAccept.background.setTint(
            ContextCompat.getColor(
                context,
                R.color.colorPrimary
            )
        )
        holder.binding.imageButtonAccept.setOnClickListener {
            val userID = item.userID.toString()
            builder.setMessage("Confirm to accept this claim?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    val databaseReference = FirebaseDatabase.getInstance().getReference("claims")
                    databaseReference.child(claimID).child("status").setValue("ACCEPTED")

                    val claimBalance = mapOf(
                        "Food" to "food_balance",
                        "Medical" to "medical_balance",
                        "Others" to "others_balance",
                        "Transportation" to "transportation_balance"
                    )

                    var usedMedical = 0.0
                    val databaseReferenceUsedMedical =
                        FirebaseDatabase.getInstance().getReference("claim_balances")
                    databaseReferenceUsedMedical.child(userID).child("medical_used").get()
                        .addOnSuccessListener {
                            usedMedical = it.value.toString().toDouble()
                        }.addOnFailureListener {
                            Log.d("firebase", "Error getting data used medical claim", it)
                        }

                    val databaseReference2 =
                        FirebaseDatabase.getInstance().getReference("claim_balances")
                    databaseReference2.child(userID).child(claimBalance[item.claimType]!!).get()
                        .addOnSuccessListener {
                            val finalAmt = it.value.toString().toDouble() - item.amount!!
                            databaseReference2.child(userID).child(claimBalance[item.claimType]!!)
                                .setValue(String.format("%.2f", finalAmt).toDouble())

                            if (item.claimType == "Medical") {
                                databaseReference2.child(userID).child("medical_used").setValue(
                                    String.format("%.2f", usedMedical + item.amount!!).toDouble()
                                )
                            }
                        }.addOnFailureListener {
                            Log.d("firebase", "Error getting data", it)
                        }
                    Toast.makeText(context, "Claim successfully accepted.", Toast.LENGTH_SHORT)
                        .show()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        holder.binding.imageButtonReject.setOnClickListener {
            builder.setMessage("Confirm to reject this claim?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    val databaseReference = FirebaseDatabase.getInstance().getReference("claims")
                    databaseReference.child(claimID).child("status").setValue("REJECTED")
                    Toast.makeText(context, "Claim successfully rejected.", Toast.LENGTH_SHORT)
                        .show()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    override fun getItemCount(): Int {
        return claimList.size
    }

    fun updateList(newList: List<Claim>) {
        claimList = newList
    }

    private fun showBox(
        context: Context,
        title: String,
        date: String,
        amtAndType: String,
        desc: String,
        url: String,
    ) {
        Log.d(TAG, "context: $context")
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
        val view = LayoutInflater.from(context).inflate(R.layout.manager_claim_detail, null)
        val button = view.findViewById<Button>(R.id.buttonClose)

        val claimTitle = view.findViewById<TextView>(R.id.textViewClaimTitle2)
        val claimDate = view.findViewById<TextView>(R.id.textViewClaimDate2)
        val claimAmtAndType = view.findViewById<TextView>(R.id.textViewAmtCat)
        val textViewDesc = view.findViewById<TextView>(R.id.textViewDesc)
        val uploadedImg = view.findViewById<ImageView>(R.id.imageViewDocument2)
        Glide.with(context).load(url).into(uploadedImg)
        Log.d(TAG, "url: $url")
        claimTitle.text = title
        claimDate.text = date
        claimAmtAndType.text = amtAndType
        textViewDesc.text = desc

        builder.setView(view)
        button.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }
}