package com.example.layoutfinal.ui.dashboard

import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R
import android.Manifest
import androidx.core.app.NotificationCompat.getColor


import org.jtransforms.fft.DoubleFFT_1D as FFT



class DashboardFragment : Fragment() {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 44100
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
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteText = view.findViewById(R.id.noteText)
        tunerSeekBar = view.findViewById(R.id.tunerSeekBar)
        tunerSeekBar.isEnabled = false
        centsText = view.findViewById(R.id.centsText)

        // Initialize views
        tunerTextView = view.findViewById(R.id.tunerText)
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

        val audioBuffer = ShortArray(bufferSize)
        audioRecord?.startRecording() // Now it's safe to call this!

        Thread {
            while (true) {
                audioRecord?.read(audioBuffer, 0, bufferSize)
                val frequency = getFrequency(audioBuffer)
                val semiNotes = getMusicalNoteAndCents(frequency)
                val (note, cents) = semiNotes// Asegúrate de que devuelve un par (nota, cents)

                activity?.runOnUiThread {
                    val formattedCents = "%.3f".format(cents)
                    // Actualizar el texto del afinador
                    tunerTextView.text = "Frequency: ${frequency}Hz -> Note: $note, Cents: $formattedCents"

                    // Actualizar el texto de los cents
                    centsText.text = "Cents: $formattedCents"

                    // Convertir cents (-50 a 50) a un rango de 0 a 100 para la SeekBar
                    val progress = (cents ).coerceIn(0.0, 100.0)
                    tunerSeekBar.progress = progress.toInt()

                    // Cambiar color dependiendo de si está afinado
                    if (cents.toInt() in -5..5) {
                        noteText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRecieveText))
                    } else {
                        noteText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusText))
                    }
                }

                Thread.sleep(50) // Pequeña pausa para evitar consumo excesivo de CPU
            }
        }.start()

    }


    private fun getFrequency(audioData: ShortArray): Double {
        val fftSize = audioData.size
        val fftData = DoubleArray(fftSize)

        // Convert audio data (ShortArray) to DoubleArray
        for (i in audioData.indices) {
            fftData[i] = audioData[i].toDouble()
        }

        // Perform FFT
        val fft = FFT(fftData.size.toLong()) // Initialize FFT with the correct size
        fft.realForward(fftData) // Compute the FFT in-place

        // Find the dominant frequency
        var maxIndex = 0
        var maxMagnitude = 0.0

        for (i in 0 until fftSize / 2) { // FFT output is half the original size
            val real = fftData[2 * i]
            val imag = fftData[2 * i + 1]
            val magnitude = Math.sqrt(real * real + imag * imag)

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                maxIndex = i
            }
        }

        return maxIndex * sampleRate.toDouble() / fftSize
    }
    fun getMusicalNoteAndCents(frequency: Double): Pair<String, Double> {
        val adjustm = frequency*(1)/(440 + 2.9687)
        val semitones = 12 * Math.log((frequency -adjustm)/ (440)) / Math.log(2.0)
        val noteIndex = Math.floorMod((semitones + 12).toInt(), 12)
        // Starting from A (index 0)
        val note = getNoteFromIndex(noteIndex)

        // Calculate the cents of detune
        val cents = 100 * semitones/12

        return Pair(note, cents)
    }

    fun getNoteFromIndex(index: Int): String {
        // Array of notes starting from A
        val notes = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
        return notes[index]
    }


    override fun onDestroy() {
        super.onDestroy()
        audioRecord?.stop()
        audioRecord?.release()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTuner()
        } else {
            Log.e("Permission", "Microphone permission denied")
        }
    }

}
