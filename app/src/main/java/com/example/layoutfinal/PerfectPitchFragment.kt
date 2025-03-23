package com.example.layoutfinal.PerfectPitch

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R

class PerfectPitchFragment : Fragment() {

    private lateinit var soundPool: SoundPool
    private val soundMap = HashMap<Int, Int>()
    private var isEditSelectionMode = false
    private val noteSelection = BooleanArray(12) { true }
    private var currentNote = -1
    private var startTime: Long = 0

    // Variables de estadísticas
    private val correctCount = IntArray(12) { 0 }
    private val attemptCount = IntArray(12) { 0 }
    private val responseTimes = Array(12) { mutableListOf<Long>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout del fragment
        return inflater.inflate(R.layout.fragment_perfect_pitch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Cargar sonidos (note0.mp3, note1.mp3, etc. en res/raw)
        for (i in 0 until 12) {
            val resId = resources.getIdentifier("note$i", "raw", requireContext().packageName)
            soundMap[i] = soundPool.load(requireContext(), resId, 1)
        }

        // Configurar botones del piano
        for (i in 0 until 12) {
            val buttonId = resources.getIdentifier("btn_note_$i", "id", requireContext().packageName)
            val btn = view.findViewById<Button>(buttonId)
            btn.setOnClickListener {
                if (isEditSelectionMode) {
                    // Alterna la selección
                    noteSelection[i] = !noteSelection[i]
                    updateButtonAppearance(btn, noteSelection[i])
                } else {
                    // Verifica respuesta
                    if (currentNote != -1) {
                        val responseTime = SystemClock.elapsedRealtime() - startTime
                        attemptCount[currentNote]++
                        if (i == currentNote) {
                            correctCount[currentNote]++
                            responseTimes[currentNote].add(responseTime)
                            // Muestra un feedback (Toast, Snackbar, etc.)
                        } else {
                            // Feedback de error
                        }
                        currentNote = -1
                    }
                }
            }
            updateButtonAppearance(btn, noteSelection[i])
        }

        // Botón de reproducir nota
        val playButton = view.findViewById<Button>(R.id.btn_play)
        playButton.setOnClickListener {
            val availableNotes = noteSelection.withIndex().filter { it.value }.map { it.index }
            if (availableNotes.isNotEmpty()) {
                currentNote = availableNotes.random()
                startTime = SystemClock.elapsedRealtime()
                soundPool.play(soundMap[currentNote] ?: 0, 1f, 1f, 1, 0, 1f)
            }
        }

        // Botón de editar selección
        val editSelectionButton = view.findViewById<Button>(R.id.btn_edit_selection)
        editSelectionButton.setOnClickListener {
            isEditSelectionMode = !isEditSelectionMode
            // Cambia UI si lo deseas
        }

        // Botón de ver/ocultar estadísticas
        val viewStatisticsButton = view.findViewById<Button>(R.id.btn_view_statistics)
        val statsLayout = view.findViewById<View>(R.id.statistics_layout)

        viewStatisticsButton.setOnClickListener {
            statsLayout.visibility =
                if (statsLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            // Aquí podrías actualizar las gráficas (MPAndroidChart, etc.)
        }
    }

    private fun updateButtonAppearance(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
        } else {
            button.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.release()
    }
}
