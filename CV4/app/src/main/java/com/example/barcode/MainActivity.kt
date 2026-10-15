package com.example.barcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.barcode.ui.theme.BarcodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarcodeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BarcodeExample(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(all = 32.dp)
                    )
                }
            }
        }
    }
}

// UPC-A L a R patterny
private val Lp = intArrayOf(
    0x0D, // 0001101
    0x19, // 0011001
    0x13, // 0010011
    0x3D, // 0111101
    0x23, // 0100011
    0x31, // 0110001
    0x2F, // 0101111
    0x3B, // 0111011
    0x37, // 0110111
    0x0B  // 0001011
)

private val Rp = intArrayOf(
    0x72, // 1110010
    0x66, // 1100110
    0x6C, // 1101100
    0x42, // 1000010
    0x5C, // 1011100
    0x4E, // 1001110
    0x50, // 1010000
    0x44, // 1000100
    0x48, // 1001000
    0x74  // 1110100
)

// Ukazaka vykreslovani
@Composable
fun BarcodeExample(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Rozmery pro obdelnik
        val rectWidth = 600f
        val rectHeight = 200f

        // Bily obdelnik (pozadi)
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(rectWidth, rectHeight)
        )


        // Uhlopricka z leveho horniho do praveho dolniho rohu
        drawLine(
            color = Color.Black,
            start = Offset(0f, 0f),
            end = Offset(rectWidth, rectHeight),
            strokeWidth = 2f
        )

    }
}
