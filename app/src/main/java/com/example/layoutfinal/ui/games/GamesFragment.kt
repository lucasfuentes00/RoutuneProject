package com.example.layoutfinal.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutfinal.R

class GamesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games, container, false)

        recyclerView = view.findViewById(R.id.gamesRecyclerView)
        adapter = GamesAdapter(listOf("Perfect Pitch")) {
            findNavController().navigate(R.id.action_gamesFragment_to_perfectPitchFragment)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }
}
