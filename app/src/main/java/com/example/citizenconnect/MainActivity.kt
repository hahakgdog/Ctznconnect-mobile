package com.example.citizenconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize bottom navigation view
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        // Initialize FloatingActionButton (FAB)
        val fabComplain: FloatingActionButton = findViewById(R.id.fabComplain)

        // Initialize fragments
        val homeFragment = Home()
        val hotlinesFragment = Hotline()
        val complainsFragment = Complains() // Complains Fragment
        val announcementFragment = Announcement()
        val notificationFragment = Notification()

        // Set the default fragment to Home
        setCurrentFragment(homeFragment)

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.hotlines -> setCurrentFragment(hotlinesFragment)
                R.id.announcement -> setCurrentFragment(announcementFragment)
                R.id.notification -> setCurrentFragment(notificationFragment)
            }
            true
        }

        // Handle FloatingActionButton click to switch to Complains fragment
        fabComplain.setOnClickListener {
            setCurrentFragment(complainsFragment) // Switch to Complains Fragment
        }
    }

    // Function to switch between fragments
    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
