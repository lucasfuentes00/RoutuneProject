package com.example.layoutfinal.ui.loops

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R
import com.example.layoutfinal.databinding.ItemSoundBinding

class SoundAdapter(
    private val soundList: List<Sound>,
    private val onPlayClickListener: (Sound) -> Unit,
    private val onDownloadClickListener: (Sound) -> Unit
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.soundName)
        val durationTextView: TextView = view.findViewById(R.id.soundDuration)
        val playButton: Button = view.findViewById(R.id.playButtonItem)
        val downloadButton: Button = view.findViewById(R.id.downloadButtonItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sound_item, parent, false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = soundList[position]
        holder.nameTextView.text = sound.name
        holder.durationTextView.text = "Duration: ${sound.duration}s"

        holder.playButton.setOnClickListener {
            onPlayClickListener(sound)
        }

        holder.downloadButton.setOnClickListener {
            onDownloadClickListener(sound)
        }
    }

    override fun getItemCount(): Int {
        return soundList.size
    }
}