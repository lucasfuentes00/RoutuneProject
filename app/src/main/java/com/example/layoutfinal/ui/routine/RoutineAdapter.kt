package com.example.layoutfinal.ui.routine

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class RoutineAdapter(
    var routines: MutableList<Routine>,
    private val onDeleteClick: (Routine) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    companion object {
        private const val TAG = "RoutineAdapter"
    }

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineName: TextView = itemView.findViewById(R.id.instrumentName)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instrument, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]
        Log.d(TAG, "onBindViewHolder: binding routine at position $position with name: ${routine.name}")

        holder.routineName.text = routine.name

        holder.deleteButton.setOnClickListener {
            Log.d(TAG, "Delete clicked for routine: ${routine.name}")
            onDeleteClick(routine)
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${routines.size}")
        return routines.size
    }
}
