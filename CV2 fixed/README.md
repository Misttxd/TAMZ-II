# Složený úrok

Otevři složku `CV2 fixed` jako projekt v Android Studiu a spusť `app`.

Posuvníky nastavují vklad, roční úrok a počet let. Výsledek i graf se
aktualizují automaticky. Úrok se připisuje jednou ročně, bez daně a dalších vkladů.

Kód je v `app/src/main/java/com/example/urok/MainActivity.kt`:
- `InterestScreen` obsahuje tři stavové proměnné, texty a posuvníky.
- `compoundInterest` počítá `vklad * (1 + sazba / 100)^roky`.
- `money` zaokrouhluje částku pouze pro zobrazení.
- `InterestChart` zobrazuje vklad a získané úroky ve stejném měřítku.

Stejně jako ve cvičení používáme `remember { mutableStateOf(...) }`.
`value` předává hodnotu posuvníku a `onValueChange` ji mění. Výsledek se
vypočítá z aktuálních hodnot. Při otočení telefonu se vstupy vrátí na výchozí hodnoty.

Graf používá MPAndroidChart uvedený v zadání:
https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started
`AndroidView` umožňuje vložit tento klasický Android prvek do Compose.

Ověření sestavení a výpočtu:
`gradlew.bat :app:assembleDebug :app:testDebugUnitTest`
