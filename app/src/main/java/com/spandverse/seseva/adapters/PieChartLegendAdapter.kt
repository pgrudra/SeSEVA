package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spandverse.seseva.PieChartLegendItem
import com.spandverse.seseva.databinding.PieChartLegendItemViewBinding


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
