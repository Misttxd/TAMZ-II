package com.example.weatherapp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ForecastEntry
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun TemperatureChart(entries: List<ForecastEntry>, modifier: Modifier = Modifier) {
    val lowest = floor(entries.minOf { it.temperature }).toInt()
    val highest = ceil(entries.maxOf { it.temperature }).toInt().coerceAtLeast(lowest + 1)
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisLabelStyle = MaterialTheme.typography.labelSmall

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$highest °C", style = axisLabelStyle)
                Text("$lowest °C", style = axisLabelStyle)
            }

            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                for (fraction in listOf(0f, 0.5f, 1f)) {
                    val y = size.height * fraction
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }

                val path = Path()
                val xStep = size.width / entries.lastIndex.coerceAtLeast(1)

                entries.forEachIndexed { index, entry ->
                    val x = index * xStep
                    val fraction = (highest - entry.temperature) / (highest - lowest)
                    val y = size.height * fraction.toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    if (entries.size == 1) drawCircle(lineColor, 3.dp.toPx(), Offset(x, y))
                }

                drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(entries.first().dateTime, style = axisLabelStyle)
            if (entries.size > 1) Text(entries.last().dateTime, style = axisLabelStyle)
        }
    }
}
