package com.example.layoutfinal.ui.routine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class RoutineSelectionViewModel : ViewModel() {

    private val _selectedRoutine = MutableLiveData<Routine?>()
    val selectedRoutine: LiveData<Routine?> get() = _selectedRoutine

    private val _selectedInstrument = MutableLiveData<Instrument?>()
    val selectedInstrument: LiveData<Instrument?> get() = _selectedInstrument

    private val _tempo = MutableLiveData<Int>()
    val tempo: LiveData<Int> get() = _tempo

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("RoutinePrefs", Context.MODE_PRIVATE)
        loadSavedState()
    }

    private fun saveSelectedInstrument(instrument: Instrument?) {
        prefs?.edit()?.putString("selectedInstrument", instrument?.let { gson.toJson(it) })?.apply()
    }

    private fun saveSelectedRoutine(routine: Routine?) {
        prefs?.edit()?.putString("selectedRoutine", routine?.let { gson.toJson(it) })?.apply()
    }

    private fun loadSavedState() {
        val savedInstrumentJson = prefs?.getString("selectedInstrument", null)
        _selectedInstrument.value = savedInstrumentJson?.let { gson.fromJson(it, Instrument::class.java) }

        val savedRoutineJson = prefs?.getString("selectedRoutine", null)
        val loadedRoutine = savedRoutineJson?.let { gson.fromJson(it, Routine::class.java) }
        _selectedRoutine.value = loadedRoutine
        _tempo.value = loadedRoutine?.tempo ?: 90 // Load routine tempo or default
    }

    fun selectRoutine(routine: Routine) {
        _selectedRoutine.value = routine
        _tempo.value = routine.tempo
        saveSelectedRoutine(routine)
        Log.d("RoutineViewModel", "Selected routine: ${routine.name}, ${_tempo.value} BPM")
    }

    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        _selectedRoutine.value = null
        _tempo.value = 90
        saveSelectedInstrument(instrument)
        saveSelectedRoutine(null) // Clear any selected routine when instrument changes
    }

    fun updateTempo(newTempo: Int) {
        _tempo.value = newTempo
        // No need to save tempo here directly, it's saved with the routine
    }

    fun saveRoutine(context: Context, selectedRoutine: Routine, tempo: Int?) {
        val currentRoutine = selectedRoutine
        if (tempo != null) {
            currentRoutine.tempo = tempo
            RoutineStorage.updateRoutineTempo(context, currentRoutine.name, tempo)
        }

        // Update in selected instrument
        _selectedInstrument.value?.let { instrument ->
            val routineIndex = instrument.routines.indexOfFirst { it.name == currentRoutine.name }
            if (routineIndex != -1) {
                instrument.routines[routineIndex].tempo = tempo ?: 90
                saveSelectedInstrument(instrument)
            }
        }

        saveSelectedRoutine(currentRoutine)
    }


}