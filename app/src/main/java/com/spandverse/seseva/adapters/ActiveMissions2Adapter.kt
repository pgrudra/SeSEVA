package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.spandverse.seseva.R
import com.spandverse.seseva.data.missions.DomainActiveMission
import com.spandverse.seseva.databinding.AccomplishedMissionItemViewBinding
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
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_imageplaceholder)
                        .error(R.drawable.ic_imageplaceholder))
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
