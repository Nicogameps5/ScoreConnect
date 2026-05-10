package com.example.scoreconnect

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FindMatchFiltersActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_match_filters)

        NavigationUtils.setupHomeButton(this)

        val spinnerSport = findViewById<Spinner>(R.id.spinnerFilterSport)
        val spinnerLevel = findViewById<Spinner>(R.id.spinnerFilterLevel)
        val seekBarRadius = findViewById<SeekBar>(R.id.seekBarRadius)
        val tvRadiusValue = findViewById<TextView>(R.id.tvRadiusValue)
        val tvSelectedLocation = findViewById<TextView>(R.id.tvSelectedLocation)

        // Setup Spinners
        val sports = listOf("Any", "Football", "Basketball", "Padel", "Tennis", "Volleyball", "Running")
        val levels = listOf("Any", "Beginner", "Intermediate", "Advanced")

        spinnerSport.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sports).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Setup Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.filterMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // SeekBar Logic
        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvRadiusValue.text = if (progress == 0) "Any distance" else "$progress km"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnStartSearching).setOnClickListener {
            val intent = Intent(this, FindMatchResultsActivity::class.java)
            intent.putExtra("filterSport", spinnerSport.selectedItem.toString())
            intent.putExtra("filterLevel", spinnerLevel.selectedItem.toString())
            intent.putExtra("filterRadius", seekBarRadius.progress.toDouble())

            // Pass custom location from map
            intent.putExtra("customLat", selectedLat)
            intent.putExtra("customLng", selectedLng)

            startActivity(intent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val defaultLocation = LatLng(28.1235, -15.4362)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        googleMap?.setOnMapClickListener { latLng ->
            googleMap?.clear()
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Search here"))
            selectedLat = latLng.latitude
            selectedLng = latLng.longitude
            findViewById<TextView>(R.id.tvSelectedLocation).text = "Custom location selected"
        }
    }
}