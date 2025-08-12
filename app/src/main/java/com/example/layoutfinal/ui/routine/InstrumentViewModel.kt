package com.example.layoutfinal.ui.routine

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class InstrumentViewModel(application: Application) : AndroidViewModel(application) {
    val instruments: MutableList<Instrument> =
        InstrumentStorage.loadInstruments(application)

    fun saveInstruments() {
        InstrumentStorage.saveInstruments(getApplication(), instruments)
    }
}
