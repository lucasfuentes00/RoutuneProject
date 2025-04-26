package com.example.layoutfinal.ui.routine
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class Instrument(
    val name: String,
    var routines: MutableList<Routine> = mutableListOf()
)

object InstrumentStorage {

    private const val PREFS_NAME = "instrument_prefs"
    private const val KEY_INSTRUMENTS = "instruments"

    fun saveInstruments(context: Context, instruments: List<Instrument>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(instruments)
        prefs.edit().putString(KEY_INSTRUMENTS, json).apply()
    }

    fun loadInstruments(context: Context): MutableList<Instrument> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_INSTRUMENTS, null)
        val instruments = if (json != null) {
            val type = object : TypeToken<MutableList<Instrument>>() {}.type
            Gson().fromJson<MutableList<Instrument>>(json, type)
        } else {
            mutableListOf()
        }
        instruments.forEachIndexed { index, instrument ->
            if (instrument.routines == null) {
                instruments[index] = instrument.copy(routines = mutableListOf())
            }
        }

        return instruments
    }

}
