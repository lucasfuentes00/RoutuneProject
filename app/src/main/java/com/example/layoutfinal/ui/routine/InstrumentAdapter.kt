package com.example.layoutfinal.ui.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class InstrumentAdapter(
    private val instruments: List<Instrument>,
    private val onItemClick: (Instrument) -> Unit,
    private val onDeleteClick: (Instrument) -> Unit
) : RecyclerView.Adapter<InstrumentAdapter.InstrumentViewHolder>() {

    inner class InstrumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val instrumentName: TextView = itemView.findViewById(R.id.instrumentName)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstrumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instrument, parent, false)
        return InstrumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: InstrumentViewHolder, position: Int) {
        val instrument = instruments[position]
        holder.instrumentName.text = instrument.name
        holder.itemView.setOnClickListener {
            onItemClick(instrument)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(instrument)
        }
    }

    override fun getItemCount(): Int = instruments.size
}
