package com.example.us0.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.us0.R
import com.example.us0.data.SponsorCardContents
import com.example.us0.databinding.AllSponsorsItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AllSponsorsAdapter(private val onCLickListener: AllSponsorsAdapter.OnClickListener): ListAdapter<SponsorCardContents, AllSponsorsAdapter.ViewHolder>(
    SponsorCardContentsDiffCallback()
) {
    class ViewHolder private constructor(val binding: AllSponsorsItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ){private val cloudImagesReference= Firebase.storage

        fun bind(item: SponsorCardContents){
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/mission${item.sponsoredMissionNumbers[0]}SponsorLogo.png")
            Glide.with(binding.sponsorLogo.context)
                .load(reference)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground))
                .into(binding.sponsorLogo)
            var sponsoredMissionsText="Sponsored Rs${item.totalMoneySponsored} for\n"
            for(i in item.sponsoredMissions){
                sponsoredMissionsText+="$i,\n"
            }
            binding.sponsoredMoneyAndMissiions.text=sponsoredMissionsText
        }

        companion object {
            fun from(parent: ViewGroup): AllSponsorsAdapter.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding= AllSponsorsItemViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AllSponsorsAdapter.ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onCLickListener.onCLick(item) }
        holder.bind(item)
    }
    class OnClickListener(val clickListener:(sponsor: SponsorCardContents)->Unit){
        fun onCLick(sponsor: SponsorCardContents)=clickListener(sponsor)
    }
}
class SponsorCardContentsDiffCallback(): DiffUtil.ItemCallback<SponsorCardContents>() {
    override fun areItemsTheSame(
        oldItem: SponsorCardContents,
        newItem: SponsorCardContents
    ): Boolean {
        return oldItem.sponsorName== newItem.sponsorName
    }

    override fun areContentsTheSame(
        oldItem: SponsorCardContents,
        newItem: SponsorCardContents
    ): Boolean {
        return oldItem == newItem
    }
}

