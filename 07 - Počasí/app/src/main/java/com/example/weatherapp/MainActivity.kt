package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherapp.components.CurrentWeatherScreen
import com.example.weatherapp.components.ForecastScreen
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun WeatherApp(modifier: Modifier = Modifier) {
    var city by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf<CurrentWeather?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showForecast by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = showForecast) { showForecast = false }

    if (showForecast) {
        ForecastScreen(
            city = city.trim(),
            onBack = { showForecast = false },
            modifier = modifier
        )
    } else {
        CurrentWeatherScreen(
            city = city,
            weather = weather,
            loading = loading,
            message = message,
            onCityChange = {
                city = it
                weather = null
                message = null
            },
            onRefresh = {
                val requestedCity = city.trim()
                loading = true
                message = null
                scope.launch {
                    try {
                        weather = loadCurrentWeather(requestedCity)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        message = "Počasí se nepodařilo načíst."
                    } finally {
                        loading = false
                    }
                }
            },
            onForecast = { showForecast = true },
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherAppPreview() {
    WeatherAppTheme {
        WeatherApp()
    }
}
