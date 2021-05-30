package com.example.us0.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.databinding.AllMissionsItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class AllMissionsAdapter(private val onCLickListener: AllMissionsAdapter.OnClickListener): ListAdapter<DomainActiveMission, AllMissionsAdapter.ViewHolder>(
    DomainActiveMissionDiffCallback()
) {
    class ViewHolder private constructor(val binding: AllMissionsItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ){private val cloudImagesReference= Firebase.storage

        fun bind(item: DomainActiveMission){
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${item.missionNumber}Image.jpg")
            binding.missionCategory.text=item.category
            binding.missionName.text=item.missionName
            binding.totalMoneyRaised.text=item.totalMoneyRaised.toString()
            binding.contributors.text=item.usersActive.toString()
            Glide.with(binding.missionImage.context)
                .load(reference)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground))
                .into(binding.missionImage)
            val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
            if(item.deadline<nowMinusOneDay) {
                if (item.reportAvailable) {
                    binding.activeSymbol.setImageResource(R.drawable.ic_report_available)
                }
                else{
                    binding.activeSymbol.setImageResource(R.drawable.ic_report_in_progress)
                }
            }

        }

        companion object {
            fun from(parent: ViewGroup): AllMissionsAdapter.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= AllMissionsItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AllMissionsAdapter.ViewHolder.from(parent)
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
