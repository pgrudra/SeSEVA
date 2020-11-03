package com.example.us0

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.us0.data.AppAndCategory

class InstalledAppAdapter:RecyclerView.Adapter<InstalledAppAdapter.ViewHolder>(){
    var data=listOf<AppAndCategory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val packageName: TextView = itemView.findViewById(R.id.package_name)
    }
    override fun getItemCount()=data.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=data[position]
        holder.appName.text=item.appName
        holder.packageName.text=item.packageName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.installed_app_item_view, parent, false)
        return ViewHolder(view)
    }

}