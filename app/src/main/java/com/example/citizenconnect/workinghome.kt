package com.example.citizenconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class workinghome : Fragment() {

    private lateinit var tabComplaints: TextView
    private lateinit var tabAnnouncements: TextView
    private lateinit var complaintsSection: LinearLayout
    private lateinit var announcementsSection: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workinghome, container, false)

        // Initialize views
        tabComplaints = view.findViewById(R.id.tabComplaints)
        tabAnnouncements = view.findViewById(R.id.tabAnnouncements)
        complaintsSection = view.findViewById(R.id.complaintsSection)
        announcementsSection = view.findViewById(R.id.announcementsSection)

        // Default selection
        selectTab("complaints")

        // Tab click listeners
        tabComplaints.setOnClickListener {
            selectTab("complaints")
        }

        tabAnnouncements.setOnClickListener {
            selectTab("announcements")
        }

        return view
    }

    private fun selectTab(tab: String) {
        when (tab) {
            "complaints" -> {
                tabComplaints.setBackgroundResource(R.drawable.tab_selected)
                tabAnnouncements.setBackgroundResource(R.drawable.tab_unselected)
                tabComplaints.setTextColor(resources.getColor(R.color.blue))
                tabAnnouncements.setTextColor(resources.getColor(R.color.blue))

                complaintsSection.visibility = View.VISIBLE
                announcementsSection.visibility = View.GONE
            }

            "announcements" -> {
                tabComplaints.setBackgroundResource(R.drawable.tab_unselected)
                tabAnnouncements.setBackgroundResource(R.drawable.tab_selected)
                tabComplaints.setTextColor(resources.getColor(R.color.blue))
                tabAnnouncements.setTextColor(resources.getColor(R.color.blue))

                complaintsSection.visibility = View.GONE
                announcementsSection.visibility = View.VISIBLE
            }
        }
    }
}
