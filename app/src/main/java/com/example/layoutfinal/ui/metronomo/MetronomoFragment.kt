package com.example.layoutfinal.ui.metronomo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R

class MetronomoFragment : Fragment() {

    private lateinit var metronomeFrame: ImageView
    private lateinit var metronomeBar: ImageView
    private lateinit var bpmTextView: TextView
    private lateinit var bpmSeekBar: SeekBar
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private var mediaPlayer: MediaPlayer? = null
    private var bpm: Int = 60
    private val minBpm = 30
    private val maxBpm = 200

    private val handler = Handler(Looper.getMainLooper())
    private var isAnimating = false
    private var currentDirection = 1f
    private var currentAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_metronomo, container, false)

        // Referencias UI
        metronomeFrame = view.findViewById(R.id.metronomeFrame)
        metronomeBar = view.findViewById(R.id.metronomeBar)
        bpmTextView = view.findViewById(R.id.bpmTextView)
        bpmSeekBar = view.findViewById(R.id.bpmSeekBar)
        playButton = view.findViewById(R.id.playButton)
        stopButton = view.findViewById(R.id.stopButton)

        // Animación de entrada
        animateInView(metronomeFrame, 0)
        animateInView(metronomeBar, 100)
        animateInView(bpmTextView, 200)
        animateInView(bpmSeekBar, 300)
        animateInView(playButton, 400)
        animateInView(stopButton, 500)

        // Configuración pivote de barra
        metronomeBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                metronomeBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                metronomeBar.pivotX = metronomeBar.width / 2f
                metronomeBar.pivotY = metronomeBar.height.toFloat()
            }
        })

        initializeMediaPlayer()
        bpmTextView.text = "$bpm BPM"

        bpmSeekBar.max = maxBpm - minBpm
        bpmSeekBar.progress = bpm - minBpm

        bpmSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                bpm = progress + minBpm
                updateBpm()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        playButton.setOnClickListener {
            if (!isAnimating) {
                ensureMediaPlayerReady()
                updateBpm()
                startMetronomeAnimation()
                mediaPlayer?.start()
            }
        }

        stopButton.setOnClickListener {
            stopMetronomeAnimation()
            mediaPlayer?.pause()
        }

        return view
    }

    private fun animateInView(view: View, delay: Long) {
        view.translationY = -50f
        view.alpha = 0f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .setStartDelay(delay)
            .start()
    }

    private fun initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.tic_tac_sound).apply {
                isLooping = true
            }
        }
    }

    private fun ensureMediaPlayerReady() {
        if (mediaPlayer == null) {
            initializeMediaPlayer()
        }
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.seekTo(0)
        }
    }

    private fun updateBpm() {
        bpmTextView.text = "$bpm BPM"
        val newSpeed = bpm.toFloat() / 60f
        try {
            mediaPlayer?.playbackParams?.let {
                mediaPlayer?.setPlaybackParams(it.setSpeed(newSpeed))
            }
        } catch (e: Exception) {
            Log.e("MetronomeFragment", "Error setting playback speed", e)
        }
    }

    private fun startMetronomeAnimation() {
        isAnimating = true
        currentDirection = 1f
        animateSwing()
    }

    private fun animateSwing() {
        if (!isAnimating) return

        val interval = (60000f / bpm).toLong()
        val fromDeg = metronomeBar.rotation
        val toDeg = if (currentDirection > 0) 30f else -30f

        currentAnimator = ObjectAnimator.ofFloat(metronomeBar, "rotation", fromDeg, toDeg).apply {
            duration = interval
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isAnimating) {
                        currentDirection *= -1
                        animateSwing()
                    }
                }
            })
        }

        currentAnimator?.start()
    }

    private fun stopMetronomeAnimation() {
        isAnimating = false
        handler.removeCallbacksAndMessages(null)
        currentAnimator?.cancel()

        val reset = ObjectAnimator.ofFloat(metronomeBar, "rotation", metronomeBar.rotation, 0f).apply {
            duration = 300
            interpolator = LinearInterpolator()
        }

        reset.start()
    }

    override fun onPause() {
        super.onPause()
        stopMetronomeAnimation()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMetronomeAnimation()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
