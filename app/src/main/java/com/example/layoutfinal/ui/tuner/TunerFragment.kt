package com.example.layoutfinal.ui.tuner

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R
import kotlinx.coroutines.*
import org.jtransforms.fft.DoubleFFT_1D as FFT
import kotlin.math.*

class TunerFragment : Fragment() {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 44100
    private var isTuning = false // Flag to stop the loop

    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(1024) // Ensure buffer size is at least 1024 bytes

    private lateinit var tunerTextView: TextView
    private lateinit var noteText: TextView
    private lateinit var tunerSeekBar: SeekBar
    private lateinit var centsText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tuner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteText = view.findViewById(R.id.noteText)
        tunerSeekBar = view.findViewById(R.id.tunerSeekBar)
        tunerSeekBar.isEnabled = false
        centsText = view.findViewById(R.id.centsText)
        tunerTextView = view.findViewById(R.id.tunerText)

        // Check microphone permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startTuner() // Start only if permission is granted
        }
    }

    private fun startTuner() {
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            Log.e("AudioRecord", "Invalid buffer size: $bufferSize")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "Failed to initialize AudioRecord")
            return
        }

        audioRecord?.startRecording()
        isTuning = true

        CoroutineScope(Dispatchers.Default).launch {
            val audioBuffer = ShortArray(bufferSize)

            while (isTuning) {
                val readResult = audioRecord?.read(audioBuffer, 0, bufferSize) ?: AudioRecord.ERROR_INVALID_OPERATION
                if (readResult < 0) {
                    Log.e("AudioRecord", "Error reading audio data: $readResult")
                    break
                }

                val frequency = getFrequency2(audioBuffer)
                val (note, cents) = getMusicalNoteAndCents(frequency)

                withContext(Dispatchers.Main) {
                    updateUI(frequency, note, cents)
                }

                delay(50) // Small delay to avoid high CPU usage
            }
        }
    }



    private fun updateUI(frequency: Double, note: String, cents: Int) {


        //tunerTextView.text = "Frequency: ${frequency}Hz -> Note: $note, Cents: $cents"
        noteText.text = "Nota: $note"
        centsText.text = "Cents: $cents"

        val progress = ((cents + 50) * 100 / 100).coerceIn(0, 100)
        tunerSeekBar.progress = progress.toInt()

        // Change color if note is in tune (-5 to +5 cents)
        val colorRes = if (cents in -10..10) R.color.colorRecieveText else R.color.colorStatusText
        noteText.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun getFrequency(audioData: ShortArray): Double {
        val fftSize = audioData.size
        val fftData = DoubleArray(fftSize * 2) { i ->
            if (i < fftSize) audioData[i].toDouble() else 0.0
        }

        val fft = FFT(fftSize.toLong())
        fft.realForward(fftData)

        var maxIndex = 0
        var maxMagnitudeSquared = 0.0

        // Empezar desde el segundo "bin" para evitar la componente DC (índice 0)
        for (i in 2 until fftSize step 2) {
            val real = fftData[i]
            val imag = fftData[i + 1]
            val magnitudeSquared = real * real + imag * imag

            if (magnitudeSquared > maxMagnitudeSquared) {
                maxMagnitudeSquared = magnitudeSquared
                maxIndex = i / 2 // El índice de frecuencia es la mitad del índice en fftData
            }
        }

        return maxIndex * sampleRate.toDouble() / fftSize
    }
    private fun getFrequency2(audioData: ShortArray): Double {
        val buffer = audioData.map { it.toDouble() }.toDoubleArray()
        val bufferSize = buffer.size
        val yinBuffer = DoubleArray(bufferSize / 2)

        // Paso 1: Función de diferencia
        for (tau in 1 until yinBuffer.size) {
            var sum = 0.0
            for (i in 0 until bufferSize - tau) { // Corregido el límite del bucle
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
        }

        // Paso 2: Diferencia media acumulativa normalizada
        yinBuffer[0] = 1.0
        var runningSum = 0.0
        for (tau in 1 until yinBuffer.size) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] /= (runningSum / tau).coerceAtLeast(1e-9) // Normalización y evitar división por cero
        }

        // Paso 3: Umbral absoluto
        val threshold = 0.15
        var tauEstimate = -1
        var tau = 2
        while (tau < yinBuffer.size) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 < yinBuffer.size && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++
                }
                tauEstimate = tau
                break
            }
            tau++
        }


        if (tauEstimate == -1) {
            return -1.0 // No se encontró tono
        }

        // Paso 4: Interpolación parabólica (opcional para mejorar la precisión)
        val betterTau = if (tauEstimate > 0 && tauEstimate < yinBuffer.size - 1) {
            val s0 = yinBuffer[tauEstimate - 1]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[tauEstimate + 1]
            tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0))
        } else {
            tauEstimate.toDouble()
        }

        return sampleRate / betterTau
    }



    private fun getMusicalNoteAndCents(frequency: Double): Pair<String, Int> {
        if (frequency <= 0.0) return Pair("-", 0)

        val r = 2.0.pow(1.0 / 12.0) // Ratio entre semitonos
        val x = ln(frequency / 440.0) / ln(r) // Distancia en semitonos desde A4
        val semitoneIndex = round(x).toInt()
        val noteIndex = (semitoneIndex ).mod(12) // Ajuste para que A=0
        val note = getNoteFromIndex(noteIndex)

        val cents = ((x - semitoneIndex) * 100).roundToInt()

        return Pair(note, cents)
    }


    private fun getNoteFromIndex(index: Int): String {
        val notes = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
        return notes.getOrElse(index) { "-" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isTuning = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
