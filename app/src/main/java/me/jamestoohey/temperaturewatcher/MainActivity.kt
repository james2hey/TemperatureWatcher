package me.jamestoohey.temperaturewatcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
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
    private var notificationsOn = false

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
                val city = cityText.text.toString()
                if (low > high) {
                    showError("Low temp can't be larger than high temp.")
                } else {
                    mainInput = MainInput(low, high, measurementType, city, notificationsOn)
                    restartTempService()
                    errorText.text = ""
                }
            } catch (error: NumberFormatException) {
                showError("Invalid input. Fill in all fields.")
            }

        }
        setupTemperatureSpinner()
        notificationsSwitch.setOnCheckedChangeListener { _, isOn -> notificationsOn = isOn }

        createNotificationChannel()
        loadDataIfExists()
    }


    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let {
            measurementType = it.getItemAtPosition(position) as TemperatureMeasurement
        }
    }

    override fun onStop() {
        mainInput?.apply {
            val json = Gson().toJson(mainInput)
            openFileOutput("temperature_watcher_data.json", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        }
        super.onStop()
    }

    private fun loadDataIfExists() {
        try {
            openFileInput("temperature_watcher_data.json").use {
                val json = String(it.readBytes())
                val typeToken = object : TypeToken<MainInput>() {}.type
                val convertedModel = Gson().fromJson<MainInput>(json, typeToken)
                mainInput = convertedModel
            }
            mainInput?.let {
                lowTempThresholdText.setText(it.low.toString())
                highTempThresholdText.setText(it.high.toString())
                tempMeasurementSpinner.setSelection(it.measurementType.ordinal)
                Log.d("asdf", it.measurementType.ordinal.toString())
                measurementType = it.measurementType
                cityText.setText(it.city)
                notificationsSwitch.isChecked = it.notificationsOn
            }
        } catch (e: FileNotFoundException) {
            // do nothing
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
        val adapter = ArrayAdapter<TemperatureMeasurement>(this, R.layout.support_simple_spinner_dropdown_item, temperatureMeasurementItems)
        tempMeasurementSpinner.adapter = adapter
        tempMeasurementSpinner.onItemSelectedListener = this
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

}

