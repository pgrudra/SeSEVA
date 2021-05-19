package com.example.us0.adapters

import android.R
import android.content.ClipData.Item
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.PieChartLegendItem
import com.example.us0.data.apps.AppAndCategory
import com.example.us0.databinding.InstalledAppItemViewBinding
import com.example.us0.databinding.PieChartLegendItemViewBinding


class PieChartLegendAdapter: ListAdapter<PieChartLegendItem, PieChartLegendAdapter.ViewHolder>(
    PieChartLegendItemDiffCallback()
){


    class ViewHolder private constructor(val binding: PieChartLegendItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: PieChartLegendItem){
            binding.form.setBackgroundColor(item.formColor)
            binding.label.text=item.label
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= PieChartLegendItemViewBinding.inflate(layoutInflater,parent,false)
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

class PieChartLegendItemDiffCallback : DiffUtil.ItemCallback<PieChartLegendItem>() {
    override fun areItemsTheSame(oldItem: PieChartLegendItem, newItem: PieChartLegendItem): Boolean {
        return oldItem.label == newItem.label
    }

    override fun areContentsTheSame(
        oldItem: PieChartLegendItem,
        newItem: PieChartLegendItem
    ): Boolean {
        return oldItem == newItem
    }
}
