package com.example.layoutfinal.ui.routine

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
        _selectedRoutine.value = null // Reset selected routine when new instrument is selected
    }
}
