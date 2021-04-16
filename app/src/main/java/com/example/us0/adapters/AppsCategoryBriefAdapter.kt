package com.example.us0.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.CategoryRuleStatus
import com.example.us0.R
import com.example.us0.data.appcategories.AppsCategory
import com.example.us0.databinding.ActiveMissionItemViewBinding
import com.example.us0.databinding.AppsCategoryBriefItemViewBinding

class AppsCategoryBriefAdapter(private val onCLickListener: OnClickListener):
    ListAdapter<AppsCategory, AppsCategoryBriefAdapter.ViewHolder>(AppsCategoryBriefDiffCallback()) {

    class ViewHolder private constructor(val binding: AppsCategoryBriefItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(item: AppsCategory) {
binding.catName.text=item.categoryName
            when(item.ruleBroken){
                CategoryRuleStatus.BROKEN->{binding.statusSkrim.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)}
                CategoryRuleStatus.WARNING->{binding.statusSkrim.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)}
                CategoryRuleStatus.SAFE->{binding.statusSkrim.visibility= View.GONE}
            }
            when(item.categoryName){
                "SOCIAL"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "COMMUNICATION"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "GAMES"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "VIDEO"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "ENTERTAINMENT"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "MSNBS"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "OTHERS"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
                "WHITELISTED"->binding.catIcon.setImageResource(R.drawable.ic__dummy_category_icon)
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