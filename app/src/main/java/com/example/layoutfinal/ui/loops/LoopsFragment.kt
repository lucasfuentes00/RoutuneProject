package com.example.layoutfinal.ui.loops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.layoutfinal.databinding.FragmentLoopsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Make sure to import your Sound model and SoundAdapter classes

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoopsFragment : Fragment() {

    private var _binding: FragmentLoopsBinding? = null
    private val binding get() = _binding!!

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://freesound.org/apiv2/") // URL base de la API de Freesound
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(FreesoundApiService::class.java)

    private val apiKey = "YOUR_API_KEY" // Coloca tu API Key aquí
    private val clientId = "YOUR_CLIENT_ID" // Coloca tu Client ID aquí

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoopsBinding.inflate(inflater, container, false)

        // Configuramos el listener para el botón de búsqueda
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            searchSounds(query)
        }

        return binding.root
    }

    private fun searchSounds(query: String) {
        // Llamamos a la API de Freesound en un hilo separado
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Llamamos a la API pasando el API key y Client ID en los parámetros
                val response = apiService.searchSounds(query, clientId, "Bearer $apiKey")
                displayResults(response.results)
            } catch (e: Exception) {
                e.printStackTrace() // Manejo de errores
            }
        }
    }

    private fun displayResults(sounds: List<Sound>) {
        // Aquí puedes mostrar los resultados en una lista o Grid
        val adapter = SoundAdapter(sounds)
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


