package com.example.tva0001

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

// Jersey City, NJ 07307. Only these coordinates are sent to Open-Meteo.
private const val LATITUDE = 40.75
private const val LONGITUDE = -74.05
private const val UNIT = "fahrenheit"
private const val REFRESH_MS = 30 * 60 * 1000L

data class Weather(val degrees: Int, val symbol: Symbol)

/** The current weather, refreshed every 30 minutes; null while loading or offline. */
@Composable
fun rememberWeather(): State<Weather?> = produceState<Weather?>(initialValue = null) {
    while (true) {
        fetchWeather()?.let { value = it }
        delay(REFRESH_MS)
    }
}

private suspend fun fetchWeather(): Weather? = withContext(Dispatchers.IO) {
    try {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$LATITUDE&longitude=$LONGITUDE" +
                "&current=temperature_2m,weather_code,is_day&temperature_unit=$UNIT"
        )
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        val body = try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
        val current = JSONObject(body).getJSONObject("current")
        Weather(
            degrees = current.getDouble("temperature_2m").roundToInt(),
            symbol = symbolFor(current.getInt("weather_code"), current.getInt("is_day") == 1),
        )
    } catch (e: Exception) {
        // No internet or an unexpected answer: just hide the weather.
        null
    }
}

/** Turns Open-Meteo's weather code into one of our symbols. */
private fun symbolFor(code: Int, isDay: Boolean): Symbol = when (code) {
    0, 1 -> if (isDay) Symbol.Sun else Symbol.Moon
    2, 3 -> Symbol.Cloud
    45, 48 -> Symbol.Fog
    in 71..77, 85, 86 -> Symbol.Snow
    in 95..99 -> Symbol.Storm
    else -> Symbol.Rain
}
