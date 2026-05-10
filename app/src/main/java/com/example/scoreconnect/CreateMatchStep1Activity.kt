package com.example.scoreconnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class CreateMatchStep1Activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var etLocation: EditText
    private lateinit var etDateTime: EditText
    private var googleMap: GoogleMap? = null

    // Variable to store the selected coordinates
    private var selectedLatLng: LatLng? = null

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step1)

        etLocation = findViewById(R.id.etLocation)
        etDateTime = findViewById(R.id.etDateTime)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        etDateTime.setOnClickListener {
            showDatePicker()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val location = etLocation.text.toString().trim()
            val dateTime = etDateTime.text.toString().trim()

            if (location.isEmpty() || dateTime.isEmpty() || selectedLatLng == null) {
                Toast.makeText(this, "Please select a location on the map and a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CreateMatchStep2Activity::class.java)
            intent.putExtra("location", location)
            intent.putExtra("dateTime", dateTime)
            // Pass coordinates for future filtering
            intent.putExtra("latitude", selectedLatLng?.latitude ?: 0.0)
            intent.putExtra("longitude", selectedLatLng?.longitude ?: 0.0)
            startActivity(intent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val defaultLocation = LatLng(28.1235, -15.4362)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        googleMap?.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            googleMap?.clear()
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Match Venue"))
            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            getAddressFromCoordinates(latLng)
        }
    }

    private fun getAddressFromCoordinates(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = address.getAddressLine(0)
                etLocation.setText(addressText)
            } else {
                etLocation.setText("${latLng.latitude}, ${latLng.longitude}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            etLocation.setText("${latLng.latitude}, ${latLng.longitude}")
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                showTimePicker()
            },
            year, month, day
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            updateDateTimeField()
        }, hour, minute, true).show()
    }

    private fun updateDateTimeField() {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        etDateTime.setText(format.format(calendar.time))
    }
}