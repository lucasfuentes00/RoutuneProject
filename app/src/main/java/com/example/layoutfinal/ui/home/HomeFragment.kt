package com.example.layoutfinal.ui.home

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.layoutfinal.R
import com.example.layoutfinal.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var metronomoImage: ImageView
    private lateinit var playButton: Button
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_metronomo, container, false)

        // Configurar la imagen y el botón
        metronomoImage = view.findViewById(R.id.metronomoImage)
        playButton = view.findViewById(R.id.playButton)

        // Cargar la animación de rotación
        val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_metronome)
        metronomoImage.startAnimation(rotateAnimation)

        // Cargar el archivo de sonido
        mediaPlayer = MediaPlayer.create(context, R.raw.tic_tac_sound)

        // Habilitar el loop infinito
        mediaPlayer.isLooping = true

        // Iniciar o detener el sonido cuando se presiona el botón
        playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()  // Pausa el sonido
            } else {
                mediaPlayer.start()  // Reproduce el sonido
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos al destruir el fragmento
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}