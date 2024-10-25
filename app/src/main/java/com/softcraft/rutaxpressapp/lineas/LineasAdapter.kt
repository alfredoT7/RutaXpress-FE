package com.softcraft.rutaxpressapp.lineas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.softcraft.rutaxpressapp.R

class LineasAdapter(private val onItemClick: (LineaResponse) -> Unit) : ListAdapter<LineaResponse, LineasAdapter.LineaViewHolder>(LineaDiffCallback()) {

    class LineaViewHolder(itemView: View, val onItemClick: (LineaResponse) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvLineaID: TextView = itemView.findViewById(R.id.tvLineaID)
        private val tvLineaDescription: TextView = itemView.findViewById(R.id.tvLineaDescription)
        private var currentLinea: LineaResponse? = null

        init {
            itemView.setOnClickListener {
                currentLinea?.let { onItemClick(it) }
            }
        }

        fun bind(linea: LineaResponse) {
            currentLinea = linea
            tvLineaID.text = linea.routeId
            tvLineaDescription.text = linea.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lineas_card, parent, false)
        return LineaViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: LineaViewHolder, position: Int) {
        holder.bind(getItem(position))
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