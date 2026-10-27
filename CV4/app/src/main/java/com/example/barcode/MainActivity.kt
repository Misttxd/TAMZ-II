package com.example.barcode

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.barcode.ui.theme.BarcodeTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

import android.provider.MediaStore
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_UPC_A)
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        setContent {
            BarcodeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val input = remember { mutableStateOf("036000291452") }
                    val displayedCode = remember { mutableStateOf("036000291452") }
                    val message = remember { mutableStateOf("") }

                    val graphicsLayer = rememberGraphicsLayer()
                    val scope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(all = 32.dp)
                    ) {
                        OutlinedTextField(
                            value = input.value,
                            onValueChange = { input.value = it },
                            label = { Text("12místný kód") }
                        )

                        Button(
                            onClick = {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        input.value = barcode.rawValue!!
                                    }
                            }
                        ) {
                            Text("Načíst kód")
                        }

                        Button(
                            onClick = {
                                if (input.value.length != 12 || input.value.any { it !in '0'..'9' }) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Zadejte přesně 12 číslic.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                var sum = 0

                                for (i in 0..11) {
                                    val digit = input.value[i].digitToInt()

                                    if (i % 2 == 0) {
                                        sum += digit * 3
                                    } else {
                                        sum += digit
                                    }
                                }

                                if (sum % 10 == 0) {
                                    displayedCode.value = input.value
                                    message.value = "Kontrolní číslice je správná."
                                } else {
                                    message.value = "Kontrolní číslice je chybná."
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Kontrolní číslice je chybná.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            Text("Vykreslit")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    saveBarcodeToGallery(graphicsLayer, displayedCode.value)
                                    message.value = "Obrázek uložen do galerie."
                                }
                            }
                        ) {
                            Text("Uložit do galerie")
                        }

                        Text(message.value)



                        BarcodeExample( //grafická vrstva
                            code = displayedCode.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .drawWithContent {
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }

                                    drawLayer(graphicsLayer)
                                }
                        )
                    }
                }
            }
        }
    }

    private suspend fun saveBarcodeToGallery(graphicsLayer: GraphicsLayer, code: String) {
        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()

        MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "UPC_${code}",
            "Čárový kód UPC-A"
        )
    }
}
//test

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
    code: String,
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
            size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight)
        )


        val unitWidth = 6f
        var x = 0f

        // Pocatecni znacka 101
        for (bit in 2 downTo 0) {
            if (((0b101 shr bit) and 1) == 1) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(unitWidth, rectHeight)
                )
            }

            x += unitWidth
        }

        for (i in 0..5) {
            val digit = code[i].digitToInt()
            val pattern = Lp[digit]

            for (bit in 6 downTo 0) {
                if (((pattern shr bit) and 1) == 1) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x, 0f),
                        size = androidx.compose.ui.geometry.Size(unitWidth, rectHeight)
                    )
                }

                x += unitWidth
            }
        }

        // Prostredni znacka 01010
        for (bit in 4 downTo 0) {
            if (((0b01010 shr bit) and 1) == 1) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(unitWidth, rectHeight)
                )
            }

            x += unitWidth
        }

        for (i in 6..11) {
            val digit = code[i].digitToInt()
            val pattern = Rp[digit]

            for (bit in 6 downTo 0) {
                if (((pattern shr bit) and 1) == 1) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x, 0f),
                        size = androidx.compose.ui.geometry.Size(unitWidth, rectHeight)
                    )
                }

                x += unitWidth
            }
        }

        // Koncova znacka 101
        for (bit in 2 downTo 0) {
            if (((0b101 shr bit) and 1) == 1) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(unitWidth, rectHeight)
                )
            }

            x += unitWidth
        }

    }
}
