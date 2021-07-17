package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.spandverse.seseva.R
import com.spandverse.seseva.data.sponsors.Sponsor
import com.spandverse.seseva.databinding.AllSponsorsItemViewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AllSponsorsAdapter(private val onCLickListener: AllSponsorsAdapter.OnClickListener): ListAdapter<Sponsor, AllSponsorsAdapter.ViewHolder>(
    SponsorDiffCallback()
) {
    class ViewHolder private constructor(val binding: AllSponsorsItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ){private val cloudImagesReference= Firebase.storage

        fun bind(item: Sponsor){
            val reference=cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${item.sponsorNumber}Logo.png")
            Glide.with(binding.sponsorLogo.context)
                .load(reference)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_sponsor)
                        .error(R.drawable.ic_sponsor))
                .into(binding.sponsorLogo)
            if(item.sponsoredAmount!=0){
                binding.sponsoredAmount.text= binding.sponsoredAmount.context.getString(R.string.rs,item.sponsoredAmount)
            }
            else{
                binding.sponsoredAmount.visibility=View.GONE
                binding.sponsoredText.text=binding.sponsoredText.context.getString(R.string.sponsoring_an_ongoing_mission)
            }
            binding.sponsorName.text=item.sponsorName
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
    class OnClickListener(val clickListener:(sponsor: Sponsor)->Unit){
        fun onCLick(sponsor: Sponsor)=clickListener(sponsor)
    }
}
class SponsorDiffCallback(): DiffUtil.ItemCallback<Sponsor>() {
    override fun areItemsTheSame(
        oldItem: Sponsor,
        newItem: Sponsor
    ): Boolean {
        return oldItem.sponsorNumber== newItem.sponsorNumber
    }

    override fun areContentsTheSame(
        oldItem: Sponsor,
        newItem: Sponsor
    ): Boolean {
        return oldItem == newItem
    }
}

