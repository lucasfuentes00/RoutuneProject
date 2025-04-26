package com.example.layoutfinal.ui.routine

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class RoutineFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InstrumentAdapter
    val instrumentViewModel: InstrumentViewModel by activityViewModels()  // Using shared ViewModel
    val routineSelectionViewModel: RoutineSelectionViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.instrumentRecyclerView)
        val addButton = view.findViewById<Button>(R.id.addInstrumentButton)
        val titleText = view.findViewById<TextView>(R.id.titleText)

        // Use ViewModel's list of instruments
        adapter = InstrumentAdapter(instrumentViewModel.instruments,
            onItemClick = { instrument ->

                routineSelectionViewModel.selectInstrument(instrument)
                // Hide elements for statistics fragment
                recyclerView.visibility = View.GONE
                addButton.visibility = View.GONE
                titleText.visibility = View.GONE

                // Show the statistics fragment
                view.findViewById<FrameLayout>(R.id.statisticsFragmentContainer).visibility = View.VISIBLE

                val statisticsFragment = StatisticsFragment.newInstance(instrument.name)
                childFragmentManager.beginTransaction()
                    .replace(R.id.statisticsFragmentContainer, statisticsFragment)
                    .commit()
            },
            onDeleteClick = { instrument ->
                instrumentViewModel.instruments.remove(instrument)  // Modify the ViewModel data
                adapter.notifyDataSetChanged()
                instrumentViewModel.saveInstruments()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            showAddInstrumentDialog()
        }
    }

    private fun showAddInstrumentDialog() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_DONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("New Instrument")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    instrumentViewModel.instruments.add(
                        Instrument(name))
                    adapter.notifyItemInserted(instrumentViewModel.instruments.size - 1)
                    instrumentViewModel.saveInstruments()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Allow adding instrument with the Enter key
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val name = v.text.toString().trim()
                if (name.isNotEmpty()) {
                    instrumentViewModel.instruments.add(Instrument(name))
                    adapter.notifyItemInserted(instrumentViewModel.instruments.size - 1)
                    instrumentViewModel.saveInstruments()
                }
                hideKeyboard(v)
                dialog.dismiss()
                true
            } else {
                false
            }
        }

        dialog.show()
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()

        // Restore the visibility of the instrument list, add button, and title text
        view?.findViewById<RecyclerView>(R.id.instrumentRecyclerView)?.visibility = View.VISIBLE
        view?.findViewById<Button>(R.id.addInstrumentButton)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.titleText)?.visibility = View.VISIBLE

        // Hide the statistics fragment container when coming back
        //view?.findViewById<FrameLayout>(R.id.statisticsFragmentContainer)?.visibility = View.GONE
    }

    fun showRoutineUI() {
        view?.findViewById<RecyclerView>(R.id.instrumentRecyclerView)?.visibility = View.VISIBLE
        view?.findViewById<Button>(R.id.addInstrumentButton)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.titleText)?.visibility = View.VISIBLE
        view?.findViewById<FrameLayout>(R.id.statisticsFragmentContainer)?.visibility = View.GONE

        // Optional: remove the statistics fragment
        childFragmentManager.beginTransaction()
            .remove(childFragmentManager.findFragmentById(R.id.statisticsFragmentContainer)!!)
            .commit()
    }
}


