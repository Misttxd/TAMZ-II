package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherapp.components.CurrentWeatherScreen
import com.example.weatherapp.components.FavoriteCitiesScreen
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
                WeatherApp()
            }
        }
    }
}

private enum class WeatherSection(val label: String, val icon: Int) {
    HOME("Domů", R.drawable.nav_home),
    FORECAST("Předpověď", R.drawable.nav_forecast),
    GRAPH("Graf", R.drawable.nav_graph),
    FAVORITES("Oblíbené", R.drawable.nav_favorites)
}

@Composable
private fun WeatherApp(modifier: Modifier = Modifier) {
    var city by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf<CurrentWeather?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(WeatherSection.HOME) }
    val scope = rememberCoroutineScope()

    fun changeCity(newCity: String) {
        if (city == newCity) return
        city = newCity
        weather = null
        message = null
    }

    BackHandler(enabled = section != WeatherSection.HOME) { section = WeatherSection.HOME }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                for (destination in WeatherSection.entries) {
                    NavigationBarItem(
                        selected = section == destination,
                        onClick = { section = destination },
                        icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (section) {
            WeatherSection.FORECAST, WeatherSection.GRAPH -> ForecastScreen(
                city = city.trim(),
                onBack = { section = WeatherSection.HOME },
                modifier = screenModifier,
                showGraph = section == WeatherSection.GRAPH
            )
            WeatherSection.FAVORITES -> FavoriteCitiesScreen(
                onSelectCity = { favorite ->
                    changeCity(favorite)
                    section = WeatherSection.HOME
                },
                modifier = screenModifier
            )
            WeatherSection.HOME -> CurrentWeatherScreen(
                city = city,
                weather = weather,
                loading = loading,
                message = message,
                onCityChange = ::changeCity,
                onRefresh = {
                    val requestedCity = city.trim()
                    loading = true
                    message = null
                    scope.launch {
                        try {
                            val currentWeather = loadCurrentWeather(requestedCity)
                            if (city.trim() == requestedCity) weather = currentWeather
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            if (city.trim() == requestedCity) {
                                message = "Počasí se nepodařilo načíst."
                            }
                        } finally {
                            loading = false
                        }
                    }
                },
                onForecast = { section = WeatherSection.FORECAST },
                modifier = screenModifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherAppPreview() {
    WeatherAppTheme {
        WeatherApp()
    }
}
