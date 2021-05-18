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
import com.example.us0.databinding.PlainMissionCardItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PlainMissionsCardAdapter(private val onCLickListener: OnClickListener): ListAdapter<DomainActiveMission, PlainMissionsCardAdapter.ViewHolder>(
    PlainMissionsCardDiffCallback()
){

    class ViewHolder private constructor(val binding: PlainMissionCardItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ){private val cloudImagesReference= Firebase.storage

        fun bind(item: DomainActiveMission){
            binding.missionCategory.text=item.category
            binding.missionName.text=item.missionName
            binding.sponsor.text="Sponsored by ${item.sponsorName}"
            binding.contribution.text=item.contribution.toString()
            binding.totalMoneyRaised.text=item.totalMoneyRaised.toString()
            binding.contributors.text=item.usersActive.toString()
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${item.missionNumber}Image.jpg")
            Glide.with(binding.missionImage.context)
                .load(reference)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground))
                .into(binding.missionImage)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= PlainMissionCardItemViewBinding.inflate(layoutInflater, parent, false)
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
class PlainMissionsCardDiffCallback : DiffUtil.ItemCallback<DomainActiveMission>() {
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
