package com.example.scoreconnect

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.adapters.MatchResultAdapter
import com.example.scoreconnect.model.MatchResult
import com.example.scoreconnect.model.WaitingPlayer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FindMatchResultsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: MatchResultAdapter

    private var filterSport: String = "Any"
    private var filterLevel: String = "Any"
    private var filterRadius: Double = 50.0

    private var userLat: Double? = null
    private var userLng: Double? = null
    private var customLat: Double = 0.0
    private var customLng: Double = 0.0

    private val LOCATION_PERMISSION_REQ_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_match_results)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get Filters
        filterSport = intent.getStringExtra("filterSport") ?: "Any"
        filterLevel = intent.getStringExtra("filterLevel") ?: "Any"
        filterRadius = intent.getDoubleExtra("filterRadius", 50.0)
        customLat = intent.getDoubleExtra("customLat", 0.0)
        customLng = intent.getDoubleExtra("customLng", 0.0)

        setupUI()
        checkLocationLogic()
    }

    private fun setupUI() {
        NavigationUtils.setupHomeButton(this)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnStopSearching).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.rvMatches)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MatchResultAdapter(mutableListOf()) { result -> openJoinDialog(result) }
        recyclerView.adapter = adapter

        val distanceText = if (filterRadius == 0.0) "Any" else "${filterRadius.toInt()} km"
        findViewById<TextView>(R.id.tvResultsSubtitle).text = "Filters -> $filterSport | $filterLevel | $distanceText"
    }

    private fun checkLocationLogic() {
        // PRIORYTET: Jeśli użytkownik wybrał punkt na mapie
        if (customLat != 0.0 && customLng != 0.0) {
            userLat = customLat
            userLng = customLng
            loadMatches()
        }
        // BACKUP: Jeśli mapa pusta, prosimy o GPS urządzenia
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE)
            } else {
                fetchDeviceLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQ_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchDeviceLocation()
        } else {
            loadMatches() // Load without distance if permission denied
        }
    }

    private fun fetchDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLat = location.latitude
                    userLng = location.longitude
                }
                loadMatches()
            }
        } catch (e: SecurityException) { loadMatches() }
    }

    private fun loadMatches() {
        val currentUid = auth.currentUser?.uid.orEmpty()

        db.collection("matches").get().addOnSuccessListener { result ->
            val matches = result.documents.mapNotNull { document ->
                val sport = document.getString("sport").orEmpty()
                val level = document.getString("level").orEmpty()
                val ownerId = document.getString("ownerId") ?: document.getString("creatorId") ?: ""
                val mLat = document.getDouble("latitude") ?: 0.0
                val mLng = document.getDouble("longitude") ?: 0.0

                var isWithinRange = true
                if (filterRadius > 0.0) {
                    // Ignorujemy mecze bez lokalizacji jeśli filtr jest aktywny
                    if (mLat == 0.0 || mLng == 0.0) {
                        isWithinRange = false
                    } else if (userLat != null && userLng != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(userLat!!, userLng!!, mLat, mLng, results)
                        isWithinRange = (results[0] / 1000) <= filterRadius
                    } else {
                        isWithinRange = false // Nie mamy lokalizacji usera a filtr jest włączony
                    }
                }

                if ((filterSport == "Any" || sport == filterSport) &&
                    (filterLevel == "Any" || level == filterLevel) &&
                    ownerId != currentUid && isWithinRange) {

                    // Create MatchResult object (skrócone dla czytelności)
                    MatchResult(
                        id = document.id,
                        creatorId = ownerId,
                        sport = sport,
                        level = level,
                        date = document.getString("dateTime") ?: "",
                        positionNeeded = "Check details",
                        location = document.getString("location").orEmpty(),
                        occupation = "${document.getLong("currentPlayers") ?: 0}/${document.getLong("totalPlayers") ?: 0}",
                        availablePositions = emptyList()
                    )
                } else null
            }
            adapter.updateItems(matches)
            findViewById<TextView>(R.id.tvEmptyMatches).visibility = if (matches.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun openJoinDialog(result: MatchResult) { /* Logika dołączania (taka jak wcześniej) */ }
}