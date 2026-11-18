package com.example.weatherapp

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone

data class CurrentWeather(
    val temperature: Double,
    val description: String,
    val icon: String
)

data class ForecastEntry(
    val dateTime: String,
    val temperature: Double,
    val description: String,
    val icon: String
)

suspend fun loadCurrentWeather(city: String): CurrentWeather = withContext(Dispatchers.IO) {
    val json = requestWeather("weather", city)
    val conditions = json.getJSONArray("weather").getJSONObject(0)

    CurrentWeather(
        temperature = json.getJSONObject("main").getDouble("temp"),
        description = conditions.getString("description"),
        icon = conditions.getString("icon")
    )
}

suspend fun loadForecast(city: String): List<ForecastEntry> = withContext(Dispatchers.IO) {
    val json = requestWeather("forecast", city)
    val offsetSeconds = json.getJSONObject("city").getInt("timezone")
    val dateFormat = SimpleDateFormat("d. M. yyyy  HH:mm", Locale.getDefault())
    dateFormat.timeZone = SimpleTimeZone(offsetSeconds * 1000, "City")

    val items = json.getJSONArray("list")
    val forecast = mutableListOf<ForecastEntry>()

    for (index in 0 until items.length()) {
        val item = items.getJSONObject(index)
        val conditions = item.getJSONArray("weather").getJSONObject(0)
        val time = Date(item.getLong("dt") * 1000)

        forecast.add(
            ForecastEntry(
                dateTime = dateFormat.format(time),
                temperature = item.getJSONObject("main").getDouble("temp"),
                description = conditions.getString("description"),
                icon = conditions.getString("icon")
            )
        )
    }

    forecast
}

private fun requestWeather(endpoint: String, city: String): JSONObject {
    val uri = Uri.parse("https://api.openweathermap.org/data/2.5/$endpoint")
        .buildUpon()
        .appendQueryParameter("q", city)
        .appendQueryParameter("appid", BuildConfig.OPENWEATHER_API_KEY)
        .appendQueryParameter("units", "metric")
        .appendQueryParameter("lang", "cz")
        .build()

    val connection = URL(uri.toString()).openConnection() as HttpURLConnection
    connection.connectTimeout = 10_000
    connection.readTimeout = 10_000

    val response = try {
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }

    return JSONObject(response)
}
