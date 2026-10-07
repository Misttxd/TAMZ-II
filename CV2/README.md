# Složený úrok – CV2

Otevři tuto složku (`CV2`) jako projekt v Android Studiu a spusť modul `app`.

Aplikace používá Kotlin a Jetpack Compose. Posuvníky nastavují jednorázový
vklad (0–500 000 Kč, po 1 000 Kč), roční úrok (0–15 %) a dobu (0–30 let).
Výsledek a dva sloupce grafu se aktualizují automaticky. Oba sloupce mají
stejné měřítko. Daň ani další vklady se nepočítají.
Graf má procentní měřítko: vyšší sloupec je 100 %, přesné částky jsou nad grafem.
Graf zůstává při pohybu slideru stejnou komponentou, aby neblikal.

Výpočet: `vklad * (1 + úrok / 100)^početLet`. Zaokrouhluje se až zobrazení.

Celý kód aplikace je v `app/src/main/java/com/example/urokcv2/MainActivity.kt`:
- `MainActivity` spouští obrazovku.
- `InterestScreen` obsahuje stav, posuvníky a výsledek.
- `compoundInterest` vypočítá konečnou sumu.
- `InterestChart` předává vklad a úroky knihovně AAY-chart, která vykresluje graf.

Graf používá AAY-chart (https://github.com/TheChance101/AAY-chart).
Závislost `io.github.thechance101:chart:1.1` je v `app/build.gradle.kts`;
importy `BarChart` a `BarParameters` jsou v `MainActivity.kt`.

Zápis odpovídá ukázkám CV1: `UrokTheme`, `Scaffold`, parametr `modifier`,
mezery přes `Spacer` a stav přes `remember { mutableStateOf(...) }`.
Stav funguje podobně jako `useState` v Reactu. Při otočení telefonu se hodnoty
vrátí na výchozí nastavení. Výsledek se odvozuje z aktuálního stavu.

Ověření: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
Test ověřuje oba příklady zadání, nulový úrok, nulovou dobu a nulový vklad.

Na tomto Windows se generovaným složkám vrací příznak „jen pro čtení“.
Kořenový `build.gradle.kts` ho proto před sestavením odstraňuje z adresářů
`app/build`. Configuration cache je vypnutá, aby tato oprava proběhla pokaždé.
