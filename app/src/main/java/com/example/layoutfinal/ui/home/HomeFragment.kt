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
import com.example.layoutfinal.R

class HomeFragment : Fragment() {

    private lateinit var metronomoImage: ImageView
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private lateinit var increaseButton: Button
    private lateinit var decreaseButton: Button
    private lateinit var bpmTextView: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private var speed: Float = 1.0f  // Velocidad del metrónomo (1.0 es normal)
    private var bpm: Int = 120  // BPM por defecto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_metronomo, container, false)

        // Configurar la imagen, botones y texto
        metronomoImage = view.findViewById(R.id.metronomoImage)
        playButton = view.findViewById(R.id.playButton)
        stopButton = view.findViewById(R.id.stopButton)
        increaseButton = view.findViewById(R.id.increaseButton)
        decreaseButton = view.findViewById(R.id.decreaseButton)
        bpmTextView = view.findViewById(R.id.bpmTextView)

        // Cargar la animación de balanceo
        val balanceAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_metronome)

        // Configurar el MediaPlayer con el archivo de sonido
        mediaPlayer = MediaPlayer.create(context, R.raw.tic_tac_sound)
        mediaPlayer.isLooping = true

        // Mostrar el BPM inicial
        bpmTextView.text = "$bpm BPM"

        // Iniciar o detener el sonido y animación cuando se presiona el botón "Start"
        playButton.setOnClickListener {
            metronomoImage.startAnimation(balanceAnimation)  // Iniciar animación
            mediaPlayer.start()  // Reproducir sonido
        }

        // Detener el sonido y animación cuando se presiona el botón "Stop"
        stopButton.setOnClickListener {
            metronomoImage.clearAnimation()  // Detener animación
            mediaPlayer.pause()  // Pausar sonido
        }

        // Acelerar el sonido cuando se presiona el botón "+"
        increaseButton.setOnClickListener {
            if (bpm < 240) {
                bpm += 5
                updateBpm()
            }
        }

        // Desacelerar el sonido cuando se presiona el botón "-"
        decreaseButton.setOnClickListener {
            if (bpm > 40) {
                bpm -= 5
                updateBpm()
            }
        }

        return view
    }

    private fun updateBpm() {
        bpmTextView.text = "$bpm BPM"
        // Calcular la nueva velocidad basada en el BPM
        val newSpeed = bpm.toFloat() / 120f  // Ajuste de velocidad relativo a 120 BPM
        mediaPlayer.setPlaybackParams(mediaPlayer.playbackParams.setSpeed(newSpeed))
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