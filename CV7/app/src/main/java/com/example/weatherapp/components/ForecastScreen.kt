package com.example.weatherapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ForecastEntry
import com.example.weatherapp.loadForecast
import kotlinx.coroutines.CancellationException

@Composable
fun ForecastScreen(city: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var forecast by remember(city) { mutableStateOf<List<ForecastEntry>?>(null) }
    var failed by remember(city) { mutableStateOf(false) }

    LaunchedEffect(city) {
        try {
            forecast = loadForecast(city)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            failed = true
        }
    }

    val entries = forecast
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Předpověď – $city", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onBack) {
            Text("Zpět")
        }

        when {
            failed -> Text(
                "Předpověď se nepodařilo načíst.",
                color = MaterialTheme.colorScheme.error
            )
            entries == null -> Text("Načítám…")
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    ForecastCard(entry)
                }
            }
        }
    }
}

@Composable
private fun ForecastCard(entry: ForecastEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherIcon(code = entry.icon, modifier = Modifier.size(48.dp))
            Column {
                Text(entry.dateTime, style = MaterialTheme.typography.titleMedium)
                Text("${entry.temperature} °C")
                Text(entry.description)
            }
        }
    }
}
