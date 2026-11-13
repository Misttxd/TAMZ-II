package com.example.weatherapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.CurrentWeather

@Composable
fun CurrentWeatherScreen(
    city: String,
    weather: CurrentWeather?,
    loading: Boolean,
    message: String?,
    onCityChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onForecast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aktuální počasí", style = MaterialTheme.typography.headlineSmall)

        WeatherCard("Teplota") {
            Text(
                text = weather?.let { "${it.temperature} °C" } ?: "—",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        WeatherCard("Popis počasí") {
            Text(weather?.description ?: "—")
        }

        WeatherCard("Ikona počasí") {
            if (weather == null) {
                Text("—")
            } else {
                WeatherIcon(code = weather.icon, modifier = Modifier.size(64.dp))
            }
        }

        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            label = { Text("Město") },
            placeholder = { Text("Např. Ostrava") },
            singleLine = true,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onRefresh,
            enabled = city.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Načítám…" else "Aktualizovat")
        }

        Button(
            onClick = onForecast,
            enabled = city.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Předpověď")
        }

        if (message != null) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun WeatherCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            content()
        }
    }
}
