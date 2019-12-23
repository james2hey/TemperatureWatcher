package me.jamestoohey.temperaturewatcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var highTempThresholdText: EditText
    private lateinit var lowTempThresholdText: EditText
    private lateinit var cityText: EditText
    private lateinit var errorText: TextView
    private lateinit var saveButton: Button
    private lateinit var notificationsSwitch: Switch
    private lateinit var tempMeasurementSpinner: Spinner

    private var mainInput: MainInput? = null
    private var serviceIntent: Intent? = null
    private var measurementType = TemperatureMeasurement.Celsius


    // If the given temperature changes above/below a threshold, send a notification.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        highTempThresholdText = findViewById(R.id.high_temp_threshold)
        lowTempThresholdText = findViewById(R.id.low_temp_threshold)
        cityText = findViewById(R.id.city)
        errorText = findViewById(R.id.error_text)
        saveButton = findViewById(R.id.save_button)
        tempMeasurementSpinner = findViewById(R.id.spinner)
        notificationsSwitch = findViewById(R.id.notificationsSwitch)

        serviceIntent = createServiceIntent()

        saveButton.setOnClickListener {
            try {
                val low = lowTempThresholdText.text.toString().toDouble()
                val high = highTempThresholdText.text.toString().toDouble()
                val city = lowTempThresholdText.text.toString()
                if (low > high) {
                    showError("Low temp can't be larger than high temp.")
                } else {
                    mainInput = MainInput(low, high, measurementType, city)
                    restartTempService()
                    errorText.text = ""
                }
            } catch (error: NumberFormatException) {
                showError("Invalid input. Fill in all fields.")
            }

        }
        setupTemperatureSpinner()

        createNotificationChannel()
        notificationsSwitch.setOnCheckedChangeListener { _, isOn ->
            if (isOn) startService(serviceIntent) else stopService(serviceIntent)
        }
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.isVisible = true
    }

    private fun restartTempService() {
        stopService(serviceIntent)
        serviceIntent = createServiceIntent()
        if (notificationsSwitch.isChecked) startService(serviceIntent)

    }

    private fun createServiceIntent(): Intent {
        return Intent(this, TempService::class.java).apply {
            mainInput?.let {
                putExtra("highTempThreshold", it.high)
                putExtra("lowTempThreshold", it.low)
                putExtra("measurementType", it.measurementType)
                putExtra("city", it.city)
            }
        }
    }

    private fun setupTemperatureSpinner() {
        val temperatureMeasurementItems = arrayOf(TemperatureMeasurement.Celsius, TemperatureMeasurement.Fahrenheit, TemperatureMeasurement.Kelvin)
        temperatureMeasurementItems.forEach { t -> t.toString().toUpperCase() }
        val adapter = ArrayAdapter<TemperatureMeasurement>(this, R.layout.support_simple_spinner_dropdown_item, temperatureMeasurementItems)
        tempMeasurementSpinner.adapter = adapter
    }

    private fun createNotificationChannel() {
        val name = "Temperature Checker Notifications"
        val descriptionText = "Receive notifications that the current temperature has gone above/below your given threshold."
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(Notification.CATEGORY_EVENT, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let {
            measurementType = it.getItemAtPosition(position) as TemperatureMeasurement
            serviceIntent?.putExtra("measurementType", measurementType)
        }
    }


}

