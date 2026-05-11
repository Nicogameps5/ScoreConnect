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
        if (customLat != 0.0 && customLng != 0.0) {
            userLat = customLat
            userLng = customLng
            loadMatches()
        }
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
            loadMatches()
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
        data class MatchCandidate(
            val id: String,
            val creatorId: String,
            val sport: String,
            val level: String,
            val date: String,
            val positionNeeded: String,
            val location: String,
            val occupation: String,
            val availablePositions: List<String>
        )

        val currentUid = auth.currentUser?.uid.orEmpty()

        db.collection("matches")
            .get()
            .addOnSuccessListener { result ->

                val candidates = result.documents.mapNotNull { document ->
                    val sport = document.getString("sport").orEmpty()
                    val level = document.getString("level").orEmpty()

                    val ownerId = document.getString("ownerId")
                        ?: document.getString("creatorId")
                        ?: ""

                    // Si el partido no tiene creador válido, lo ignoramos para evitar crash
                    if (ownerId.isBlank()) {
                        return@mapNotNull null
                    }

                    val latValue = document.get("latitude")
                    val lngValue = document.get("longitude")

                    val mLat = when (latValue) {
                        is Number -> latValue.toDouble()
                        is String -> latValue.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    val mLng = when (lngValue) {
                        is Number -> lngValue.toDouble()
                        is String -> lngValue.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    val currentPlayers = (document.getLong("currentPlayers") ?: 1L).toInt()
                    val totalPlayers = (document.getLong("totalPlayers") ?: 1L).toInt()
                    val hasFreeSpace = currentPlayers < totalPlayers

                    var isWithinRange = true

                    if (filterRadius > 0.0) {
                        if (mLat == 0.0 || mLng == 0.0) {
                            isWithinRange = false
                        } else if (userLat != null && userLng != null) {
                            val results = FloatArray(1)
                            Location.distanceBetween(userLat!!, userLng!!, mLat, mLng, results)
                            isWithinRange = (results[0] / 1000) <= filterRadius
                        } else {
                            isWithinRange = false
                        }
                    }

                    val rawPositions = document.get("positions") as? Map<*, *>

                    val availablePositions = rawPositions
                        ?.filterValues { value ->
                            ((value as? Number)?.toInt() ?: 0) > 0
                        }
                        ?.keys
                        ?.map { it.toString() }
                        ?: listOf("Player")

                    val positionsText = if (rawPositions.isNullOrEmpty()) {
                        "Player"
                    } else {
                        rawPositions.entries
                            .filter { entry ->
                                ((entry.value as? Number)?.toInt() ?: 0) > 0
                            }
                            .joinToString(", ") { entry ->
                                "${entry.key} x${(entry.value as? Number)?.toInt() ?: 0}"
                            }
                    }

                    if (
                        (filterSport == "Any" || sport == filterSport) &&
                        (filterLevel == "Any" || level == filterLevel) &&
                        ownerId != currentUid &&
                        isWithinRange &&
                        hasFreeSpace
                    ) {
                        MatchCandidate(
                            id = document.id,
                            creatorId = ownerId,
                            sport = sport,
                            level = level,
                            date = document.getString("dateTime") ?: "",
                            positionNeeded = positionsText,
                            location = document.getString("location").orEmpty(),
                            occupation = "$currentPlayers/$totalPlayers players",
                            availablePositions = availablePositions
                        )
                    } else {
                        null
                    }
                }

                if (candidates.isEmpty()) {
                    adapter.updateItems(emptyList())
                    findViewById<TextView>(R.id.tvEmptyMatches).visibility = android.view.View.VISIBLE
                    return@addOnSuccessListener
                }

                val matches = mutableListOf<MatchResult>()
                var remaining = candidates.size

                fun finishOne() {
                    remaining--

                    if (remaining == 0) {
                        adapter.updateItems(matches)
                        findViewById<TextView>(R.id.tvEmptyMatches).visibility =
                            if (matches.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    }
                }

                candidates.forEach { candidate ->
                    db.collection("users")
                        .document(candidate.creatorId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val creatorUsername = userDoc.getString("username")
                                ?.takeIf { it.isNotBlank() }
                                ?: userDoc.getString("email")
                                ?: "Unknown user"

                            matches.add(
                                MatchResult(
                                    id = candidate.id,
                                    creatorId = candidate.creatorId,
                                    creatorUsername = creatorUsername,
                                    sport = candidate.sport,
                                    level = candidate.level,
                                    date = candidate.date,
                                    positionNeeded = candidate.positionNeeded,
                                    location = candidate.location,
                                    occupation = candidate.occupation,
                                    availablePositions = candidate.availablePositions
                                )
                            )

                            finishOne()
                        }
                        .addOnFailureListener {
                            matches.add(
                                MatchResult(
                                    id = candidate.id,
                                    creatorId = candidate.creatorId,
                                    creatorUsername = "Unknown user",
                                    sport = candidate.sport,
                                    level = candidate.level,
                                    date = candidate.date,
                                    positionNeeded = candidate.positionNeeded,
                                    location = candidate.location,
                                    occupation = candidate.occupation,
                                    availablePositions = candidate.availablePositions
                                )
                            )

                            finishOne()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading matches: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openJoinDialog(result: MatchResult) {
        val positions = result.availablePositions.ifEmpty {
            listOf("Player")
        }

        if (positions.size == 1) {
            submitJoinRequest(result, positions.first())
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Choose position")
            .setItems(positions.toTypedArray()) { _, which ->
                submitJoinRequest(result, positions[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitJoinRequest(result: MatchResult, selectedPosition: String) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "You need to log in again", Toast.LENGTH_SHORT).show()
            return
        }

        if (result.creatorId == currentUser.uid) {
            Toast.makeText(this, "You cannot join your own match", Toast.LENGTH_SHORT).show()
            return
        }

        val matchRef = db.collection("matches").document(result.id)

        matchRef.collection("requests")
            .whereEqualTo("requesterId", currentUser.uid)
            .get()
            .addOnSuccessListener { existingRequests ->
                val alreadyRequested = existingRequests.documents.any { document ->
                    val status = document.getString("status") ?: "pending"
                    status == "pending" || status == "accepted"
                }

                if (alreadyRequested) {
                    Toast.makeText(
                        this,
                        "You already requested to join this match",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("username")
                            ?.takeIf { it.isNotBlank() }
                            ?: currentUser.email.orEmpty()

                        val requestRef = matchRef.collection("requests").document()

                        val waitingPlayer = WaitingPlayer(
                            id = requestRef.id,
                            requesterId = currentUser.uid,
                            position = selectedPosition,
                            name = username,
                            status = "pending"
                        )

                        val batch = db.batch()
                        batch.set(requestRef, waitingPlayer)
                        batch.update(matchRef, "pendingRequests", FieldValue.increment(1))

                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Join request sent for $selectedPosition",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error sending request: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error loading user profile: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Could not verify previous requests: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}