package com.example.urokcv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF00897B))) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InterestScreen()
                }
            }
        }
    }
}

// Jednorázový vklad, úrok se připisuje jednou ročně, bez daně.
fun compoundInterest(deposit: Double, rate: Double, years: Int): Double {
    return deposit * (1 + rate / 100).pow(years)
}

fun money(value: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("cs-CZ"))
    format.maximumFractionDigits = 0
    return format.format(value) + " Kč"
}

@Composable
fun InterestScreen() {
    // Podobně jako useState v Reactu: změna hodnoty aktualizuje obrazovku.
    var deposit by rememberSaveable { mutableStateOf(100000f) }
    var rate by rememberSaveable { mutableStateOf(5f) }
    var years by rememberSaveable { mutableStateOf(30f) }

    val total = compoundInterest(deposit.toDouble(), rate.toDouble(), years.toInt())
    val interest = total - deposit

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Složený úrok", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Jednorázový vklad • roční úročení • bez daně")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Naspořená suma", fontSize = 16.sp)
                Text(money(total), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Z toho úroky: ${money(interest)}", fontSize = 18.sp)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vklad a získané úroky", fontWeight = FontWeight.Bold)
                InterestChart(deposit.toDouble(), interest)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Vklad: ${money(deposit.toDouble())}", fontWeight = FontWeight.Bold)
                Slider(
                    value = deposit,
                    onValueChange = { deposit = (it / 1000).roundToInt() * 1000f },
                    valueRange = 0f..500000f
                )
                Spacer(Modifier.height(12.dp))
                Text("Roční úrok: ${rate.toInt()} %", fontWeight = FontWeight.Bold)
                Slider(
                    value = rate,
                    onValueChange = { rate = it.roundToInt().toFloat() },
                    valueRange = 0f..15f,
                    steps = 14
                )
                Spacer(Modifier.height(12.dp))
                Text("Období: ${years.toInt()} let", fontWeight = FontWeight.Bold)
                Slider(
                    value = years,
                    onValueChange = { years = it.roundToInt().toFloat() },
                    valueRange = 0f..30f,
                    steps = 29
                )
            }
        }
    }
}

@Composable
fun InterestChart(deposit: Double, interest: Double) {
    // Oba sloupce používají stejné měřítko. Minimum 1 zabrání dělení nulou.
    val maximum = maxOf(deposit, interest, 1.0)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ChartBar("Vklad", deposit, maximum, Color(0xFF80CBC4), Modifier.weight(1f))
        ChartBar("Úroky", interest, maximum, Color(0xFF00897B), Modifier.weight(1f))
    }
}

@Composable
fun ChartBar(label: String, value: Double, maximum: Double, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(money(value), fontSize = 14.sp)
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier.width(64.dp).height((160 * value / maximum).toFloat().dp)
                    .background(color)
            )
        }
        Text(label, modifier = Modifier.padding(top = 8.dp))
    }
}
