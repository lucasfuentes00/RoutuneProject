package com.example.layoutfinal

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.layoutfinal.databinding.ActivityMainBinding
import com.example.layoutfinal.ui.routine.RoutineSelectionViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var routineSelectionViewModel: RoutineSelectionViewModel
    private var currentTempo: Int = 120 // Default tempo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTempo() // Load tempo first
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MainActivity", "Binding and content view set")

        // Initialize the ViewModel
        routineSelectionViewModel = ViewModelProvider(this)[RoutineSelectionViewModel::class.java]
        Log.d("MainActivity", "ViewModel initialized")

        // Setup Navigation
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        Log.d("MainActivity", "NavController obtained")

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_routine,
                R.id.navigation_home,
                R.id.navigation_tuner,
                R.id.navigation_notifications,
                R.id.navigation_loops
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        Log.d("MainActivity", "Navigation setup complete")


    }

    // --- TEMPO MANAGEMENT ---

    fun setTempo(newTempo: Int) {
        currentTempo = newTempo
        saveTempo()
    }

    fun getTempo(): Int {
        return currentTempo
    }

    private fun saveTempo() {
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        prefs.edit().putInt("tempo", currentTempo).apply()
    }

    private fun loadTempo() {
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        currentTempo = prefs.getInt("tempo", 120) // Default if not saved
    }
}
