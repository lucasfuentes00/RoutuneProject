package com.example.layoutfinal.ui.loops

import android.media.SoundPool
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.layoutfinal.MainActivity
import com.example.layoutfinal.R
import com.example.layoutfinal.databinding.FragmentSoundsBinding
import com.example.layoutfinal.ui.routine.RoutineSelectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SoundsFragment : Fragment() {

    private var _binding: FragmentSoundsBinding? = null
    private val binding get() = _binding!!

    private lateinit var soundPool: SoundPool
    private val soundIdMap = mutableMapOf<String, Int>()  // URL to soundId
    private var currentStreamId: Int? = null
    private var currentSoundUrl: String? = null

    private val apiKey = "tyVR1A4eM0P7LnURQsxtn36ABunnHH1ad9ycAMdZ"
    private val soundIdsToFetch = listOf(39334, 245048,805000,804999,804998,804997)
    private val bpmSample = listOf(93, 97,80,63,99,111)
    private var tempo = 96 // Default tempo

    private lateinit var routineSelectionViewModel: RoutineSelectionViewModel


    private val retrofit = Retrofit.Builder()
        .baseUrl("https://freesound.org/apiv2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(FreesoundApiService::class.java)
    private val soundList = mutableListOf<Sound>()
    private lateinit var soundAdapter: SoundAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoundsBinding.inflate(inflater, container, false)

        routineSelectionViewModel = ViewModelProvider(requireActivity())[RoutineSelectionViewModel::class.java]

        Log.d("DEBUG", "Tempo fetched from MainActivity: $tempo") // Debugging the tempo value

        updateTempoText()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        binding.recyclerViewSounds.layoutManager = LinearLayoutManager(requireContext())
        soundAdapter = SoundAdapter(soundList,
            onPlayClickListener = { sound ->
                playLoop(sound)
            },
            onStopClickListener = {
                stopLoop()
            }
        )
        binding.recyclerViewSounds.adapter = soundAdapter

        fetchSoundDetailsForMultipleIds()

        binding.tempoUpButton.setOnClickListener {
            if (tempo < 110) {
                tempo += 1
                restartCurrentLoop()
                updateTempoText()
            }
        }

        binding.tempoDownButton.setOnClickListener {
            if (tempo > 70) {
                tempo -= 1
                restartCurrentLoop()
                updateTempoText()
            }
        }

        updateTempoText()

        return binding.root
    }

    private fun fetchSoundDetailsForMultipleIds() {
        lifecycleScope.launch(Dispatchers.IO) {
            soundIdsToFetch.forEach { soundId ->
                try {
                    val response = apiService.getSoundDetails(soundId, "Token $apiKey")
                    if (response.isSuccessful) {
                        val soundDetails = response.body()
                        soundDetails?.let { details ->
                            val sound = Sound(
                                name = details.name,
                                duration = details.duration,
                                username = details.username,
                                previewUrl = details.previews?.`preview-hq-mp3` ?: ""
                            )
                            soundList.add(sound)
                            withContext(Dispatchers.Main) {
                                soundAdapter.notifyItemInserted(soundList.lastIndex)
                            }
                        }
                    } else {
                        Log.e("Freesound Error", "Failed to fetch sound $soundId: ${response.code()}")
                        showToast("Failed to fetch sound $soundId: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("Freesound Error", "Error fetching sound $soundId: ${e.localizedMessage}")
                    showToast("Error fetching sound $soundId: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun playLoop(sound: Sound) {
        val previewUrl = sound.previewUrl
        if (previewUrl.isEmpty()) {
            showToast("No preview URL available")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val soundId = soundIdMap[previewUrl] ?: run {
                val localFile = downloadPreviewToFile(previewUrl, sound.name)
                if (localFile != null && localFile.exists()) {
                    val id = soundPool.load(localFile.absolutePath, 1)
                    soundIdMap[previewUrl] = id
                    id
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Error downloading sound")
                    }
                    return@launch
                }
            }

            withContext(Dispatchers.Main) {
                stopLoop()

                val index = soundList.indexOfFirst { it.previewUrl == previewUrl }
                val originalBpm = if (index in bpmSample.indices) bpmSample[index] else 96 // fallback

                val playbackRate = tempo.toFloat() / originalBpm
                currentStreamId = soundPool.play(soundId, 1f, 1f, 1, -1, playbackRate)
                currentSoundUrl = previewUrl
                updateTempoText()
                showToast("Playing loop...")
            }
        }
    }

    private suspend fun downloadPreviewToFile(url: String, soundName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val fileName = "preview_${soundName.replace("\\s+".toRegex(), "_")}.mp3"
                    val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)

                    inputStream?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    file
                } else {
                    null
                }
            } catch (e: IOException) {
                Log.e("Download Error", e.localizedMessage ?: "Unknown error")
                null
            }
        }
    }

    private fun stopLoop() {
        currentStreamId?.let {
            soundPool.stop(it)
        }
        currentStreamId = null
        currentSoundUrl = null
    }

    override fun onStop() {
        super.onStop()
        stopLoop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        soundPool.release()
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun restartCurrentLoop() {
        currentSoundUrl?.let { url ->
            val soundId = soundIdMap[url]
            if (soundId != null) {
                stopLoop()

                val index = soundList.indexOfFirst { it.previewUrl == url }
                val originalBpm = if (index in bpmSample.indices) bpmSample[index] else 96

                val playbackRate = tempo.toFloat() / originalBpm
                currentStreamId = soundPool.play(soundId, 1f, 1f, 1, -1, playbackRate)
            }
        }
    }

    private fun updateTempoText() {
        binding.textTempo.text = "Tempo: $tempo BPM"
        routineSelectionViewModel.updateTempo(tempo)
        val loopsFragment = parentFragment as? LoopsFragment
        loopsFragment?.updateTempoInLoopsFragment(tempo)
        Log.d("LoopsFragment", "new tempo, Tempo: $tempo BPM")

    }
    fun updateTempoFromRoutine(newTempo: Int) {
        tempo = newTempo
        binding.textTempo.text = "Tempo: $tempo BPM"
        Log.d("SoundFragment", "Tempo updated in SoundFragment: $tempo BPM")
    }

    fun getCurrentTempo(): Int {
        return tempo
    }
}
