package com.example.layoutfinal.ui.games

import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R

class PerfectPitchFragment : Fragment(R.layout.fragment_perfect_pitch) {

    private lateinit var soundPool: SoundPool
    private val soundMap = HashMap<Int, Int>()
    private val noteSelection = BooleanArray(12) { true }

    private var isEditSelectionMode = false
    private var isRoundActive = false
    private var currentNote = -1
    private var lastNote: Int? = null
    private var startTime: Long = 0

    private var oldPlayVisibility = View.GONE
    private var oldPlayAgainVisibility = View.GONE
    private var oldPlayNextNoteVisibility = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val feedbackTextView = view.findViewById<TextView>(R.id.txt_feedback)
        val btnPlay = view.findViewById<Button>(R.id.btn_play)
        val btnPlayAgain = view.findViewById<Button>(R.id.btn_play_again)
        val btnPlayNextNote = view.findViewById<Button>(R.id.btn_play_next_note)
        val btnShowAnswer = view.findViewById<Button>(R.id.btn_show_answer)
        val btnEditSelection = view.findViewById<Button>(R.id.btn_edit_selection)
        val btnDone = view.findViewById<Button>(R.id.btn_done)
        val btnViewStatistics = view.findViewById<Button>(R.id.btn_view_statistics)
        val statsLayout = view.findViewById<View>(R.id.statistics_layout)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        for (i in 0 until 12) {
            val resId = resources.getIdentifier("note$i", "raw", requireContext().packageName)
            soundMap[i] = soundPool.load(requireContext(), resId, 1)
        }

        val noteButtonMap = mapOf(
            0 to R.id.key_C,
            1 to R.id.key_Csharp,
            2 to R.id.key_D,
            3 to R.id.key_Dsharp,
            4 to R.id.key_E,
            5 to R.id.key_F,
            6 to R.id.key_Fsharp,
            7 to R.id.key_G,
            8 to R.id.key_Gsharp,
            9 to R.id.key_A,
            10 to R.id.key_Asharp,
            11 to R.id.key_B,
        )

        for ((i, buttonId) in noteButtonMap) {
            val btnNote = view.findViewById<Button>(buttonId)
            btnNote?.let { button ->
                button.setOnClickListener {
                    if (isEditSelectionMode) {
                        noteSelection[i] = !noteSelection[i]
                        updateButtonAppearance(button, noteSelection[i])
                        return@setOnClickListener
                    }

                    if (!isRoundActive || !noteSelection[i]) return@setOnClickListener

                    val responseTime = SystemClock.elapsedRealtime() - startTime
                    if (i == currentNote) {
                        feedbackTextView.text = "¡Correct!"
                        feedbackTextView.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                        btnShowAnswer.visibility = View.GONE
                        currentNote = -1
                    } else {
                        feedbackTextView.text = "Incorrect"
                        feedbackTextView.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                        vibratePhone()
                        btnShowAnswer.visibility = View.VISIBLE
                    }

                    feedbackTextView.visibility = View.VISIBLE
                    isRoundActive = false
                    btnPlay.visibility = View.GONE
                    btnPlayAgain.visibility = View.VISIBLE
                    btnPlayNextNote.visibility = View.VISIBLE
                }

                updateButtonAppearance(button, noteSelection[i])
            }
        }

        btnPlay.setOnClickListener {
            startNewRound(view, feedbackTextView, btnPlay, btnPlayAgain, btnPlayNextNote, btnShowAnswer)
        }

        btnPlayAgain.setOnClickListener {
            lastNote?.let {
                soundPool.play(soundMap[it] ?: 0, 1f, 1f, 1, 0, 1f)
            }
        }

        btnPlayNextNote.setOnClickListener {
            startNewRound(view, feedbackTextView, btnPlay, btnPlayAgain, btnPlayNextNote, btnShowAnswer)
        }

        btnShowAnswer.setOnClickListener {
            val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val answerIndex = if (currentNote != -1) currentNote else lastNote
            answerIndex?.let {
                feedbackTextView.text = "${feedbackTextView.text}. La respuesta era: ${noteNames[it]}"
            }
            btnShowAnswer.visibility = View.GONE
        }

        btnEditSelection.setOnClickListener {
            isEditSelectionMode = true
            oldPlayVisibility = btnPlay.visibility
            oldPlayAgainVisibility = btnPlayAgain.visibility
            oldPlayNextNoteVisibility = btnPlayNextNote.visibility

            btnPlay.visibility = View.GONE
            btnPlayAgain.visibility = View.GONE
            btnPlayNextNote.visibility = View.GONE
            btnEditSelection.visibility = View.GONE
            btnDone.visibility = View.VISIBLE
        }

        btnDone.setOnClickListener {
            isEditSelectionMode = false
            btnDone.visibility = View.GONE
            btnEditSelection.visibility = View.VISIBLE
            btnPlay.visibility = oldPlayVisibility
            btnPlayAgain.visibility = oldPlayAgainVisibility
            btnPlayNextNote.visibility = oldPlayNextNoteVisibility
        }

        btnViewStatistics.setOnClickListener {
            statsLayout.visibility =
                if (statsLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun startNewRound(
        view: View,
        feedbackTextView: TextView,
        btnPlay: Button,
        btnPlayAgain: Button,
        btnPlayNextNote: Button,
        btnShowAnswer: Button
    ) {
        feedbackTextView.visibility = View.GONE
        isRoundActive = true
        btnPlayAgain.visibility = View.GONE
        btnPlayNextNote.visibility = View.GONE
        btnShowAnswer.visibility = View.GONE
        btnPlay.visibility = View.GONE

        val availableNotes = noteSelection.withIndex().filter { it.value }.map { it.index }
        if (availableNotes.isNotEmpty()) {
            currentNote = availableNotes.random()
            lastNote = currentNote
            startTime = SystemClock.elapsedRealtime()
            soundPool.play(soundMap[currentNote] ?: 0, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun vibratePhone() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val duration = 200L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun updateButtonAppearance(button: Button, isSelected: Boolean) {
        val id = button.id
        val isBlack = when (id) {
            R.id.key_Csharp, R.id.key_Dsharp, R.id.key_Fsharp,
            R.id.key_Gsharp, R.id.key_Asharp -> true
            else -> false
        }

        val drawableRes = when {
            isBlack && isSelected -> R.drawable.black_key_enabled
            isBlack && !isSelected -> R.drawable.black_key_disabled
            !isBlack && isSelected -> R.drawable.white_key_enabled
            else -> R.drawable.white_key_disabled
        }

        button.backgroundTintList = null
        button.background = ContextCompat.getDrawable(requireContext(), drawableRes)

        // 👇 Traer teclas negras al frente cada vez
        if (isBlack) {
            button.bringToFront()
        }

        (button.parent as? View)?.invalidate()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.release()
    }
}
