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

                val frequency = getFrequency(audioBuffer)
                val (note, cents) = getMusicalNoteAndCents(frequency)

                withContext(Dispatchers.Main) {
                    updateUI(frequency, note, cents)
                }

                delay(50) // Small delay to avoid high CPU usage
            }
        }
    }



    private fun updateUI(frequency: Double, note: String, cents: Int) {


        tunerTextView.text = "Frequency: ${frequency}Hz -> Note: $note, Cents: $cents"
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
        val fftData = DoubleArray(fftSize) { i -> audioData[i].toDouble() }

        val fft = FFT(fftSize.toLong())
        fft.realForward(fftData)

        var maxIndex = 0
        var maxMagnitude = 0.0

        for (i in 1 until fftSize / 2) { // Ignore DC component (i = 0)
            val real = fftData[2 * i]
            val imag = fftData[2 * i + 1]
            val magnitude = sqrt(real * real + imag * imag)

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                maxIndex = i
            }
        }

        return maxIndex * sampleRate.toDouble() / fftSize
    }

    private fun getMusicalNoteAndCents(frequency: Double): Pair<String, Int> {
        var f = abs(frequency)

        val r = 2.0.pow(1.0 / 12.0)
        val x = ln(frequency / 440.0) / ln(r)
        val noteIndex = if (x >= 0) round(x).toInt() % 12 else round(x + 12).toInt() % 12
        Log.d("Tuner"," noteIndex: $noteIndex")

        var cents = round((x - floor(x)) * 100).toInt()

        cents = when {
            cents in 0..50 -> abs(cents)
            cents > 50 -> -(100 - abs(cents))
            cents < -50 -> 100 - abs(cents)
            cents in -50..<0 -> -abs(cents)
            cents == 100 -> 0
            else -> cents
        }

        val note = getNoteFromIndex(abs(noteIndex))
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
