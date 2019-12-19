package me.jamestoohey.temperaturewatcher

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

class TempAsyncTask: AsyncTask<URL, Void, TempReading?>() {

    override fun doInBackground(vararg p0: URL?): TempReading? {
        val url = URL("https://api.openweathermap.org/data/2.5/weather?q=Auckland&APPID=36e5456edd58d94d884b9cb301a30553")
        val connection = url.openConnection() as HttpsURLConnection

        try {
            val jsonString = BufferedInputStream(connection.inputStream).readBytes().toString(Charset.defaultCharset())
            val json = JSONObject(jsonString)
            val temp = json.getJSONObject("main").getInt("temp")
            val feelsLike = json.getJSONObject("main").getInt("feels_like")
            val location = json.getString("name")
            return TempReading(kelvinToCelsius(temp), kelvinToCelsius(feelsLike), location)

        } finally {
            connection.disconnect()
        }
    }

    private fun kelvinToCelsius(temp: Int): Int = (temp - 273.15).roundToInt()

    private fun kelvinToFahrenheit(temp: Int): Int {
        val celsiusTemp = kelvinToCelsius(temp)
        return (celsiusTemp * 9 / 5) + 32
    }


}