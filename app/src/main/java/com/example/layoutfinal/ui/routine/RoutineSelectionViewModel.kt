// RoutineSelectionViewModel.kt
package com.example.layoutfinal.ui.routine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoutineSelectionViewModel : ViewModel() {
    private val _selectedRoutine = MutableLiveData<Routine?>()
    val selectedRoutine: LiveData<Routine?> get() = _selectedRoutine

    fun selectRoutine(routine: Routine) {
        _selectedRoutine.value = routine
    }
}
