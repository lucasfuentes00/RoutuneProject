package com.example.layoutfinal

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.layoutfinal.databinding.ActivityMainBinding
import com.example.layoutfinal.ui.routine.Routine
import com.example.layoutfinal.ui.routine.RoutineFragment
import com.example.layoutfinal.ui.routine.RoutineSelectionViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var routineSelectionViewModel: RoutineSelectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        routineSelectionViewModel = ViewModelProvider(this)[RoutineSelectionViewModel::class.java]


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_routine,
                R.id.navigation_home,
                R.id.navigation_tuner,
                R.id.navigation_notifications,
                R.id.navigation_loops
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


}

