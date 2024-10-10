package com.example.tripplan.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripplan.R
import com.example.tripplan.data.GuestHouseData
import com.example.tripplan.databinding.LayoutItemBinding

class DataListAdapter(private val onClickAction: (data: GuestHouseData) -> Unit) :
    ListAdapter<GuestHouseData, DataListAdapter.GuestHouseItemViewHolder>(object :
        DiffUtil.ItemCallback<GuestHouseData>() {
        override fun areItemsTheSame(oldItem: GuestHouseData, newItem: GuestHouseData): Boolean {
            return oldItem.titleForGuestHouse == newItem.titleForGuestHouse
        }

        override fun areContentsTheSame(oldItem: GuestHouseData, newItem: GuestHouseData): Boolean {
            return oldItem == newItem
        }

    }) {

    inner class GuestHouseItemViewHolder(binding: LayoutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val thumbnailView = binding.GuestHouseIv
        private val nameView = binding.GuestHouseNameTv
        private val codeView = binding.GuestHouseCodeTv
        private val vendorView = binding.GuestHouseVendorTv

        fun bind(data: GuestHouseData) {
            with(data) {
                Glide.with(itemView).load(imageUrl)
                    .into(thumbnailView)
                nameView.text = titleForGuestHouse
                codeView.text = addressForGuestHouse
                vendorView.text = telForGuestHouse
                itemView.setOnClickListener { onClickAction(data) }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestHouseItemViewHolder {
        val binding = LayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuestHouseItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestHouseItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}