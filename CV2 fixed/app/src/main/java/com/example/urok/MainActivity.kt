package com.example.urok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.urok.ui.theme.UrokTheme
import kotlin.math.pow
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UrokTheme(darkTheme = false, dynamicColor = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InterestScreen(modifier = Modifier.padding(innerPadding))
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
fun InterestScreen(modifier: Modifier = Modifier) {
    // Stejně jako v NewComp03 v CV1: změna stavu automaticky překreslí rozhraní.
    // remember uchová hodnoty při překreslení této komponenty.
    var deposit by remember { mutableStateOf(100000f) }
    var rate by remember { mutableStateOf(5f) }
    var years by remember { mutableStateOf(30f) }

    // Výsledek se při změně vstupních hodnot přepočítá.
    val total = compoundInterest(deposit.toDouble(), rate.toDouble(), years.toInt())
    val interest = total - deposit

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Složený úrok",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Jednorázový vklad • roční úročení • bez daně")
        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Naspořená suma", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = money(total), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Z toho úroky: ${money(interest)}", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Vklad a získané úroky", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                InterestChart(deposit = deposit.toDouble(), interest = interest)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Vklad: ${money(deposit.toDouble())}", fontWeight = FontWeight.Bold)
                // Stejně jako u textového pole v NewComp04:
                // value je aktuální hodnota a onValueChange hlásí novou hodnotu.
                Slider(
                    value = deposit,
                    onValueChange = {
                        // Zaokrouhlení vkladu na celé tisíce korun.
                        deposit = (it / 1000).roundToInt() * 1000f
                    },
                    valueRange = 0f..500000f
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Roční úrok: ${rate.toInt()} %", fontWeight = FontWeight.Bold)
                Slider(
                    value = rate,
                    onValueChange = {
                        rate = it.roundToInt().toFloat()
                    },
                    valueRange = 0f..15f,
                    steps = 14
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Období: ${years.toInt()} let", fontWeight = FontWeight.Bold)
                Slider(
                    value = years,
                    onValueChange = {
                        years = it.roundToInt().toFloat()
                    },
                    valueRange = 0f..30f,
                    steps = 29
                )
            }
        }
    }
}

@Composable
fun InterestChart(deposit: Double, interest: Double, modifier: Modifier = Modifier) {
    // AndroidView umožňuje použít klasický Android graf uvnitř Compose.
    AndroidView(
        modifier = modifier.fillMaxWidth().height(260.dp),
        factory = { context ->
            val chart = BarChart(context)
            chart.description.isEnabled = false
            chart.legend.isEnabled = false
            chart.setTouchEnabled(false)
            chart.setDrawValueAboveBar(true)
            chart.axisLeft.isEnabled = false
            chart.axisRight.isEnabled = false

            // Rozsah má dvě stejně široké poloviny se středy 0 a 1.
            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = 1.5f
            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            chart.xAxis.granularity = 1f
            chart.xAxis.setLabelCount(2)
            chart.xAxis.setDrawGridLines(false)
            chart.xAxis.textSize = 14f
            chart.xAxis.textColor = android.graphics.Color.DKGRAY
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Vklad", "Úroky"))
            chart
        },
        update = { chart ->
            // update se spustí i při změně slideru: předáme grafu nové částky.
            val entries = listOf(
                BarEntry(0f, deposit.toFloat()),
                BarEntry(1f, interest.toFloat())
            )
            val bars = BarDataSet(entries, "Vklad a úroky")
            bars.setColors(
                android.graphics.Color.rgb(128, 203, 196),
                android.graphics.Color.rgb(0, 137, 123)
            )
            bars.valueTextColor = android.graphics.Color.BLACK
            bars.valueTextSize = 12f
            bars.valueFormatter = object : ValueFormatter() {
                override fun getBarLabel(barEntry: BarEntry): String {
                    // Použijeme původní Double, aby převod na Float nezměnil popisek.
                    return money(if (barEntry.x == 0f) deposit else interest)
                }
            }

            val data = BarData(bars)
            data.barWidth = 0.7f
            chart.data = data
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = maxOf(deposit, interest, 1.0).toFloat() * 1.2f
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}
