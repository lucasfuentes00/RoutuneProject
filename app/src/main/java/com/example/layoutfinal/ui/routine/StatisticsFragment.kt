

package com.example.layoutfinal.ui.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class StatisticsFragment : Fragment() {

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

        val backButton = view.findViewById<Button>(R.id.backButton)
        val instrumentNameTextView = view.findViewById<TextView>(R.id.instrumentNameTextView)

        // Set the instrument name in the TextView
        val instrumentName = arguments?.getString(ARG_INSTRUMENT_NAME)
        instrumentNameTextView.text = instrumentName

        // Handle back button click
        backButton.setOnClickListener {
            // Go back to the RoutineFragment (parent fragment)
            parentFragmentManager.popBackStack()
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





