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
        val selectedInstrumentText = binding.selectedInstrumentText

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

        // Listen to fragment changes and show/hide topbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Navigation destination changed to: ${destination.id}")
            if (destination.id == R.id.navigation_loops || destination.id == R.id.navigation_home) {
                binding.topbar.visibility = View.VISIBLE
                Log.d("MainActivity", "Top bar set to VISIBLE")
            } else {
                binding.topbar.visibility = View.GONE
                Log.d("MainActivity", "Top bar set to GONE")
            }
        }

        // Setup the Spinner (Dropdown Menu)
        val spinner: Spinner = binding.routineSpinner
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            Log.d("MainActivity", "Save button clicked")
            routineSelectionViewModel.saveRoutine(this)
            Log.d("MainActivity", "ViewModel saveRoutine() called")
            Log.d("MainActivity", "Toast displayed")
        }

        // Observe selected instrument and update Spinner
        routineSelectionViewModel.selectedInstrument.observe(this) { instrument ->
            Log.d("MainActivity", "selectedInstrument LiveData updated: $instrument")
            if (instrument != null) {
                selectedInstrumentText.text = "Instrument: ${instrument.name}" // Update text
                val routineNames = instrument.routines.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routineNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                Log.d("MainActivity", "Spinner adapter updated with routines: $routineNames")
                // If instrument is changed, and a routine was selected, keep the routine
                if (routineSelectionViewModel.selectedRoutine.value != null) {
                    val routineIndex = instrument.routines.indexOf(routineSelectionViewModel.selectedRoutine.value)
                    if (routineIndex != -1) {
                        spinner.setSelection(routineIndex)
                    }
                }
            }
        }

        // Set correct OnItemSelectedListener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedRoutineName = parent.getItemAtPosition(position) as String
                Log.d("MainActivity", "Spinner item selected: $selectedRoutineName at position: $position")
                val selectedRoutine = routineSelectionViewModel.selectedInstrument.value?.routines?.find {
                    it.name == selectedRoutineName
                }
                if (selectedRoutine != null) {
                    routineSelectionViewModel.selectRoutine(selectedRoutine)
                    Log.d("MainActivity", "ViewModel selectRoutine() called with: $selectedRoutine")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d("MainActivity", "Spinner: onNothingSelected()")
                // Optional: handle if nothing is selected
            }
        }
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
