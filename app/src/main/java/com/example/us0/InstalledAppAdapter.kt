package com.example.us0

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.data.AppAndCategory
import com.example.us0.databinding.InstalledAppItemViewBinding

class InstalledAppAdapter: ListAdapter<AppAndCategory, InstalledAppAdapter.ViewHolder>(AppAndCategoryDiffCallback()){


    class ViewHolder private constructor(val binding: InstalledAppItemViewBinding) : RecyclerView.ViewHolder(binding.root){

fun bind(item:AppAndCategory){
    binding.appName.text=item.appName
    binding.packageName.text=item.packageName
    binding.appCategory.text=item.appCategory

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
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