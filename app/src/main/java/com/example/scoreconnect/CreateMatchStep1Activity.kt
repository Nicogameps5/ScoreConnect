package com.example.scoreconnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class CreateMatchStep1Activity : AppCompatActivity() {

    private lateinit var etLocation: EditText
    private lateinit var etDateTime: EditText

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_match_step1)

        etLocation = findViewById(R.id.etLocation)
        etDateTime = findViewById(R.id.etDateTime)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
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