package com.example.us0.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.databinding.NonSelectedMissionItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class NonSelectedMissionsAdapter(private val onCLickListener: OnClickListener): ListAdapter<DomainActiveMission, NonSelectedMissionsAdapter.ViewHolder>(
    NonSelectedMissionDiffCallback()
){

    class ViewHolder private constructor(val binding: NonSelectedMissionItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ){private val cloudImagesReference= Firebase.storage

        fun bind(item: DomainActiveMission){
           /* *//* binding.appName.text=item.appName
             binding.packageName.text=item.packageName
             binding.appCategory.text=item.appCategory*//*
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${item.missionNumber}Image.jpg")
            binding.availableTill.text=item.deadlineAsDate
            binding.money.text=item.totalMoneyRaised.toString()
            binding.sponsorName.text=item.sponsorName
            binding.category.text=item.category
            binding.missionName.text=item.missionName
            binding.activeContributors.text=item.usersActive.toString()
            Glide.with(binding.imageView.context)
                .load(reference)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground))
                .into(binding.imageView)*/


        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= NonSelectedMissionItemViewBinding.inflate(layoutInflater, parent, false)
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
    class OnClickListener(val clickListener:(mission:DomainActiveMission)->Unit){
        fun onCLick(mission:DomainActiveMission)=clickListener(mission)
    }
}
class NonSelectedMissionDiffCallback : DiffUtil.ItemCallback<DomainActiveMission>() {
    override fun areItemsTheSame(
        oldItem: DomainActiveMission,
        newItem: DomainActiveMission
    ): Boolean {
        return oldItem.missionNumber == newItem.missionNumber
    }

    override fun areContentsTheSame(
        oldItem: DomainActiveMission,
        newItem: DomainActiveMission
    ): Boolean {
        return oldItem == newItem
    }


}
