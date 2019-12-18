package me.jamestoohey.temperaturewatcher

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection

class TempChecker: AsyncTask<URL, Void, TempReading?>() {

    override fun doInBackground(vararg p0: URL?): TempReading? {
        val url = URL("https://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=36e5456edd58d94d884b9cb301a30553")
        val connection = url.openConnection() as HttpsURLConnection

        try {
            val jsonString = BufferedInputStream(connection.inputStream).readBytes().toString(Charset.defaultCharset())
            val json = JSONObject(jsonString)
            val temp = json.getJSONObject("main").getInt("temp")
            val feelsLike = json.getJSONObject("main").getInt("feels_like")
            val location = json.getString("name")
            val tempReading = TempReading(temp, feelsLike, location)
            return tempReading

        } finally {
            connection.disconnect()
        }
    }

}