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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
                Toast.makeText(requireContext(), "Failed to capture image.", Toast.LENGTH_SHORT)
                    .show()
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
                    "Choose",
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

        // Reference to the EditText fields
        val datePicker = view.findViewById<EditText>(R.id.datePicker)
        val timePicker = view.findViewById<EditText>(R.id.timePicker)

        // Date Picker Logic
        datePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Format the date as DD/MM/YYYY
                    val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    // Log the selected date
                    Log.d("DatePicker", "Selected date: $formattedDate")
                    datePicker.setText(formattedDate)  // Set the selected date in the EditText
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
                    // Format the time as HH:mm
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    // Log the selected time
                    Log.d("TimePicker", "Selected time: $formattedTime")
                    timePicker.setText(formattedTime)  // Set the selected time in the EditText
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

    // --- Handling Permission Request Result ---
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
