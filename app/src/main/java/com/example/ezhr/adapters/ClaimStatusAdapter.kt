package com.example.ezhr.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ezhr.data.Claim
import com.example.ezhr.databinding.ClaimListItemBinding

internal class ClaimStatusAdapter(
    private var claimList: List<Claim>,
    private var idList: List<String>,
    private var fileNameList: List<String>
) :
    RecyclerView.Adapter<ClaimStatusAdapter.MyViewHolder>() {
    private val TAG = "ClaimsStatusAdapter"
    private lateinit var listener: ItemListener

    interface ItemListener {
        fun onItemClicked(claim: Claim, claimID: String, uploadedImg: String, position: Int)
    }

    fun setListener(listener: ItemListener) {
        this.listener = listener
    }

    internal inner class MyViewHolder(val binding: ClaimListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ClaimListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = claimList[position]
        holder.binding.title.text = item.title
        holder.binding.status.text = item.status
        holder.binding.dateApplied.text = item.dateApplied

        holder.itemView.setOnClickListener {
            listener.onItemClicked(item, idList[position], fileNameList[position], position)
            notifyDataSetChanged()
            Log.d(TAG, "clicked: " + item.title)
        }
    }

    override fun getItemCount(): Int {
        return claimList.size
    }

    // Add in this too
    fun updateList(newList: List<Claim>) {
        claimList = newList
    }
}