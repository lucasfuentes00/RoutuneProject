package com.example.layoutfinal.ui.loops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R
import com.example.layoutfinal.databinding.FragmentLoopsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        // Set up button listeners
        binding.btnSearch.setOnClickListener {
            loadFragment(SearchFragment())
        }

        binding.btnSounds.setOnClickListener {
            loadFragment(SoundsFragment())
        }

        // Load the default fragment (e.g., SearchFragment)
        loadFragment(SoundsFragment())

        return binding.root
    }

    private fun loadFragment(fragment: Fragment) {
        // Dynamically load the fragment into the FrameLayout
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
