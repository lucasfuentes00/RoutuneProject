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

    fun selectRoutine(routine: Routine) {
        _selectedRoutine.value = routine
    }

    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        _selectedRoutine.value = null // Reset selected routine when a new instrument is selected
    }

    // Save routine to shared preferences
    fun saveRoutine(context: Context) {
        _selectedRoutine.value?.let { currentRoutine ->
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
}

