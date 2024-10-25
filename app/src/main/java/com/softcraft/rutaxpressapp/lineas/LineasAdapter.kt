package com.softcraft.rutaxpressapp.lineas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.softcraft.rutaxpressapp.R

class LineasAdapter : ListAdapter<LineaResponse, LineasAdapter.LineaViewHolder>(LineaDiffCallback()) {

    class LineaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLineaID: TextView = itemView.findViewById(R.id.tvLineaID)
        val tvLineaDescription: TextView = itemView.findViewById(R.id.tvLineaDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lineas_card, parent, false)
        return LineaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineaViewHolder, position: Int) {
        val linea = getItem(position)
        holder.tvLineaID.text = linea.routeId
        holder.tvLineaDescription.text = linea.description
    }

    class LineaDiffCallback : DiffUtil.ItemCallback<LineaResponse>() {
        override fun areItemsTheSame(oldItem: LineaResponse, newItem: LineaResponse): Boolean {
            return oldItem.routeId == newItem.routeId
        }

        override fun areContentsTheSame(oldItem: LineaResponse, newItem: LineaResponse): Boolean {
            return oldItem == newItem
        }
    }

}
