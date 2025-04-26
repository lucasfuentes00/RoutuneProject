package com.example.layoutfinal

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the ViewModel
        routineSelectionViewModel = ViewModelProvider(this)[RoutineSelectionViewModel::class.java]
        val selectedInstrumentText = binding.selectedInstrumentText


        // Setup Navigation
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

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

        // Setup the Spinner (Dropdown Menu)
        val spinner: Spinner = binding.routineSpinner

        // Observe selected instrument and update Spinner
        routineSelectionViewModel.selectedInstrument.observe(this) { instrument ->
            if (instrument != null) {
                selectedInstrumentText.text = "Instrument: ${instrument.name}" // <-- UPDATE TEXT
                val routineNames = instrument.routines.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routineNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        // Set correct OnItemSelectedListener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedRoutineName = parent.getItemAtPosition(position) as String
                val selectedRoutine = routineSelectionViewModel.selectedInstrument.value?.routines?.find {
                    it.name == selectedRoutineName
                }
                if (selectedRoutine != null) {
                    routineSelectionViewModel.selectRoutine(selectedRoutine)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle if nothing is selected
            }
        }
    }
}
