package com.example.urok

import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urok.ui.theme.UrokTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UrokTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->


                    //Greeting( name = "Android", modifier = Modifier.padding(innerPadding) )

                    //NewComp01(name = "TAMZ", modifier = Modifier.padding(innerPadding))

                    //NewComp02(name = "TAMZ", modifier = Modifier.padding(innerPadding))

                    NewComp03(name = "TAMZ", modifier = Modifier.padding(innerPadding))

                    //NewComp04(name = "TAMZ", modifier = Modifier.padding(innerPadding))

                }
            }
        }
    }
}

// Prvni ukazka vlozeni a zarovnani textu
@Composable
fun NewComp01(name: String, modifier: Modifier = Modifier) {
    // Prvky jsou uspořádány pod sebe na střed obrazovky
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello $name!",
            fontSize = 24.sp
        )

        // Vytvoření pevné mezery mezi texty
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Druhé cvičení TAMZ II",
            fontSize = 16.sp
        )
    }
}

// Druha ukazka tlacitko
// Pokud je uvnitř composable funkce vytvořena klasická proměnná, Jetpack Compose o jejích změnách neví.
// Kliknutí na tlačítko sice proměnnou counter v paměti navýší,
// ale Compose není o změně nijak informován, takže nedojde k překreslení (rekompozici)
// Aby se obrazovka při změně hodnoty automaticky aktualizovala a hodnota zůstala zachována,
// používá se sledovatelný stav - viz NewComp03.
@Composable
fun NewComp02(name: String, modifier: Modifier = Modifier) {
    // Definována běžná lokální proměnná
    var counter = 0

    // Prvky jsou uspořádány pod sebe na střed obrazovky
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello $name!",
            fontSize = 24.sp
        )

        // Vytvoření pevné mezery mezi texty
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Druhé cvičení TAMZ II",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Hodnota v paměti se sice navýší, ale uživatelské rozhraní se nepřekreslí
                counter++
                Log.d("counter", "$counter")
            }
        ) {
            Text("Kliknout")
        }

        Text(
            text = "Pocet kliknuti: $counter",
            fontSize = 16.sp
        )

    }
}

// mutableStateOf(0) – Vytváří sledovatelný stav.
// Jakmile je hodnota změněna, Compose je upozorněn a automaticky překreslí všechny části rozhraní,
// které tuto hodnotu čtou.
// remember { ... } – Zajišťuje, že při opětovném překreslení (rekompozici) se hodnota nevynuluje zpět na 0,
// ale zůstane zachován její poslední stav.
@Composable
fun NewComp03(name: String, modifier: Modifier = Modifier) {
    // Definována běžná lokální proměnná
    //var counter = 0

    var counter by remember { mutableStateOf(0) }

    // Prvky jsou uspořádány pod sebe na střed obrazovky
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello $name!",
            fontSize = 24.sp
        )

        // Vytvoření pevné mezery mezi texty
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Druhé cvičení TAMZ II",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Hodnota je navýšena a rozhraní je automaticky překresleno
                counter++
                Log.d("counter", "$counter")
            }
        ) {
            Text("Kliknout")
        }

        Text(
            text = "Pocet kliknuti: $counter",
            fontSize = 16.sp
        )

    }
}

// Ukázka textového pole.
// Pole samo o sobě neuchovává zadaný text – pouze zobrazuje hodnotu, která je mu předána, a hlásí každou změnu.
// Pro reakci na psaní jsou klíčové dva parametry:
// value – aktuální text zobrazený v poli.
// onValueChange – funkce zavolaná při každém stisku klávesy
@Composable
fun NewComp04(name: String, modifier: Modifier = Modifier) {

    var textInput by remember { mutableStateOf("") }

    // Prvky jsou uspořádány pod sebe na střed obrazovky
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                // Stav je aktualizován novým řetězcem při každém stisku klávesy
                textInput = it
            },
            label = { Text("Zadejte zprávu") },
            placeholder = { Text("Napište něco...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Vytvoření pevné mezery mezi texty
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (textInput.isEmpty()) "Pole je prázdné" else "Zadáno: $textInput",
            fontSize = 16.sp
        )

    }
}

// TODO Pokuste se vytvorit kombinací přechozích ukázek jednoduchou kalkulačku pro sčítání dvou čísel pomocí jednoto tlačítka a dvou TextField

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UrokTheme {
        Greeting("Android")
    }
}