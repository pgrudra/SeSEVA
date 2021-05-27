package com.example.us0.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.R
import com.example.us0.databinding.InstalledAppItemViewBinding
import com.example.us0.databinding.SponsoredMissionItemViewBinding


class SponsoredMissionsAdapter(private val onClickListener: OnClickListener): ListAdapter<String, SponsoredMissionsAdapter.ViewHolder>(
    SponsoredMissionsDiffCallback()
){


    class ViewHolder private constructor(val binding: SponsoredMissionItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: String, position: Int){
            binding.missionName.text= binding.missionName.context.getString(R.string.sponsored_mission_name,position+1,item)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= SponsoredMissionItemViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onClickListener.onClick(item) }
        holder.bind(item,position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class OnClickListener(val clickListener:(missionName: String)->Unit){
        fun onClick(missionName: String)=clickListener(missionName)
    }


}

class SponsoredMissionsDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}