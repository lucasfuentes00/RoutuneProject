package com.example.layoutfinal  // Asegúrate de que este paquete sea el correcto

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils

class MetronomoFragment : Fragment() {

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

        // Iniciar o detener el sonido cuando se presiona el botón
        playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
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
