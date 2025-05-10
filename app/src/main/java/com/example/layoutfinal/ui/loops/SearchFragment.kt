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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.layoutfinal.databinding.FragmentSearchBinding
import com.google.gson.Gson
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

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://freesound.org/apiv2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(FreesoundApiService::class.java)
    private val apiKey = "UtejxE8T5H7rlSxfvQ6TvKujR1tIOJ21zv54FdUJ"

    private lateinit var soundAdapter: SoundAdapter
    private val searchResultList = mutableListOf<SoundResult>()
    private val soundList = mutableListOf<Sound>()

    private lateinit var soundPool: SoundPool
    private val soundIdMap = mutableMapOf<String, Int>()
    private var currentStreamId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        try {
            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .build()

            binding.recyclerViewSearch.layoutManager = LinearLayoutManager(requireContext())
            soundAdapter = SoundAdapter(
                soundList,
                onPlayClickListener = { sound -> playLoop(sound) },
                onStopClickListener = { stopLoop() }
            )
            binding.recyclerViewSearch.adapter = soundAdapter

            binding.searchButton.setOnClickListener {
                Log.d("SearchFragment", "Search button clicked!")
                val query = binding.searchEditText.text.toString()
                if (query.isNotBlank()) {
                    searchSounds(query)
                }
            }
        } catch (e: Exception) {
            Log.e("SearchFragment", "Error in onCreateView: ${e.localizedMessage}", e)
        }

        return binding.root
    }

    private fun searchSounds(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("SearchFragment", "Starting search for query: $query")
            try {
                val response = apiService.searchSounds(query, "Token $apiKey")
                Log.d("SearchFragment", "Search response received: ${response.code()}")
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    Log.d("SearchFragment", "Search results: ${Gson().toJson(searchResponse)}")

                    val results = searchResponse?.results?.take(10) ?: emptyList()
                    searchResultList.clear()
                    searchResultList.addAll(results)

                    // Fetch details for each search result to get the preview URL
                    results.forEach { result ->
                        fetchSoundDetails(result.id)
                    }

                } else {
                    Log.e("SearchFragment", "Search failed with code: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Search failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Error during search: ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Search error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchSoundDetails(soundId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
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
                        withContext(Dispatchers.Main) {
                            val existingIndex = soundList.indexOfFirst { it.name == sound.name }
                            if (existingIndex != -1) {
                                soundList[existingIndex] = sound
                                soundAdapter.notifyItemChanged(existingIndex)
                            } else {
                                soundList.add(sound)
                                soundAdapter.notifyItemInserted(soundList.lastIndex)
                            }
                            Log.d("SearchFragment", "Fetched details for ${sound.name}, preview URL: ${sound.previewUrl}")
                        }
                    }
                } else {
                    Log.e("SearchFragment", "Failed to fetch details for sound $soundId: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to fetch sound details: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Error fetching details for sound $soundId: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching sound details: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun playLoop(sound: Sound) {
        val previewUrl = sound.previewUrl
        if (previewUrl.isEmpty()) {
            Toast.makeText(requireContext(), "No preview URL available", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val localFile = downloadPreviewToFile(previewUrl, sound.name)

            if (localFile != null && localFile.exists()) {
                Log.d("SearchFragment", "Downloaded file successfully: ${localFile.absolutePath}")

                // Load the sound asynchronously
                val soundId = soundPool.load(localFile.absolutePath, 1)
                Log.d("SearchFragment", "Sound load initiated with soundId: $soundId")

                // Set the onLoadCompleteListener outside the coroutine
                soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) { // 0 means successful loading
                        Log.d("SearchFragment", "Sound loaded successfully with soundId: $sampleId")

                        // Switch to the main thread to play the sound
                        lifecycleScope.launch(Dispatchers.Main) {
                            stopLoop()
                            currentStreamId = soundPool.play(sampleId, 1f, 1f, 1, -1, 1f)
                            Log.d("SearchFragment", "Playing sound with streamId: $currentStreamId")
                            Toast.makeText(requireContext(), "Playing loop...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("SearchFragment", "Failed to load sound with sampleId: $sampleId")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error loading sound", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Log.e("SearchFragment", "Error downloading sound or file does not exist.")
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error downloading sound", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private suspend fun downloadPreviewToFile(url: String, soundName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("Download", "Downloading from URL: $url")
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
                    Log.d("Download", "File downloaded to: ${file.absolutePath}")
                    file
                } else {
                    Log.e("Download Error", "Failed to download: ${response.code}")
                    null
                }
            } catch (e: IOException) {
                Log.e("Download Error", e.localizedMessage ?: "Unknown error")
                null
            }
        }
    }


    private fun stopLoop() {
        currentStreamId?.let { soundPool.stop(it) }
        currentStreamId = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        soundPool.release()
    }
}