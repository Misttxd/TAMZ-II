# Složený úrok – CV2

Otevři tuto složku (`CV2`) jako projekt v Android Studiu a spusť modul `app`.

Aplikace používá Kotlin a Jetpack Compose. Posuvníky nastavují jednorázový
vklad (0–500 000 Kč, po 1 000 Kč), roční úrok (0–15 %) a dobu (0–30 let).
Výsledek a dva sloupce grafu se aktualizují automaticky. Oba sloupce mají
stejné měřítko. Daň ani další vklady se nepočítají.

Výpočet: `vklad * (1 + úrok / 100)^početLet`. Zaokrouhluje se až zobrazení.

Celý kód aplikace je v `app/src/main/java/com/example/urokcv2/MainActivity.kt`:
- `MainActivity` spouští obrazovku.
- `InterestScreen` obsahuje stav, posuvníky a výsledek.
- `compoundInterest` vypočítá konečnou sumu.
- `InterestChart` a `ChartBar` zobrazují graf bez další knihovny.

Stav v `rememberSaveable` funguje podobně jako `useState` v Reactu a zachová
hodnoty také při otočení telefonu. Výsledek se odvozuje z těchto hodnot.

Ověření: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.
Test ověřuje oba příklady zadání, nulový úrok, nulovou dobu a nulový vklad.

Na tomto Windows se generovaným složkám vrací příznak „jen pro čtení“.
Kořenový `build.gradle.kts` ho proto před sestavením odstraňuje z adresářů
`app/build`. Configuration cache je vypnutá, aby tato oprava proběhla pokaždé.
