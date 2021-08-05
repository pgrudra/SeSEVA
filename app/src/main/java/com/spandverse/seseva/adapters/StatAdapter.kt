package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spandverse.seseva.data.stats.Stat
import com.spandverse.seseva.databinding.InstalledAppItemViewBinding

class StatAdapter(private val onClickListener: OnClickListener): ListAdapter<Stat, StatAdapter.ViewHolder>(
    StatDiffCallback()
){


    class ViewHolder private constructor(val binding:InstalledAppItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Stat){
            binding.appName.text=item.appName
            try{
                binding.appIcon.setImageDrawable(binding.appIcon.context.packageManager.getApplicationIcon(item.packageName!!))
            }catch(e:Exception){}
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