package com.example.citizenconnect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File
import java.io.IOException
import java.util.*

class Complains : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var imagePreview: ImageView
    private lateinit var imagePreviewEmergency: ImageView
    private lateinit var btnTakePhoto_emergency: Button
    private lateinit var btnTakePhoto: Button
    private var currentImagePreview: ImageView? = null
    private var photoUri: Uri? = null
    private var nonEmergencyPhotoUri: Uri? = null
    private var emergencyPhotoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                if (currentImagePreview != null) {
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(currentImagePreview!!)
                    currentImagePreview!!.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Photo captured!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture image.", Toast.LENGTH_SHORT).show()
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

        val btnEmergency = view.findViewById<TextView>(R.id.btnEmergency)
        val btnNonEmergency = view.findViewById<TextView>(R.id.btnNonEmergency)
        val emergencySection = view.findViewById<LinearLayout>(R.id.emergency_section)
        val nonEmergencySection = view.findViewById<LinearLayout>(R.id.non_emergency_section)
        val toggleGroup = view.findViewById<RadioGroup>(R.id.toggleGroup)

        // Slider view — create a view in your XML above the buttons
        val sliderView = view.findViewById<View>(R.id.sliderView)
        val sliderBackground = sliderView.background.mutate()

        sliderView.post {
            sliderView.layoutParams.width = btnEmergency.width
            sliderView.x = toggleGroup.left.toFloat()   // not 0f
            sliderView.requestLayout()
            btnEmergency.setTextColor(Color.WHITE)
        }

        fun selectTab(tab: String) {
            val targetX = when (tab) {
                "Emergency"    -> toggleGroup.left.toFloat()
                "NonEmergency" -> (toggleGroup.left + btnNonEmergency.left).toFloat()
                else           -> toggleGroup.left.toFloat()
            }

            sliderView.animate()
                .x(targetX)
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            if (tab == "Emergency") {
                DrawableCompat.setTint(sliderBackground, Color.parseColor("#ef4136"))
                emergencySection.visibility = View.VISIBLE
                nonEmergencySection.visibility = View.GONE
                btnEmergency.setTextColor(Color.WHITE)
                btnNonEmergency.setTextColor(Color.BLACK)
            } else {
                DrawableCompat.setTint(sliderBackground, Color.parseColor("#1A237E"))
                emergencySection.visibility = View.GONE
                nonEmergencySection.visibility = View.VISIBLE
                btnEmergency.setTextColor(Color.BLACK)
                btnNonEmergency.setTextColor(Color.WHITE)
            }
        }


        btnEmergency.setOnClickListener { selectTab("Emergency") }
        btnNonEmergency.setOnClickListener { selectTab("NonEmergency") }

        // --- UI setup ---
        imagePreview = view.findViewById(R.id.imagePreview)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        imagePreviewEmergency = view.findViewById(R.id.imagePreviewEmergency)
        btnTakePhoto_emergency = view.findViewById(R.id.btnTakePhoto_emergency)

        btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto(imagePreview)
        }
        btnTakePhoto_emergency.setOnClickListener {
            checkCameraPermissionAndTakePhoto(imagePreviewEmergency)
        }

        imagePreview.setOnClickListener {
            nonEmergencyPhotoUri?.let { uri -> showFullImage(uri) }
        }

        imagePreviewEmergency.setOnClickListener {
            emergencyPhotoUri?.let { uri -> showFullImage(uri) }
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
    fun showFullImage(uri: Uri) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_fullscreen_image, null)
        val fullImageView = dialogView.findViewById<ImageView>(R.id.fullscreenImageView)
        Glide.with(this)
            .load(uri)  // Use actual Uri here
            .fitCenter()
            .into(fullImageView)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }


    private fun checkCameraPermissionAndTakePhoto(targetImageView: ImageView) {
        currentImagePreview = targetImageView
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val photoFile = createImageFile()
            photoFile?.let {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".fileprovider",
                    it
                )
                photoUri = uri
                // Save uri for emergency or non-emergency
                if (targetImageView == imagePreview) {
                    nonEmergencyPhotoUri = uri
                } else if (targetImageView == imagePreviewEmergency) {
                    emergencyPhotoUri = uri
                }

                Log.d("Camera", "Launching camera with Uri: $uri")

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Grant URI permission to all camera apps
                val resInfoList = requireActivity().packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    requireContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                    takePictureLauncher.launch(takePictureIntent)
                } else {
                    Toast.makeText(requireContext(), "No camera app found.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                storageDir
            )
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
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
            // Retry photo capture after permission granted
            currentImagePreview?.let {
                checkCameraPermissionAndTakePhoto(it)
            }
        } else {
            Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val mambugan = LatLng(14.6323, 121.1026)
        googleMap.addMarker(MarkerOptions().position(mambugan).title("Mambugan"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mambugan, 16f))

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
    }

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
