package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spandverse.seseva.data.apps.AppAndCategory
import com.spandverse.seseva.databinding.InstalledAppItemViewBinding

class InstalledAppAdapter: ListAdapter<AppAndCategory, InstalledAppAdapter.ViewHolder>(
    AppAndCategoryDiffCallback()
){


    class ViewHolder private constructor(val binding: InstalledAppItemViewBinding) : RecyclerView.ViewHolder(binding.root){

fun bind(item: AppAndCategory){
    binding.appName.text=item.appName
    binding.appIcon.setImageDrawable(binding.appIcon.context.packageManager.getApplicationIcon(item.packageName))
}

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding=InstalledAppItemViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }




}

class AppAndCategoryDiffCallback : DiffUtil.ItemCallback<AppAndCategory>() {
    override fun areItemsTheSame(oldItem: AppAndCategory, newItem: AppAndCategory): Boolean {
        return oldItem.appId == newItem.appId
    }

    override fun areContentsTheSame(oldItem: AppAndCategory, newItem: AppAndCategory): Boolean {
        return oldItem == newItem
    }
}