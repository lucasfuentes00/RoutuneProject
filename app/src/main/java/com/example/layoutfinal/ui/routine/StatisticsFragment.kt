package com.example.layoutfinal.ui.routine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class StatisticsFragment : Fragment() {
    private lateinit var routineAdapter: RoutineAdapter

    companion object {
        private const val ARG_INSTRUMENT_NAME = "instrument_name"

        fun newInstance(name: String): StatisticsFragment {
            val fragment = StatisticsFragment()
            val args = Bundle()
            args.putString(ARG_INSTRUMENT_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val instrumentName = arguments?.getString(ARG_INSTRUMENT_NAME)
        val instrumentNameTextView = view.findViewById<TextView>(R.id.instrumentNameTextView)
        instrumentNameTextView.text = instrumentName

        val backButton = view.findViewById<Button>(R.id.backButton)
        val addRoutineButton = view.findViewById<Button>(R.id.addRoutineButton)
        val routineRecyclerView = view.findViewById<RecyclerView>(R.id.routineRecyclerView)

        // Get the ViewModel from parent
        val instrumentViewModel = (parentFragment as RoutineFragment).instrumentViewModel

        // Find the instrument
        val instrument = instrumentViewModel.instruments.find { it.name == instrumentName }
            ?: return  // Instrument not found, exit early

        // Initialize the routineAdapter here
        routineAdapter = RoutineAdapter(instrument.routines) { routine ->
            instrument.routines.remove(routine)
            routineAdapter.notifyDataSetChanged()

            instrumentViewModel.saveInstruments()
        }


        routineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routineRecyclerView.adapter = routineAdapter

        addRoutineButton.setOnClickListener {
            val context = requireContext()
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Enter Routine Name")

            val input = EditText(context).apply {
                hint = "Routine name"
                imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                setSingleLine(true)
            }
            builder.setView(input)

            val dialog = builder.create()

            dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
                val routineName = input.text.toString().trim()
                if (routineName.isNotEmpty()) {
                    val newRoutine = Routine(name = routineName, tempo = 90)
                    instrument.routines.add(newRoutine) // modify only instrument.routines

                    val insertPosition = instrument.routines.size - 1
                    routineAdapter.notifyItemInserted(insertPosition)

                    instrumentViewModel.saveInstruments()
                }
                dialog.dismiss()
            }

            dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                dialog.dismiss()
            }

            input.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.performClick()
                    true
                } else {
                    false
                }
            }
            dialog.show()
        }



        backButton.setOnClickListener {
            (parentFragment as? RoutineFragment)?.showRoutineUI()
        }
    }

    override fun onResume() {
        super.onResume()

        // Restore the visibility of the "Select an Instrument" title and "Add Instrument" button
        view?.findViewById<TextView>(R.id.titleText)?.visibility = View.VISIBLE
        view?.findViewById<Button>(R.id.addInstrumentButton)?.visibility = View.VISIBLE
        view?.findViewById<RecyclerView>(R.id.instrumentRecyclerView)?.visibility = View.VISIBLE
    }
}