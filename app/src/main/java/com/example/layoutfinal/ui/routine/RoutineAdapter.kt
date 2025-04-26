package com.example.layoutfinal.ui.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class RoutineAdapter(
    private val routines: List<Routine>,
    private val onDeleteClick: (Routine) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineName: TextView = itemView.findViewById(R.id.instrumentName)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instrument, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]
        holder.routineName.text = routine.name

        holder.deleteButton.setOnClickListener {
            onDeleteClick(routine)
        }
    }

    override fun getItemCount() = routines.size
}
