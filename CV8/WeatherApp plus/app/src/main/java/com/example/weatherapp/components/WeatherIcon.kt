package com.example.weatherapp.components

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.weatherapp.R

@Composable
fun WeatherIcon(code: String, modifier: Modifier = Modifier) {
    val icon = when (code) {
        "01d" -> R.drawable.icon_01d
        "01n" -> R.drawable.icon_01n
        "02d" -> R.drawable.icon_02d
        "02n" -> R.drawable.icon_02n
        "03d" -> R.drawable.icon_03d
        "03n" -> R.drawable.icon_03n
        "04d" -> R.drawable.icon_04d
        "04n" -> R.drawable.icon_04n
        "09d" -> R.drawable.icon_09d
        "09n" -> R.drawable.icon_09n
        "10d" -> R.drawable.icon_10d
        "10n" -> R.drawable.icon_10n
        "11d" -> R.drawable.icon_11d
        "11n" -> R.drawable.icon_11n
        "13d" -> R.drawable.icon_13d
        "13n" -> R.drawable.icon_13n
        "50d" -> R.drawable.icon_50d
        "50n" -> R.drawable.icon_50n
        else -> null
    }

    if (icon == null) {
        Text("–", modifier = modifier)
    } else {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = modifier
        )
    }
}
