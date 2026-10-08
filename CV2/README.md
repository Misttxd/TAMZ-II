# Složený úrok – CV2

Otevři tuto složku (`CV2`) jako projekt v Android Studiu a spusť modul `app`.

Aplikace používá Kotlin a Jetpack Compose. Posuvníky nastavují jednorázový
vklad (0–500 000 Kč, po 1 000 Kč), roční úrok (0–15 %) a dobu (0–30 let).
Výsledek a dva sloupce grafu se aktualizují automaticky. Oba sloupce mají
stejné měřítko. Daň ani další vklady se nepočítají.
Graf zobrazuje přesné částky nad sloupci. Každý sloupec je uprostřed své poloviny
grafu a má šířku 70 % této poloviny. Obě částky používají společné měřítko v Kč.

Výpočet: `vklad * (1 + úrok / 100)^početLet`. Zaokrouhluje se až zobrazení.

Celý kód aplikace je v `app/src/main/java/com/example/urokcv2/MainActivity.kt`:
- `MainActivity` spouští obrazovku.
- `InterestScreen` obsahuje stav, posuvníky a výsledek.
- `compoundInterest` vypočítá konečnou sumu.
- `InterestChart` předává vklad a úroky knihovně MPAndroidChart přes `AndroidView`.

Graf používá MPAndroidChart (https://github.com/PhilJay/MPAndroidChart).
Závislost `com.github.PhilJay:MPAndroidChart:v3.1.0` je v `app/build.gradle.kts`;
repozitář JitPack je v `settings.gradle.kts` a importy jsou v `MainActivity.kt`.

Zápis odpovídá ukázkám CV1: `UrokTheme`, `Scaffold`, parametr `modifier`,
mezery přes `Spacer` a stav přes `remember { mutableStateOf(...) }`.
Stav funguje podobně jako `useState` v Reactu. Při otočení telefonu se hodnoty
vrátí na výchozí nastavení. Výsledek se odvozuje z aktuálního stavu.

Ověření: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
Test ověřuje oba příklady zadání, nulový úrok, nulovou dobu a nulový vklad.

Na tomto Windows se generovaným složkám vrací příznak „jen pro čtení“.
Kořenový `build.gradle.kts` ho proto před sestavením odstraňuje z adresářů
`app/build`. Configuration cache je vypnutá, aby tato oprava proběhla pokaždé.
