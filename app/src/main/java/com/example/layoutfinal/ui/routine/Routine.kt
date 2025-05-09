package com.example.layoutfinal.ui.routine

import android.content.Context
import com.example.layoutfinal.ui.routine.Routine.Companion.currentDate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Routine(
    val name: String,
    var tempo: Int = 90,
    val archived: Boolean = false,
    val date: String = currentDate(),
    val type: String = "Default"
) {
    companion object {
        fun currentDate(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return formatter.format(Date())
        }
    }
}


object RoutineStorage {

    private const val PREFS_NAME = "routine_prefs"
    private const val KEY_ROUTINES = "routines"

    fun saveRoutines(context: Context, routines: List<Routine>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(routines)
        prefs.edit().putString(KEY_ROUTINES, json).apply()
    }

    fun loadRoutines(context: Context): MutableList<Routine> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ROUTINES, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Routine>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}