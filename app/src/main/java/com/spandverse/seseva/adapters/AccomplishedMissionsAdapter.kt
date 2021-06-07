package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.databinding.AccomplishedMissionItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AccomplishedMissionsAdapter(private val onCLickListener: AccomplishedMissionsAdapter.OnClickListener): ListAdapter<DomainActiveMission, AccomplishedMissionsAdapter.ViewHolder>(
    DomainActiveMissionDiffCallback()
) {
    class ViewHolder private constructor(val binding: AccomplishedMissionItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ){private val cloudImagesReference= Firebase.storage

        fun bind(item: DomainActiveMission){
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${item.missionNumber}Image.jpg")
            binding.missionCategory.text=item.category
            binding.missionName.text=item.missionName
            binding.totalRaised.text=item.totalMoneyRaised.toString()
            binding.youRaised.text=item.contribution.toString()
            binding.sponsorName.text= binding.sponsorName.context.getString(R.string.sponsored_by_sponsor_name,item.sponsorName)
            Glide.with(binding.missionImage.context)
                .load(reference)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground))
                .into(binding.missionImage)
            if(item.reportAvailable){
                binding.reportSymbol.setImageResource(R.drawable.ic_report_available)
            }

        }

        companion object {
            fun from(parent: ViewGroup): AccomplishedMissionsAdapter.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= AccomplishedMissionItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AccomplishedMissionsAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onCLickListener.onCLick(item) }
        holder.bind(item)
    }
    class OnClickListener(val clickListener:(mission: DomainActiveMission)->Unit){
        fun onCLick(mission: DomainActiveMission)=clickListener(mission)
    }
}