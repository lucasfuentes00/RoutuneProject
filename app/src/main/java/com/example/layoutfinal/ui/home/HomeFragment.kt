package com.example.layoutfinal.ui.home

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

class HomeFragment : Fragment() {

    private lateinit var metronomeFrame: ImageView
    private lateinit var metronomeBar: ImageView
    private lateinit var bpmTextView: TextView
    private lateinit var bpmSeekBar: SeekBar
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private lateinit var mediaPlayer: MediaPlayer
    private var bpm: Int = 60
    private val minBpm = 60
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

        metronomeFrame = view.findViewById(R.id.metronomeFrame)
        metronomeBar = view.findViewById(R.id.metronomeBar)

        // Pivote inferior para que rote como péndulo
        metronomeBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                metronomeBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                metronomeBar.pivotX = metronomeBar.width / 2f
                metronomeBar.pivotY = metronomeBar.height.toFloat()
            }
        })

        bpmTextView = view.findViewById(R.id.bpmTextView)
        bpmSeekBar = view.findViewById(R.id.bpmSeekBar)
        playButton = view.findViewById(R.id.playButton)
        stopButton = view.findViewById(R.id.stopButton)

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
                startMetronomeAnimation()
                mediaPlayer.start()
            }
        }

        stopButton.setOnClickListener {
            stopMetronomeAnimation()
            mediaPlayer.pause()
        }

        return view
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(context, R.raw.tic_tac_sound).apply {
            isLooping = true
        }
    }

    private fun updateBpm() {
        bpmTextView.text = "$bpm BPM"
        val newSpeed = bpm.toFloat() / 60f
        try {
            mediaPlayer.setPlaybackParams(mediaPlayer.playbackParams.setSpeed(newSpeed))
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error setting playback speed", e)
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

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        mediaPlayer.release()
        stopMetronomeAnimation()
    }
}
