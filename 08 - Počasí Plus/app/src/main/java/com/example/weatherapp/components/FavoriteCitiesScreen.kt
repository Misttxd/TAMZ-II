package com.example.weatherapp.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

@Composable
fun FavoriteCitiesScreen(onSelectCity: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val storage = remember(context) {
        context.getSharedPreferences("favorite_cities", Context.MODE_PRIVATE)
    }
    var cities by remember(storage) {
        mutableStateOf(storage.getStringSet("cities", emptySet()).orEmpty())
    }
    var input by remember { mutableStateOf("") }
    val city = input.trim()

    fun saveCities(updatedCities: Set<String>) {
        storage.edit { putStringSet("cities", updatedCities) }
        cities = updatedCities
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Oblíbená města", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Město") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                saveCities(cities + city)
                input = ""
            },
            enabled = city.isNotEmpty() && cities.none { it.equals(city, ignoreCase = true) }
        ) {
            Text("Přidat")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cities.sorted(), key = { it }) { favorite ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onSelectCity(favorite) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(favorite)
                    }
                    TextButton(onClick = { saveCities(cities - favorite) }) {
                        Text("Odebrat")
                    }
                }
            }
        }
    }
}
