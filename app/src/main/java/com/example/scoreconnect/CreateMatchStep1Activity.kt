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

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step1)

        etLocation = findViewById(R.id.etLocation)
        etDateTime = findViewById(R.id.etDateTime)

        // Map initialization
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // 👉 CLICK EN FECHA → abre calendario
        etDateTime.setOnClickListener {
            showDatePicker()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val location = etLocation.text.toString().trim()
            val dateTime = etDateTime.text.toString().trim()

            if (location.isEmpty() || dateTime.isEmpty()) {
                Toast.makeText(this, "Complete location and date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CreateMatchStep2Activity::class.java)
            intent.putExtra("location", location)
            intent.putExtra("dateTime", dateTime)
            startActivity(intent)
        }
    }

    // Map configuration
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val defaultLocation = LatLng(28.1235, -15.4362)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Clicking on map
        googleMap?.setOnMapClickListener { latLng ->
            googleMap?.clear()

            googleMap?.addMarker(MarkerOptions().position(latLng).title("Miejsce meczu"))

            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            getAddressFromCoordinates(latLng)
        }
    }

    // Getting location from point
    private fun getAddressFromCoordinates(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            // Pobierz maksymalnie 1 wynik dopasowany do współrzędnych
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Wyciągnij pełny adres (np. "ul. Prosta 5, 00-001 Warszawa")
                val addressText = address.getAddressLine(0)

                // Ustaw tekst w polu lokalizacji
                etLocation.setText(addressText)
            } else {
                Toast.makeText(this, "Nie znaleziono adresu dla tego miejsca", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Zabezpieczenie: jeśli Geocoder zawiedzie, wstaw przynajmniej współrzędne
            etLocation.setText("${latLng.latitude}, ${latLng.longitude}")
            Toast.makeText(this, "Błąd pobierania adresu", Toast.LENGTH_SHORT).show()
        }
    }

    // 📅 SELECTOR DE FECHA
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

                // 👉 después de elegir fecha → elegir hora
                showTimePicker()
            },
            year, month, day
        )

        // ❌ bloquear fechas pasadas
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    // ⏰ SELECTOR DE HORA
    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                updateDateTimeField()
            },
            hour,
            minute,
            true
        )

        timePicker.show()
    }

    // 🧾 FORMATEAR FECHA FINAL
    private fun updateDateTimeField() {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        etDateTime.setText(format.format(calendar.time))
    }
}