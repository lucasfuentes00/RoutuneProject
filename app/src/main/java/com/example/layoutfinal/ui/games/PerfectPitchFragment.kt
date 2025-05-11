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
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class PerfectPitchFragment : Fragment(R.layout.fragment_perfect_pitch) {

    private lateinit var soundPool: SoundPool
    private val soundMap = HashMap<Int, Int>()
    private val noteSelection = BooleanArray(12) { true }

    private var isEditSelectionMode = false
    private var isRoundActive = false
    private var currentNote = -1
    private var lastSoundIndex = -1

    private var lastNote: Int? = null
    private var startTime: Long = 0

    private var oldPlayVisibility = View.GONE
    private var oldPlayAgainVisibility = View.GONE
    private var oldPlayNextNoteVisibility = View.GONE

    private var isLowerOctaveEnabled = false
    private var isCentralOctaveEnabled = true
    private var isUpperOctaveEnabled = false

    private lateinit var chartAccuracy: BarChart
    private lateinit var chartResponseTime: BarChart
    private lateinit var statsLayout: View

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
        val btnResetStatistics = view.findViewById<Button>(R.id.btn_reset_statistics)

        chartAccuracy = view.findViewById(R.id.chart_accuracy)
        chartResponseTime = view.findViewById(R.id.chart_response_time)
        statsLayout = view.findViewById(R.id.statistics_layout)


        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val btnLowerOctave = view.findViewById<Button>(R.id.btn_lower_octave)
        val btnCentralOctave = view.findViewById<Button>(R.id.btn_central_octave)
        val btnUpperOctave = view.findViewById<Button>(R.id.btn_upper_octave)

        val names = listOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")

// Octave toggles
        btnLowerOctave.setOnClickListener {
            if (canDeactivateOctave(isLowerOctaveEnabled)) {
                isLowerOctaveEnabled = !isLowerOctaveEnabled
                updateOctaveButtonAppearance(btnLowerOctave, isLowerOctaveEnabled)
            }
        }
        btnCentralOctave.setOnClickListener {
            if (canDeactivateOctave(isCentralOctaveEnabled)) {
                isCentralOctaveEnabled = !isCentralOctaveEnabled
                updateOctaveButtonAppearance(btnCentralOctave, isCentralOctaveEnabled)
            }
        }
        btnUpperOctave.setOnClickListener {
            if (canDeactivateOctave(isUpperOctaveEnabled)) {
                isUpperOctaveEnabled = !isUpperOctaveEnabled
                updateOctaveButtonAppearance(btnUpperOctave, isUpperOctaveEnabled)
            }
        }



        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundMap.clear()
        for (i in 0..36) {
            val resId = resources.getIdentifier("note$i", "raw", requireContext().packageName)
            if (resId != 0) {
                soundMap[i] = soundPool.load(requireContext(), resId, 1)
            } else {
                Log.w("SoundLoad", "Archivo de sonido note$i no encontrado")
            }
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


        for ((i, btnId) in noteButtonMap) {
            val button = view.findViewById<Button>(btnId)!!
            // listener
            button.setOnClickListener {
                if (isEditSelectionMode) {
                    noteSelection[i] = !noteSelection[i]
                    updateButtonAppearance(button, noteSelection[i])
                    return@setOnClickListener
                }
                if (!isRoundActive || !noteSelection[i]) return@setOnClickListener

                val responseTime = SystemClock.elapsedRealtime() - startTime
                val wasCorrect   = (i == currentNote)

                if (wasCorrect) {
                    feedbackTextView.text = "¡Correct!"
                    feedbackTextView.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                    btnShowAnswer.visibility = View.GONE
                } else {
                    feedbackTextView.text = "Incorrect"
                    feedbackTextView.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                    vibratePhone()
                    btnShowAnswer.visibility = View.VISIBLE
                }
                onUserAnswered(wasCorrect, names[i], responseTime)

                feedbackTextView.visibility = View.VISIBLE
                isRoundActive = false
                btnPlay.visibility = View.GONE
                btnPlayAgain.visibility = View.VISIBLE
                btnPlayNextNote.visibility = View.VISIBLE

                if (wasCorrect) {
                    currentNote = -1
                }
            }
            updateButtonAppearance(button, noteSelection[i])
        }

        btnPlay.setOnClickListener {
            startNewRound(view, feedbackTextView, btnPlay, btnPlayAgain, btnPlayNextNote, btnShowAnswer)
        }

        btnPlayAgain.setOnClickListener {
            soundPool.play(soundMap[lastSoundIndex] ?: 0, 1f, 1f, 1, 0, 1f)
        }

        btnPlayNextNote.setOnClickListener {
            startNewRound(view, feedbackTextView, btnPlay, btnPlayAgain, btnPlayNextNote, btnShowAnswer)
        }

        btnShowAnswer.setOnClickListener {
            val idx = if (currentNote != -1) currentNote else (lastSoundIndex % 12)
            feedbackTextView.text = "La respuesta era: ${names[idx]}"
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
            if (statsLayout.visibility != View.VISIBLE) {
                loadStatisticsIntoCharts()
                statsLayout.visibility = View.VISIBLE
                btnResetStatistics.visibility = View.VISIBLE
            } else {
                statsLayout.visibility = View.GONE
                btnResetStatistics.visibility = View.GONE
            }
        }

        btnResetStatistics.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("stats", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            loadStatisticsIntoCharts()
        }

        requireActivity().title = "Perfect Pitch"

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)

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
        val activeOctaves = mutableListOf<Int>()
        if (isLowerOctaveEnabled) activeOctaves.add(0)
        if (isCentralOctaveEnabled) activeOctaves.add(1)
        if (isUpperOctaveEnabled) activeOctaves.add(2)

        if (availableNotes.isNotEmpty() && activeOctaves.isNotEmpty()) {
            val noteIndex      = availableNotes.random()    // 0–11
            val selectedOctave = activeOctaves.random()     // 0,1 o 2

            currentNote = noteIndex

            // Calcula el fichero que toca
            lastSoundIndex = noteIndex + selectedOctave * 12  // 0…36
            startTime = SystemClock.elapsedRealtime()


            soundPool.play(soundMap[lastSoundIndex] ?: 0, 1f, 1f, 1, 0, 1f)
        }
    }


    private fun onUserAnswered(correct: Boolean, note: String, responseTimeSec: Long) {
        val prefs = requireContext().getSharedPreferences("stats", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            val totalKey   = "${note}_total"
            val correctKey = "${note}_correct"
            val timeKey    = "${note}_timeSum"

            putInt(totalKey, prefs.getInt(totalKey, 0) + 1)
            if (correct) {
                putInt(correctKey, prefs.getInt(correctKey, 0) + 1)
                putLong(timeKey, prefs.getLong(timeKey, 0L) + responseTimeSec)  // Solo acumula tiempo si fue correcta
            }

            apply()
        }

        if (statsLayout.visibility == View.VISIBLE) {
            loadStatisticsIntoCharts()
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

        button.backgroundTintList = null

        val drawableRes = when {
            isBlack && isSelected -> R.drawable.black_key_enabled
            isBlack && !isSelected -> R.drawable.black_key_disabled
            !isBlack && isSelected -> R.drawable.white_key_enabled
            else -> R.drawable.white_key_disabled
        }

        button.backgroundTintList = null
        button.background = ContextCompat.getDrawable(requireContext(), drawableRes)

        if (isBlack) {
            button.bringToFront()
        }

        (button.parent as? View)?.invalidate()
    }

    private fun updateOctaveButtonAppearance(button: Button, isEnabled: Boolean) {
        button.alpha = if (isEnabled) 1.0f else 0.5f
        val colorRes = if (isEnabled) R.color.octave_active else R.color.octave_inactive
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))

    }

    private fun loadStatisticsIntoCharts() {
        val prefs = requireContext().getSharedPreferences("stats", Context.MODE_PRIVATE)
        val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

        // Calcular estadísticas totales
        var totalAnswers = 0
        var correctAnswers = 0

        noteNames.forEach { note ->
            totalAnswers += prefs.getInt("${note}_total", 0)
            correctAnswers += prefs.getInt("${note}_correct", 0)
        }
        val incorrectAnswers = totalAnswers - correctAnswers

        // Mostrar resumen en el TextView
        val statsSummaryText = requireView().findViewById<TextView>(R.id.stats_summary)
        statsSummaryText.text = """
        Respuestas totales: $totalAnswers
        Respuestas correctas: $correctAnswers
        Respuestas incorrectas: $incorrectAnswers
    """.trimIndent()
        statsSummaryText.visibility = View.VISIBLE

        // Gráfica de Porcentaje de Respuestas Correctas
        val entriesAcc = noteNames.mapIndexed { i, note ->
            val total = prefs.getInt("${note}_total", 0)
            val correct = prefs.getInt("${note}_correct", 0)
            val pct = if (total > 0) correct * 100f / total else 0f
            BarEntry(i.toFloat(), pct)
        }
        val setAcc = BarDataSet(entriesAcc, "").apply {
            setDrawValues(false)
            color = Color.BLUE
        }
        val dataAcc = BarData(setAcc).apply { barWidth = 0.5f }
        chartAccuracy.apply {
            description.isEnabled = false
            legend.isEnabled = false
            data = dataAcc

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(noteNames)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setLabelCount(noteNames.size, true)
                textSize = 10f
                setDrawGridLines(false)
                setAvoidFirstLastClipping(true)
                axisMinimum = 0f
                axisMaximum = (noteNames.size - 1).toFloat()
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
            }
            axisRight.isEnabled = false

            notifyDataSetChanged()
            invalidate()
        }

        // Gráfica de Tiempo de Respuesta (en segundos)
        val entriesTime = noteNames.mapIndexed { i, note ->
            val total = prefs.getInt("${note}_total", 0)
            val timeSum = prefs.getLong("${note}_timeSum", 0L)
            val avg = if (total > 0) timeSum.toFloat() / total / 1000f else 0f  // Convertimos a segundos
            BarEntry(i.toFloat(), avg)
        }
        val setTime = BarDataSet(entriesTime, "").apply {
            setDrawValues(false)
            color = Color.RED
        }
        val dataTime = BarData(setTime).apply { barWidth = 0.5f }
        chartResponseTime.apply {
            description.isEnabled = false
            legend.isEnabled = false
            data = dataTime

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(noteNames)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setLabelCount(noteNames.size, true)
                textSize = 10f
                setDrawGridLines(false)
                setAvoidFirstLastClipping(true)
                axisMinimum = 0f
                axisMaximum = (noteNames.size - 1).toFloat()
            }

            axisLeft.apply {
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            notifyDataSetChanged()
            invalidate()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun canDeactivateOctave(currentlyActive: Boolean): Boolean {
        // Cuenta cuántas octavas están activas
        val activeCount = listOf(
            isLowerOctaveEnabled,
            isCentralOctaveEnabled,
            isUpperOctaveEnabled
        ).count { it }
        // Si solo hay una activa y es esta, no permitimos desactivarla
        return !(currentlyActive && activeCount == 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.release()
    }
}
