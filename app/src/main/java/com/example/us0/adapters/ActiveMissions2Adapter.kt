package com.example.us0.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.data.missions.DomainActiveMission
import com.example.us0.databinding.AccomplishedMissionItemViewBinding
import com.example.us0.databinding.ActiveMissions2ItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ActiveMissions2Adapter(private val onCLickListener: OnClickListener): ListAdapter<DomainActiveMission, ActiveMissions2Adapter.ViewHolder>(
    DomainActiveMissionDiffCallback()
){

    class ViewHolder private constructor(
        val binding: AccomplishedMissionItemViewBinding,
    ) : RecyclerView.ViewHolder(binding.root){private val cloudImagesReference= Firebase.storage

        fun bind(item: DomainActiveMission){
            binding.missionCategory.text=item.category
            binding.missionName.text=item.missionName
            binding.sponsorName.text=binding.sponsorName.context.getString(R.string.sponsored_by_sponsor_name,item.sponsorName)
            binding.youRaised.text=item.contribution.toString()
            binding.totalRaised.text=item.totalMoneyRaised.toString()
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${item.missionNumber}Image.jpg")
            Glide.with(binding.missionImage.context)
                .load(reference)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground))
                .into(binding.missionImage)
            binding.reportSymbol.visibility=View.GONE
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding= AccomplishedMissionItemViewBinding.inflate(layoutInflater, parent, false)
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
/*class ActiveMissions2DiffCallback : DiffUtil.ItemCallback<DomainActiveMission>() {
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


}*/
