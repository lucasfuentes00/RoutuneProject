package com.example.layoutfinal.ui.loops

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.databinding.ItemSoundBinding
class SoundAdapter(private val sounds: List<Sound>) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    // ViewHolder class that holds the view for each item in the list
    inner class SoundViewHolder(val binding: ItemSoundBinding) : RecyclerView.ViewHolder(binding.root)

    // Create new views (called by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding = ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoundViewHolder(binding)
    }

    // Replace the contents of a view (called by the layout manager)
    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = sounds[position]
        // Bind your sound data to the view holder here
        holder.binding.soundName.text = sound.name // For example, assuming your Sound model has a 'name' property
        // Handle clicks or other actions if needed
    }

    // Return the size of your list
    override fun getItemCount(): Int = sounds.size
}