package com.spandverse.seseva.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spandverse.seseva.PieChartLegendItem
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.PieChartLegendItemViewBinding
import java.util.*


class PieChartLegendAdapter: ListAdapter<PieChartLegendItem, PieChartLegendAdapter.ViewHolder>(
    PieChartLegendItemDiffCallback()
){


    class ViewHolder private constructor(val binding: PieChartLegendItemViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: PieChartLegendItem){
            binding.form.setBackgroundColor(item.formColor)
            val label=binding.label
            if(item.label.isNotEmpty()){
                if(item.label=="MSNBS"){
                    label.text=item.label
                }
                else{
                    label.text=label.context.getString(R.string.first_letter_uc,item.label.substring(0,1),item.label.substring(1).lowercase(
                        Locale.ROOT
                    )
                    )
                }
            }
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
