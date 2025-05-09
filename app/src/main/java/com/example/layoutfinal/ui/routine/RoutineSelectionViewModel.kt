package com.example.layoutfinal.ui.routine

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoutineSelectionViewModel : ViewModel() {

    private val _selectedRoutine = MutableLiveData<Routine?>()
    val selectedRoutine: LiveData<Routine?> get() = _selectedRoutine

    private val _selectedInstrument = MutableLiveData<Instrument?>()
    val selectedInstrument: LiveData<Instrument?> get() = _selectedInstrument

    // MutableLiveData for tempo
    private val _tempo = MutableLiveData<Int>()
    val tempo: LiveData<Int> get() = _tempo

    // Select routine and set the tempo to the default or previously saved one
    fun selectRoutine(routine: Routine) {
        _selectedRoutine.value = routine
        _tempo.value = routine.tempo // Set the tempo of the selected routine
        Log.d("LoopsFragment", "Selected routine: ${routine.name},  ${_tempo.value} BPM") // Logging actual tempo value

    }

    // Select instrument and reset selected routine
    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        _selectedRoutine.value = null // Reset selected routine when a new instrument is selected
        _tempo.value = 90 // Default tempo (or set based on your app logic)
    }

    // Update the tempo
    fun updateTempo(newTempo: Int) {
        _tempo.value = newTempo
        Log.d("LoopsFragment", "RoutineSelectionViewModel Tempo: ${_tempo.value} BPM") // Logging actual tempo value

    }

    // Modify the saveRoutine function to accept selectedRoutine and tempo
    fun saveRoutine(context: Context, selectedRoutine: Routine, tempo: Int?) {
        val currentRoutine = selectedRoutine // Use the passed selectedRoutine
        if (tempo != null) {
            Log.d("LoopsFragment", " saveRoutine ${currentRoutine.name} - $tempo BPM") // Logging actual tempo value

            currentRoutine.tempo = tempo
        } // Set the tempo of the routine

        val routines = RoutineStorage.loadRoutines(context).toMutableList()
        val index = routines.indexOfFirst { it.name == currentRoutine.name }
        if (index != -1) {
            routines[index] = currentRoutine
        } else {
            routines.add(currentRoutine)
        }
        RoutineStorage.saveRoutines(context, routines)
    }
}
