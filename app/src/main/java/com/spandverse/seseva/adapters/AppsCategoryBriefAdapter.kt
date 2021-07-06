package com.spandverse.seseva.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spandverse.seseva.CategoryRuleStatus
import com.spandverse.seseva.R
import com.spandverse.seseva.data.appcategories.AppsCategory
import com.spandverse.seseva.databinding.AppsCategoryBriefItemViewBinding

class AppsCategoryBriefAdapter(private val onCLickListener: OnClickListener):
    ListAdapter<AppsCategory, AppsCategoryBriefAdapter.ViewHolder>(AppsCategoryBriefDiffCallback()) {

    class ViewHolder private constructor(val binding: AppsCategoryBriefItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(item: AppsCategory) {
            if(item.categoryName.length>11){
                val context=binding.catName.context
                if(item.categoryName[0].toString()=="C"){
                    binding.catName.text=context.getString(R.string.com_n_brow_short)
                }
                else{
                    binding.catName.text=context.getString(
                        R.string.dots,item.categoryName.substring(
                            0,
                            11
                        )
                    )
                }
            }
            else{
                binding.catName.text=item.categoryName
            }
            when(item.ruleBroken){
                CategoryRuleStatus.BROKEN->{binding.statusSkrim.setBackgroundResource(R.drawable.broken_skrim)}
                CategoryRuleStatus.WARNING->{binding.statusSkrim.setBackgroundResource(R.drawable.warning_skrim)}
                else -> {binding.statusSkrim.visibility=View.GONE}
            }
            when(item.categoryName){
                "SOCIAL"->binding.catIcon.setImageResource(R.drawable.ic_social)
                "COMM. & BROWSING"->binding.catIcon.setImageResource(R.drawable.ic_communication)
                "GAMES"->binding.catIcon.setImageResource(R.drawable.ic_games)
                "VIDEO & COMICS"->binding.catIcon.setImageResource(R.drawable.ic_video)
                "ENTERTAINMENT"->binding.catIcon.setImageResource(R.drawable.ic_entertainment)
                "MSNBS"->binding.catIcon.setImageResource(R.drawable.ic_msnbs)
                "OTHERS"->binding.catIcon.setImageResource(R.drawable.ic_others)
                "WHITELISTED"->binding.catIcon.setImageResource(R.drawable.ic_whitelisted)
            }
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= AppsCategoryBriefItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onCLickListener.onCLick(item) }
        holder.bind(item)
    }
    class OnClickListener(val clickListener:(cat: AppsCategory)->Unit){
        fun onCLick(cat: AppsCategory)=clickListener(cat)
    }
}

class AppsCategoryBriefDiffCallback : DiffUtil.ItemCallback<AppsCategory>() {
    override fun areItemsTheSame(
        oldItem: AppsCategory,
        newItem: AppsCategory
    ): Boolean {
        return oldItem.categoryName == newItem.categoryName
    }

    override fun areContentsTheSame(
        oldItem: AppsCategory,
        newItem: AppsCategory
    ): Boolean {
        return oldItem == newItem
    }


}