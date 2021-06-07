package com.spandverse.seseva.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.spandverse.seseva.R

class OnBoardingAdapter: RecyclerView.Adapter<OnBoardingAdapter.ViewHolder>() {

    class ViewHolder private constructor(onBoardingImageView: View):RecyclerView.ViewHolder(onBoardingImageView){
        private val imgView:ImageView=onBoardingImageView.findViewById(R.id.imageViewOnBoarding)
        fun bind(position:Int){
            imgView.setImageResource(when(position){
                0->R.drawable.ic_launcher_foreground
                else->R.drawable.ic_launcher_background
            })
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.login_horizontal_item_view, parent, false)

                return ViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount()=4


}