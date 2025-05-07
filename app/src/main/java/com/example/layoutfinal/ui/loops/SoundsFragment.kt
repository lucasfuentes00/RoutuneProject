package com.example.layoutfinal.ui.loops

import android.media.MediaPlayer
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
import com.example.layoutfinal.databinding.FragmentSoundsBinding
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

class SoundsFragment : Fragment() {

    private var _binding: FragmentSoundsBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingUrl: String? = null

    private val apiKey = "tyVR1A4eM0P7LnURQsxtn36ABunnHH1ad9ycAMdZ"
    private val soundIdsToFetch = listOf(39334, 300, 200)

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

        binding.recyclerViewSounds.layoutManager = LinearLayoutManager(requireContext())
        soundAdapter = SoundAdapter(soundList,
            onPlayClickListener = { sound ->
                playPreview(sound.previewUrl)
            },
            onDownloadClickListener = { sound ->
                downloadPreview(sound.previewUrl, sound.name)
            })
        binding.recyclerViewSounds.adapter = soundAdapter

        fetchSoundDetailsForMultipleIds()

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
                                license = details.license,
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

    private fun playPreview(previewUrl: String) {
        if (previewUrl.isEmpty()) {
            showToast("No preview URL available")
            return
        }
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                prepare()
                start()
                currentPlayingUrl = previewUrl
                setOnCompletionListener { stopPlayback() }
            }
            showToast("Playing preview...")
        } catch (e: IOException) {
            showToast("Play error: ${e.localizedMessage}")
        }
    }

    private fun downloadPreview(downloadUrl: String, soundName: String) {
        if (downloadUrl.isEmpty()) {
            showToast("No preview URL available for download")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(downloadUrl)
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

                    withContext(Dispatchers.Main) {
                        showToast("Downloaded: $fileName")
                    }
                } else {
                    showToast("Download failed: ${response.code}")
                }
            } catch (e: IOException) {
                showToast("Download error: ${e.localizedMessage}")
            }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingUrl = null
    }

    override fun onStop() {
        super.onStop()
        stopPlayback()
    }

    private fun showToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}