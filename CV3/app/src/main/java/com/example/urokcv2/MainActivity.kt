package com.example.urokcv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aay.compose.barChart.BarChart
import com.aay.compose.barChart.model.BarParameters
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.donutChart.DonutChart
import com.aay.compose.donutChart.model.PieChartData
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

private val Czech = Locale.forLanguageTag("cs-CZ")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF26B8CC),
                secondary = Color(0xFF99D6D0),
                tertiary = Color(0xFF99D6D0),
                primaryContainer = Color(0xFFD2EDF0),
                secondaryContainer = Color(0xFFD2EDF0),
                tertiaryContainer = Color(0xFFD2EDF0),
                background = Color(0xFFFCFAF8),
                surface = Color.White,
                surfaceVariant = Color(0xFFD2EDF0),
                surfaceTint = Color(0xFF26B8CC),
                surfaceContainer = Color(0xFFEEF7F9),
                surfaceContainerLow = Color.White,
                surfaceContainerHigh = Color(0xFFEEF7F9),
                surfaceContainerHighest = Color.White,
                surfaceContainerLowest = Color.White,
                onBackground = Color(0xFF4D4D4D),
                onSurface = Color(0xFF4D4D4D)
            )) {
                InterestScreen()
            }
        }
    }
}

// Jeden společný stav pro kalkulačku, nastavení a historii.
// mutableStateOf zajistí, že se po změně hodnoty aktualizuje její zobrazení.
class SavingsState {
    var deposit by mutableStateOf(5000f)
    var monthlyDeposit by mutableStateOf(2000f)
    var rate by mutableStateOf(4f)
    var years by mutableStateOf(30f)
    var chartType by mutableStateOf("Prstencový")
    var depositStep by mutableStateOf(1000f)
    var depositMaximum by mutableStateOf(500000f)
    var graphColor by mutableStateOf("Zelená")
    var taxEnabled by mutableStateOf(false)
    var regularDeposit by mutableStateOf(true)
    var history by mutableStateOf(listOf<String>())

    fun totalAmount(): Double {
        val tax = if (taxEnabled) 15.0 else 0.0
        if (regularDeposit) {
            return monthlySavings(deposit.toDouble(), monthlyDeposit.toDouble(), rate.toDouble(), years.toInt(), tax)
        } else {
            return compoundInterest(deposit.toDouble(), rate.toDouble(), years.toInt(), tax)
        }
    }

    fun totalDeposits(): Double {
        var total = deposit.toDouble()
        if (regularDeposit) {
            total += monthlyDeposit.toDouble() * years.toInt() * 12
        }
        return total
    }

    fun changeDepositStep(step: Float) {
        depositStep = step
        val numberOfSteps = (deposit / step).roundToInt()
        val roundedDeposit = numberOfSteps * step
        deposit = roundedDeposit.coerceIn(0f, depositMaximum)
    }

    fun changeDepositMaximum(maximum: Float) {
        depositMaximum = maximum
        deposit = deposit.coerceIn(0f, maximum)
    }

    fun saveCalculation() {
        val total = totalAmount()
        val interest = total - totalDeposits()
        val initialDeposit = deposit.toDouble()
        val annualRate = rate.toInt()
        val period = years.toInt()

        var compounding = "Roční"
        var monthlyAmount = 0.0
        if (regularDeposit) {
            compounding = "Měsíční"
            monthlyAmount = monthlyDeposit.toDouble()
        }

        var tax = 0
        if (taxEnabled) {
            tax = 15
        }

        val calculation = """
            $compounding úročení
            Počáteční vklad: ${money(initialDeposit)}
            Měsíční vklad: ${money(monthlyAmount)}
            Úrok: $annualRate % • Období: $period let
            Daň: $tax %
            Naspořená suma: ${money(total)}
            Z toho úroky: ${money(interest)}
        """.trimIndent()

        history = history + calculation
    }
}

fun compoundInterest(deposit: Double, rate: Double, years: Int, tax: Double = 0.0): Double {
    val netRate = rate / 100 * (1 - tax / 100)
    return deposit * (1 + netRate).pow(years)
}

fun monthlySavings(deposit: Double, monthlyDeposit: Double, rate: Double, years: Int, tax: Double = 0.0): Double {
    val monthlyRate = rate / 100 * (1 - tax / 100) / 12
    var total = deposit
    // Urok za mesic potom novy vklad na konci mesice.
    for (month in 1..(years * 12)) {
        total = total * (1 + monthlyRate) + monthlyDeposit
    }
    return total
}

fun money(value: Double): String {
    val format = NumberFormat.getNumberInstance(Czech)
    format.maximumFractionDigits = 0
    return format.format(value) + " Kč"
}

fun graphColor(name: String): Color {
    if (name == "Zelená") {
        return Color(0xFF99D6D0)
    } else if (name == "Modrá") {
        return Color(0xFF90CAF9)
    } else {
        return Color(0xFFFFCC80)
    }
}

@Composable
fun InterestScreen() {
    // remember uchová společné hodnoty při překreslování a přepínání obrazovek.
    val state = remember { SavingsState() }
    var screen by remember { mutableStateOf("Kalkulačka") }

    BackHandler(enabled = screen != "Kalkulačka") {
        screen = "Kalkulačka"
    }

    Scaffold(topBar = {
        AppHeader(onNavigate = { selectedScreen ->
            screen = selectedScreen
        })
    }) { padding ->
        // Při změně obrazovky se posouvání vrátí na začátek.
        key(screen) {
            Column(
                modifier = Modifier.padding(padding).padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (screen == "Chart-Type") {
                    ChartTypeScreen(state, onSelect = { selectedType ->
                        state.chartType = selectedType
                        screen = "Kalkulačka"
                    })
                } else if (screen == "Settings") {
                    SettingsScreen(state)
                } else if (screen == "History") {
                    HistoryScreen(state)
                } else {
                    CalculatorScreen(state)
                }
            }
        }
    }
}

@Composable
fun AppHeader(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFEEF7F9))
            .statusBarsPadding().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Spořicí kalkulačka cv. 04", modifier = Modifier.weight(1f))
        AppMenu(onNavigate = onNavigate)
    }
}

@Composable
fun AppMenu(onNavigate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Další možnosti")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Chart-Type") },
                onClick = {
                    expanded = false
                    onNavigate("Chart-Type")
                }
            )
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    expanded = false
                    onNavigate("Settings")
                }
            )
            DropdownMenuItem(
                text = { Text("History") },
                onClick = {
                    expanded = false
                    onNavigate("History")
                }
            )
        }
    }
}

@Composable
fun CalculatorScreen(state: SavingsState) {
    val total = state.totalAmount()
    val deposits = state.totalDeposits()
    val interest = total - deposits

    SavingsSummary(total = total, interest = interest)
    SavingsChartCard(
        deposits = deposits,
        interest = interest,
        chartType = state.chartType,
        chartColor = graphColor(state.graphColor)
    )
    SavingsControls(state)
}

@Composable
fun SavingsSummary(total: Double, interest: Double) {
    Section {
        Text("Naspořená suma: ${money(total)}")
        Text("Z toho úroky: ${money(interest)}")
    }
}

@Composable
fun SavingsChartCard(deposits: Double, interest: Double, chartType: String, chartColor: Color) {
    Section {
        InterestChart(deposits, interest, chartType, chartColor)
    }
}

@Composable
fun SavingsControls(state: SavingsState) {
    Section {
        if (state.regularDeposit) {
            SavingsSlider(
                text = "Měsíční vklad: ${money(state.monthlyDeposit.toDouble())}",
                value = state.monthlyDeposit,
                maximum = 10000f,
                step = 100f,
                onValueChange = { newValue -> state.monthlyDeposit = newValue }
            )
        }
        SavingsSlider(
            text = "Počáteční vklad: ${money(state.deposit.toDouble())}",
            value = state.deposit,
            maximum = state.depositMaximum,
            step = state.depositStep,
            onValueChange = { newValue -> state.deposit = newValue }
        )
        SavingsSlider(
            text = "Úrok: ${state.rate.toInt()} %",
            value = state.rate,
            maximum = 15f,
            steps = 14,
            onValueChange = { newValue -> state.rate = newValue }
        )
        SavingsSlider(
            text = "Období: ${state.years.toInt()} let",
            value = state.years,
            maximum = 30f,
            steps = 29,
            onValueChange = { newValue -> state.years = newValue }
        )
    }
}

@Composable
fun ChartTypeScreen(state: SavingsState, onSelect: (String) -> Unit) {
    val deposits = state.totalDeposits()
    val interest = state.totalAmount() - deposits

    for (type in listOf("Sloupcový", "Prstencový")) {
        ChartPreview(
            type = type,
            deposits = deposits,
            interest = interest,
            chartColor = graphColor(state.graphColor),
            onSelect = { onSelect(type) }
        )
    }
}

@Composable
fun ChartPreview(type: String, deposits: Double, interest: Double, chartColor: Color, onSelect: () -> Unit) {
    Section(modifier = Modifier.clickable(role = Role.Button, onClick = onSelect)) {
        Text("$type graf")
        InterestChart(deposits, interest, type, chartColor)
    }
}

@Composable
fun SettingsScreen(state: SavingsState) {
    DepositStepSettings(state)
    DepositRangeSettings(state)
    ChartColorSettings(state)
    SavingsModeSettings(state)
}

@Composable
fun DepositStepSettings(state: SavingsState) {
    Column(modifier = Modifier.selectableGroup()) {
        Text("Krok posuvníku počátečního vkladu")
        for (step in listOf(1000f, 5000f, 10000f)) {
            ChoiceRow(
                text = money(step.toDouble()),
                selected = state.depositStep == step,
                onClick = { state.changeDepositStep(step) }
            )
        }
    }
}

@Composable
fun DepositRangeSettings(state: SavingsState) {
    Column(modifier = Modifier.selectableGroup()) {
        Text("Maximální počáteční vklad")
        for (maximum in listOf(100000f, 500000f, 1000000f)) {
            ChoiceRow(
                text = money(maximum.toDouble()),
                selected = state.depositMaximum == maximum,
                onClick = { state.changeDepositMaximum(maximum) }
            )
        }
    }
}

@Composable
fun ChartColorSettings(state: SavingsState) {
    Column(modifier = Modifier.selectableGroup()) {
        Text("Barva grafu")
        for (color in listOf("Zelená", "Modrá", "Oranžová")) {
            ChoiceRow(
                text = color,
                selected = state.graphColor == color,
                onClick = { state.graphColor = color }
            )
        }
    }
}

@Composable
fun SavingsModeSettings(state: SavingsState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Toggle("Daň z úroků 15 %", state.taxEnabled, onChange = { enabled ->
            state.taxEnabled = enabled
        })
        Toggle("Pravidelný měsíční vklad", state.regularDeposit, onChange = { enabled ->
            state.regularDeposit = enabled
        })
        if (state.regularDeposit) {
            Text("Měsíční úročení, vklad na konci měsíce.")
        } else {
            Text("Jednorázový vklad, roční úročení.")
        }
    }
}

@Composable
fun HistoryScreen(state: SavingsState) {
    Button(onClick = { state.saveCalculation() }) {
        Text("Uložit aktuální výpočet")
    }
    HistoryList(calculations = state.history)
}

@Composable
fun HistoryList(calculations: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (calculations.isEmpty()) {
            Text("Zatím není uložený žádný výpočet.")
        }
        for (calculation in calculations) {
            HistoryEntry(calculation)
        }
    }
}

@Composable
fun HistoryEntry(calculation: String) {
    Section {
        Text(calculation)
    }
}

@Composable
fun Section(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
fun SavingsSlider(
    text: String,
    value: Float,
    maximum: Float,
    step: Float = 1f,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(text)
        Slider(
            value = value,
            onValueChange = { newValue ->
                val numberOfSteps = (newValue / step).roundToInt()
                val roundedValue = numberOfSteps * step
                onValueChange(roundedValue.coerceIn(0f, maximum))
            },
            valueRange = 0f..maximum,
            steps = steps
        )
    }
}

@Composable
fun ChoiceRow(text: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    )
}

@Composable
fun Toggle(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
    )
}

@Composable
fun InterestChart(deposit: Double, interest: Double, chartType: String, chartColor: Color) {
    val total = deposit + interest
    if (total == 0.0) {
        Text("Pro zobrazení grafu nastav vklad větší než 0 Kč.")
        return
    }

    if (chartType == "Sloupcový") {
        BarInterestChart(deposit, interest, chartColor)
    } else {
        DonutInterestChart(deposit, interest, chartColor)
    }
}

@Composable
fun BarInterestChart(deposit: Double, interest: Double, chartColor: Color) {
    val depositColor = chartColor.copy(alpha = 0.4f)
    // Vyšší sloupec představuje 100 %, skutečná částka je uvedená nad grafem.
    val maximum = maxOf(deposit, interest, 1.0)

    Text("Vklad: ${money(deposit)} • Úroky: ${money(interest)}\n100 % = ${money(maximum)}")
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        BarChart(
            chartParameters = listOf(
                BarParameters("Vklad", listOf(deposit / maximum * 100), depositColor),
                BarParameters("Úroky", listOf(interest / maximum * 100), chartColor)
            ),
            xAxisData = listOf("%"),
            animateChart = false,
            yAxisRange = 4,
            legendPosition = LegendPosition.BOTTOM
        )
    }
}

@Composable
fun DonutInterestChart(deposit: Double, interest: Double, chartColor: Color) {
    val depositColor = chartColor.copy(alpha = 0.4f)

    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        DonutChart(
            pieChartData = listOf(
                PieChartData(data = deposit, color = depositColor, partName = "Vklad"),
                PieChartData(data = interest, color = chartColor, partName = "Úroky")
            ),
            animation = tween(durationMillis = 0),
            legendPosition = LegendPosition.BOTTOM
        )
    }
}
