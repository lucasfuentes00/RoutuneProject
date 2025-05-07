package com.example.layoutfinal.ui.loops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.layoutfinal.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : Fragment() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://freesound.org/apiv2/") // URL base de la API de Freesound
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(FreesoundApiService::class.java)
    private val apiKey = "UtejxE8T5H7rlSxfvQ6TvKujR1tIOJ21zv54FdUJ"
    private val clientId = "TyjB2kDtIFM46CsjeZC2"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate your fragment layout
        val binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Set up the search functionality
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            searchSounds(query, binding)
        }

        return binding.root
    }

    private fun searchSounds(query: String, binding: FragmentSearchBinding) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = apiService.searchSounds(query, clientId, "Bearer $apiKey")
                if (response.isSuccessful) {
                    //displayResults(response.body()?.results ?: emptyList(), binding)
                } else {
                    // Handle the case where the response is not successful
                    println("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
