package com.example.us0.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.data.stats.Stat
import com.example.us0.databinding.InstalledAppItemViewBinding

class StatAdapter(private val onClickListener: OnClickListener): ListAdapter<Stat, StatAdapter.ViewHolder>(
    StatDiffCallback()
){


    class ViewHolder private constructor(val binding:InstalledAppItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Stat){
            binding.appName.text=item.appName
            binding.appIcon.setImageDrawable(binding.appIcon.context.packageManager.getApplicationIcon(item.packageName!!))
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= InstalledAppItemViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onClickListener.onClick(item) }
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class OnClickListener(val clickListener:(stat: Stat)->Unit){
        fun onClick(stat: Stat)=clickListener(stat)
    }


}

class StatDiffCallback : DiffUtil.ItemCallback<Stat>() {
    override fun areItemsTheSame(oldItem: Stat, newItem: Stat): Boolean {
        return oldItem.statId == newItem.statId
    }

    override fun areContentsTheSame(oldItem: Stat, newItem: Stat): Boolean {
        return oldItem == newItem
    }
}