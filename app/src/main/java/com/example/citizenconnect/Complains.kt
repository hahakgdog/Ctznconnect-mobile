package com.example.citizenconnect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.util.Log
import java.util.*

class Complains : Fragment(), OnMapReadyCallback {

    private lateinit var tabEmergency: TextView
    private lateinit var tabNon_emergency: TextView
    private lateinit var emergency_section: LinearLayout
    private lateinit var non_emergency_section: LinearLayout
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var imagePreview: ImageView
    private lateinit var btnTakePhoto: Button

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    imagePreview.setImageBitmap(imageBitmap)
                    Toast.makeText(requireContext(), "Photo captured!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Image is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to capture image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_complains, container, false)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- MapView setup ---
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // --- Tab setup ---
        tabEmergency = view.findViewById(R.id.tabUrgent)
        tabNon_emergency = view.findViewById(R.id.tabNonUrgent)
        emergency_section = view.findViewById(R.id.emergency_section)
        non_emergency_section = view.findViewById(R.id.non_emergency_section)

        // Default selection
        selectTab("Urgent")

        // Tab click listeners
        tabEmergency.setOnClickListener { selectTab("Urgent") }
        tabNon_emergency.setOnClickListener { selectTab("Non-Urgent") }

        // --- UI setup ---
        imagePreview = view.findViewById(R.id.imagePreview)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)

        btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }

        // --- AutoComplete TextView Setup for Sitio and Streets ---
        val sitioSubdivisionList = listOf(
            "Sitio Kamias", "Francis Ville", "Josefina Subdivision",
            "Hollywood Hills", "Brentwood ParkHomes", "Town & Country Estates"
        )
        view.findViewById<AutoCompleteTextView>(R.id.sitioSubdivisionAutoComplete)?.apply {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                sitioSubdivisionList
            )
            setAdapter(adapter)
        }

        val streetsMambugan = listOf(
            "Sumulong Highway", "Siruna Village", "Hollywood Hills", "Francisville Subdivision"
        )
        view.findViewById<AutoCompleteTextView>(R.id.street)?.apply {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                streetsMambugan
            )
            setAdapter(adapter)
        }

        // --- Spinner Setup for Committee and Complaint ---
        view.findViewById<Spinner>(R.id.committee)?.apply {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    "Choose",
                    "Peace and Order",
                    "Waste Management and Environment",
                    "Infrastructure",
                    "Health and Sanitation"
                )
            )
            this.adapter = adapter
            this.setSelection(0)
        }

        view.findViewById<Spinner>(R.id.complaint)?.apply {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    "Select Problem",
                    "Garbage not collected on schedule",
                    "Illegal dumping of trash in open areas or waterways",
                    "Absence of recycling facilities or bins",
                    "Foul odor from accumulated waste",
                    "Burning of garbage causing smoke pollution",
                    "Others"
                )
            )
            this.adapter = adapter
            this.setSelection(0)
        }

        view.findViewById<Spinner>(R.id.emergency_complain)?.apply {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    "Select Problem",
                    "Public intoxication causing trouble",
                    "Loitering of minors during curfew hours",
                    "Noise complaints from late-night gatherings or karaoke",
                    "Street fights or disturbances in public areas",
                    "Reports of theft, vandalism or trespassing",
                    "Others"
                )
            )
            this.adapter = adapter
            this.setSelection(0)
        }

        // Reference to the EditText fields
        val datePicker = view.findViewById<EditText>(R.id.datePicker)
        val timePicker = view.findViewById<EditText>(R.id.timePicker)

        // Date Picker Logic
        datePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    Log.d("DatePicker", "Selected date: $formattedDate")
                    datePicker.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Picker Logic
        timePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    Log.d("TimePicker", "Selected time: $formattedTime")
                    timePicker.setText(formattedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // --- Anonymous Switch Logic ---
        val switchAnonymous = view.findViewById<Switch>(R.id.switchAnonymous)
        switchAnonymous.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Anonymous mode ON", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Anonymous mode OFF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectTab(tab: String) {
        when (tab) {
            "Urgent" -> {
                tabEmergency.setBackgroundResource(R.drawable.tab_urgent)
                tabNon_emergency.setBackgroundResource(R.drawable.tab_nonurgentdim)
                tabEmergency.setTextColor(resources.getColor(R.color.whitey))
                tabNon_emergency.setTextColor(resources.getColor(R.color.white_dim))

                // Overlap effect: bring selected tab to front
                tabEmergency.bringToFront()
                tabEmergency.elevation = 8f
                tabNon_emergency.elevation = 0f

                emergency_section.visibility = View.VISIBLE
                non_emergency_section.visibility = View.GONE
            }
            "Non-Urgent" -> {
                tabEmergency.setBackgroundResource(R.drawable.tab_urgentdim)
                tabNon_emergency.setBackgroundResource(R.drawable.tab_nonurgent)
                tabEmergency.setTextColor(resources.getColor(R.color.white_dim))
                tabNon_emergency.setTextColor(resources.getColor(R.color.whitey))

                // Overlap effect: bring selected tab to front
                tabNon_emergency.bringToFront()
                tabNon_emergency.elevation = 8f
                tabEmergency.elevation = 0f

                emergency_section.visibility = View.GONE
                non_emergency_section.visibility = View.VISIBLE
            }
        }
    }

    // --- Camera Permission and Capture Photo Logic ---
    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                takePictureLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(requireContext(), "No camera app found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                takePictureLauncher.launch(takePictureIntent)
            }
        } else {
            Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- MapView Setup ---
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val mambugan = LatLng(14.6323, 121.1026)
        googleMap.addMarker(MarkerOptions().position(mambugan).title("Mambugan"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mambugan, 16f))

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
    }

    // --- MapView Lifecycle ---
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}
