package com.example.layoutfinal.ui.loops

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.layoutfinal.databinding.FragmentLoopsBinding
import com.example.layoutfinal.ui.routine.RoutineSelectionViewModel

class LoopsFragment : Fragment() {

    private var _binding: FragmentLoopsBinding? = null
    private val binding get() = _binding!!
    private var tempoGlobal: Int = 90

    private lateinit var routineSelectionViewModel: RoutineSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoopsBinding.inflate(inflater, container, false)

        routineSelectionViewModel = ViewModelProvider(requireActivity())[RoutineSelectionViewModel::class.java]

        setupTopbar()
        setupButtons()
        loadFragment(SoundsFragment(), "SoundFragmentTag")

        return binding.root
    }

    private fun setupTopbar() {
        val selectedInstrumentText = binding.selectedInstrumentTextLoops
        val spinner = binding.routineSpinnerLoops
        val saveButton = binding.saveButtonLoops

        saveButton.setOnClickListener {
            val selectedRoutine = routineSelectionViewModel.selectedRoutine.value

            if (selectedRoutine == null) {
                Log.d("LoopsFragment", "No selected routine found.")
            } else {
                routineSelectionViewModel.saveRoutine(requireContext(), selectedRoutine, tempoGlobal)
                Log.d("LoopsFragment", "Routine saved: ${selectedRoutine.name}, Tempo: $tempoGlobal")
            }
        }

        routineSelectionViewModel.selectedInstrument.observe(viewLifecycleOwner) { instrument ->
            instrument?.let {
                selectedInstrumentText.text = "Instrument: ${it.name}"
                val routineNames = it.routines.map { routine -> routine.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, routineNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRoutineName = parent.getItemAtPosition(position) as String
                val selectedRoutine = routineSelectionViewModel.selectedInstrument.value?.routines?.find { it.name == selectedRoutineName }

                selectedRoutine?.let {
                    routineSelectionViewModel.selectRoutine(it)
                    routineSelectionViewModel.updateTempo(it.tempo)

                    updateTempoInSoundFragment(it.tempo)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    fun updateTempoInLoopsFragment(newTempo: Int) {
        tempoGlobal = newTempo
        Log.d("LoopsFragment", "Tempo updated in LoopsFragment: $tempoGlobal BPM")
    }

    private fun updateTempoInSoundFragment(newTempo: Int) {
        val soundFragment = childFragmentManager.findFragmentByTag("SoundFragmentTag") as? SoundsFragment
        soundFragment?.updateTempoFromRoutine(newTempo)
    }

    private fun setupButtons() {
        binding.btnSearch.setOnClickListener {
            loadFragment(SearchFragment(), "SearchFragmentTag") // <- ADD TAG
        }

        binding.btnSounds.setOnClickListener {
            loadFragment(SoundsFragment(), "SoundFragmentTag") // <- ADD TAG
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment, tag)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
