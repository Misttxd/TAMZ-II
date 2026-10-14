"""Vytvoří samostatný HTML průvodce z aktuálního Kotlin kódu a screenshotů."""
from pathlib import Path
import base64
import json
import re
import struct
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(__file__).resolve().parent
SOURCE = ROOT / 'app/src/main/java/com/example/urokcv2/MainActivity.kt'
source = SOURCE.read_text(encoding='utf-8-sig')
components = {}

def extract(name):
    match = re.search(r'^(?:class |[ \t]*fun )' + re.escape(name) + r'\b', source, re.M)
    assert match, f'V Kotlin souboru chybí {name}'
    start = match.start()
    annotation = source[:start].rstrip().splitlines()[-1]
    if annotation == '@Composable':
        start = source.rfind('@Composable', 0, start)
    opening = source.index('{', match.end())
    depth = 0
    for i in range(opening, len(source)):
        if source[i] == '{':
            depth += 1
        elif source[i] == '}':
            depth -= 1
            if depth == 0:
                code = source[start:i + 1]
                # Metody třídy zobrazíme bez společného odsazení.
                if match.group(0).startswith('    '):
                    code = '\n'.join(line[4:] if line.startswith('    ') else line for line in code.splitlines())
                return code, source[:start].count('\n') + 1
    raise ValueError(name)

def entry(name, title, role, steps, defense, group='Komponenty'):
    code, line = extract(name)
    components[name] = dict(name=name, title=title, role=role, steps=steps,
                            defense=defense, group=group, code=code, line=line)

entry('InterestScreen', 'Řízení aplikace a navigace', 'Drží společná data a určuje, která obrazovka se právě zobrazí.', [
    '`remember` vytvoří a uchová jeden objekt `SavingsState` při překreslování. Proměnná `screen` obsahuje název obrazovky.',
    '`Scaffold` oddělí hlavičku od obsahu. Podmínky `if/else` podle hodnoty `screen` zavolají jednu ze čtyř funkcí obrazovek.',
    'Menu změní `screen`. Výběr grafu změní také `state.chartType` a vrátí kalkulačku. `BackHandler` vrací kalkulačku systémovým tlačítkem Zpět.',
    '`key(screen)` při změně obrazovky vytvoří znovu její posouvání, takže obsah začíná nahoře.'
], 'Navigaci řeším obyčejnou podmínkou. Menu mění proměnnou screen a Compose podle ní zobrazí příslušnou obrazovku.', 'Obrazovky')
entry('AppHeader', 'Hlavička aplikace', 'Spojuje název aplikace a menu do jedné horní lišty.', [
    '`Row` umístí název a menu vedle sebe. `weight(1f)` nechá název zabrat volné místo, takže menu zůstane napravo.',
    '`statusBarsPadding()` nechá místo pro systémový stavový řádek. Tyrkysové pozadí a odsazení nastavuje `Modifier`.',
    '`onNavigate` předá výběr z menu do `InterestScreen`; hlavička sama nerozhoduje, co vykreslit.'
], 'Hlavička je složená komponenta: obyčejný Row s názvem a samostatným menu.')
entry('AppMenu', 'Rozbalovací menu', 'Zobrazuje tři tečky a nabídku Chart-Type, Settings a History.', [
    '`expanded` je Boolean: `true` znamená otevřené menu. Kliknutí na ikonu hodnotu obrátí.',
    '`DropdownMenu` používá `expanded`; kliknutí mimo menu zavolá `onDismissRequest` a menu zavře.',
    'Každá položka nejprve nastaví `expanded = false`, potom zavolá `onNavigate` se jménem požadované obrazovky.',
    '`onNavigate: (String) -> Unit` je předaná funkce: dostane text a provede akci. V této aplikaci nastaví `screen`.'
], 'Menu má vlastní stav otevření. Výběr pouze oznámí rodičovské komponentě, na kterou obrazovku přejít.')
entry('CalculatorScreen', 'Obrazovka kalkulačky', 'Sestaví hlavní obrazovku ze tří větších komponent.', [
    'Nejdřív získá konečnou částku a celkové vklady. Úroky jsou rozdíl těchto dvou čísel.',
    '`SavingsSummary` dostane částku a úroky; `SavingsChartCard` také součet vkladů, typ grafu a barvu.',
    '`SavingsControls` dostane společný stav, aby mohla měnit vstupy. Obrazovka už nevypisuje všechny posuvníky sama.'
], 'Kalkulačka spojuje výsledek, graf a ovládání. Výpočty jsou oddělené od jednotlivých vizuálních komponent.', 'Obrazovky')
entry('SavingsSummary', 'Přehled výsledku', 'Bílá karta s naspořenou sumou a úroky.', [
    'Vstupem jsou dvě hodnoty typu `Double`: `total` a `interest`.',
    '`Section` poskytne kartu. Dva elementy `Text` zobrazí hodnoty upravené funkcí `money`.',
    'Komponenta nic nepočítá ani neukládá. Pouze zobrazí předaná čísla.'
], 'Výsledek je samostatná komponenta se dvěma číselnými vstupy. Formátování na koruny řeší money.')
entry('SavingsChartCard', 'Karta zvoleného grafu', 'Obalí aktuálně vybraný graf bílou kartou.', [
    'Dostane vklady, úroky, název typu grafu a barvu.',
    '`Section` vykreslí společný obal a uvnitř zavolá `InterestChart`.',
    'Výběr mezi sloupcem a prstencem řeší až `InterestChart`, takže karta nemusí znát detaily knihovny.'
], 'Karta řeší obal grafu. Vykreslení konkrétního typu je úloha další komponenty.')
entry('SavingsControls', 'Ovládání kalkulačky', 'Skupina posuvníků pro vklady, sazbu a délku spoření.', [
    'Měsíční vklad se zobrazí jen při `state.regularDeposit == true`.',
    'Počáteční vklad používá krok a maximum z nastavení. Měsíční vklad má maximum 10 000 Kč a krok 100 Kč.',
    'Sazba je 0–15 % a období 0–30 let. `steps` určuje mezilehlé značky posuvníku.',
    'Každé `onValueChange` zapíše novou hodnotu do společného stavu. Compose potom aktualizuje výsledek a graf.'
], 'Ovládání mění pouze vstupní data. Změna sledované proměnné způsobí přepočet a nové zobrazení.')
entry('SavingsSlider', 'Posuvník s popiskem', 'Spojuje text aktuální hodnoty a jeden posuvník do komponenty.', [
    '`Column` umístí popisek nad `Slider`. `value` je aktuální hodnota a `maximum` konec rozsahu.',
    'Po pohybu se hodnota vydělí krokem, zaokrouhlí na počet kroků a vynásobí zpět. Například při kroku 1 000 Kč se 5 400 Kč změní na 5 000 Kč.',
    '`coerceIn(0f, maximum)` drží výsledek v povoleném rozsahu. `onValueChange` ho předá do stavu.',
    '`step` a `steps` nejsou totéž: `step` je skutečný krok hodnoty, `steps` je počet mezilehlých pozic nativního Slideru. Pro 0–15 je těchto mezilehlých pozic 14.'
], 'Společný posuvník hlídá rozsah a zaokrouhlení. Rodič určí, do které proměnné se má výsledek zapsat.')
entry('ChartTypeScreen', 'Obrazovka výběru grafu', 'Zobrazuje dva klikatelné náhledy nad stejnými aktuálními hodnotami.', [
    'Spočítá vklady a úroky stejně jako hlavní obrazovka.',
    'Běžný cyklus `for` projde dva názvy a pro každý zavolá `ChartPreview`.',
    'Po kliknutí předá vybraný název přes `onSelect` do hlavní funkce, která jej uloží a vrátí kalkulačku.'
], 'Oba náhledy používají stejná data. Rozdíl je jen ve zvoleném typu grafu.', 'Obrazovky')
entry('ChartPreview', 'Klikatelný náhled grafu', 'Karta obsahující označení typu a ukázku grafu.', [
    '`Modifier.clickable` udělá klikatelnou celou kartu, ne pouze nápis.',
    '`Role.Button` označí její význam také pro přístupnost. Kliknutí spustí předanou funkci `onSelect`.',
    'Uvnitř karty je `Text` s názvem a `InterestChart` se zadaným typem a daty.'
], 'Náhled spojuje kartu, název, graf a kliknutí. Samotné uložení volby řeší rodičovská obrazovka.')
entry('SettingsScreen', 'Obrazovka nastavení', 'Skládá čtyři pojmenované skupiny nastavení.', [
    'Krok vkladu, maximum vkladu, barva grafu a režim spoření mají vlastní komponenty.',
    'Všem skupinám předá tentýž `SavingsState`, takže změny ihned používá kalkulačka.',
    'Na této úrovni jsou pouze volání komponent; detaily jednotlivých voleb zůstávají uvnitř nich.'
], 'Nastavení je složené z menších celků, z nichž každý řeší jednu konkrétní skupinu parametrů.', 'Obrazovky')
entry('DepositStepSettings', 'Krok počátečního vkladu', 'Volba mezi kroky 1 000, 5 000 a 10 000 Kč.', [
    'Cyklus projde tři hodnoty typu `Float` a pro každou vytvoří `ChoiceRow`.',
    'Porovnání `state.depositStep == step` určí označenou možnost.',
    'Kliknutí zavolá `changeDepositStep`, která uloží krok a přizpůsobí aktuální vklad.',
    '`selectableGroup()` označí související radiové volby jako jednu skupinu.'
], 'Seznam kroků je obyčejný seznam čísel. Nemám generickou funkci; každý řádek dostane konkrétní text a akci.')
entry('DepositRangeSettings', 'Maximum počátečního vkladu', 'Volba maxima 100 000, 500 000 nebo 1 000 000 Kč.', [
    'Cyklus vytvoří jednu `ChoiceRow` pro každé povolené maximum.',
    'Označená volba odpovídá `state.depositMaximum`.',
    '`changeDepositMaximum` po změně omezí také aktuální vklad, aby se nedostal za nové maximum.'
], 'Při snížení maxima nestačí upravit rozsah posuvníku. Omezím také aktuální hodnotu vkladu.')
entry('ChartColorSettings', 'Barva grafu', 'Volba zelené, modré nebo oranžové barvy grafů.', [
    'Cyklus prochází tři názvy barev. Vybraný název je uložený ve `state.graphColor`.',
    'Kliknutí změní textovou hodnotu. Funkce `graphColor` ji při vykreslení převede na skutečnou `Color`.',
    'Stejná volba se používá v hlavním grafu i v obou náhledech.'
], 'Ve stavu ukládám název barvy a při vykreslení ho převádím na Color. Oba grafy používají tutéž volbu.')
entry('ChoiceRow', 'Jedna radiová volba', 'Popisek a RadioButton tvoří jeden klikatelný řádek.', [
    'Má konkrétní parametry `text: String`, `selected: Boolean` a `onClick: () -> Unit`. Žádné `<T>` zde není.',
    '`ListItem` řeší běžné rozložení popisku a tlačítka. Kliknutí obsluhuje celý řádek přes `selectable`.',
    'Proto má vnitřní `RadioButton` své `onClick = null`; akce není nastavená podruhé na dvou místech.'
], 'Tato komponenta jen zobrazí jednu volbu. Význam volby a změnu hodnoty dostane od rodiče.')
entry('SavingsModeSettings', 'Daň a režim spoření', 'Dva přepínače a vysvětlení způsobu úročení.', [
    'První přepínač mění `taxEnabled`, druhý `regularDeposit`.',
    'Pravidelné vklady znamenají měsíční úročení a vklad na konci měsíce. Bez nich se úročí ročně.',
    'Podmínka `if/else` zobrazuje odpovídající vysvětlení. Přepnutí mění také počet posuvníků v kalkulačce.'
], 'Přepínače pouze mění Boolean hodnoty. Podle nich se potom vybírá způsob výpočtu i obsah kalkulačky.')
entry('Toggle', 'Přepínač s popiskem', 'Spojuje text a Switch do klikatelného řádku.', [
    '`checked` určuje stav přepínače. `onChange` je akce pro jeho změnu.',
    '`ListItem` umístí popisek a přepínač. `toggleable` umožní kliknout na celý řádek.',
    'Vnitřní `Switch` má `onCheckedChange = null`, protože změnu zpracovává už řádek.'
], 'Společný přepínač dostane aktuální stav a funkci, která novou Boolean hodnotu uloží.')
entry('HistoryScreen', 'Obrazovka historie', 'Tlačítko pro uložení aktuálního výpočtu a seznam uložených záznamů.', [
    'Kliknutí na běžný `Button` zavolá `state.saveCalculation()`.',
    '`HistoryList` dostane seznam textů ze společného stavu a postará se o jejich zobrazení.',
    'Historie je uložená pouze v paměti. Po novém spuštění aplikace není trvalý archiv obnovený.'
], 'Historie ukládá textový snímek aktuálních vstupů a výsledku. Pozdější změna vstupů už tento text nezmění.', 'Obrazovky')
entry('HistoryList', 'Seznam historie', 'Zobrazuje buď zprávu o prázdné historii, nebo jednotlivé karty záznamů.', [
    'Vstupem je `List<String>`, tedy seznam textových záznamů.',
    '`isEmpty()` kontroluje prázdný seznam. Když je prázdný, zobrazí se jedna vysvětlující zpráva.',
    'Cyklus `for` předá každý text do `HistoryEntry`. `Column` rozloží karty pod sebe.'
], 'Seznam historii nevytváří, jen ji zobrazuje. Prázdný stav i všechny záznamy řeší na jednom místě.')
entry('HistoryEntry', 'Karta uloženého záznamu', 'Jeden dříve uložený výpočet v bílé kartě.', [
    'Dostane hotový text `calculation`; výpočty se zde znovu neprovádějí.',
    '`Section` poskytne stejnou kartu jako zbytku aplikace a `Text` zobrazí záznam včetně zalomení řádků.'
], 'Karta historie zobrazuje hotový snímek. Není propojená s později upravenými vstupy.')
entry('Section', 'Společný obal karty', 'Stejný jednoduchý rám pro výsledek, graf, ovládání a historii.', [
    '`Card` vyplní dostupnou šířku. Vnitřní `Column` přidá základní odsazení.',
    '`content` je předaná composable funkce. Voláním `content()` se uvnitř karty vykreslí obsah zapsaný ve složených závorkách za `Section`.',
    'Například `Section { Text(...) }` znamená: vytvoř kartu a do jejího obsahu vlož tento text.'
], 'Section sjednocuje vzhled karet. Předaný obsah může být výsledek, graf nebo několik ovládacích prvků.')
entry('InterestChart', 'Výběr vykreslovaného grafu', 'Společný vstup pro oba typy grafů a ošetření nulových dat.', [
    'Když jsou vklady i úroky nulové, vypíše zprávu a skončí pomocí `return`.',
    'Podle `chartType` zavolá `BarInterestChart` nebo `DonutInterestChart`.',
    'Vlastní tvary grafu kreslí AAY-chart. Naše komponenty připravují data a parametry.'
], 'Nejdřív ošetřím nulu, potom běžnou podmínkou vyberu jednu ze dvou komponent grafu.')
entry('BarInterestChart', 'Sloupcový graf', 'Porovnává celkové vklady s vydělanými úroky.', [
    'Dva objekty `BarParameters` obsahují název, seznam s jednou hodnotou a barvu.',
    'Vyšší částka odpovídá 100 %. Každá hodnota pro graf je částka / maximum × 100; skutečné koruny jsou vypsané nad grafem.',
    '`Box` omezí výšku grafu uvnitř posouvatelného obsahu. Knihovna vykreslí sloupce, osu a legendu.',
    '`animateChart = false` vypne animaci a `LegendPosition.BOTTOM` umístí legendu dolů.'
], 'Graf dostává dvě hodnoty přepočtené na společné procentní měřítko. Částky v korunách vypisuji nad něj.')
entry('DonutInterestChart', 'Prstencový graf', 'Ukazuje podíl celkových vkladů a úroků na výsledné sumě.', [
    'Dva objekty `PieChartData` obsahují přímo částky, názvy a barvy.',
    'Z poměru částek knihovna dopočítá velikost výsečí a procenta. Zde hodnoty na 100 nepřepočítáváme sami.',
    'Barva vkladů je světlejší. `tween(durationMillis = 0)` znamená změnu bez časového přechodu.'
], 'Prstencovému grafu předávám skutečné částky. Knihovna z jejich součtu určí procentní podíly.')
entry('MainActivity', 'Spuštění a barevné téma', 'Vstupní bod Android aplikace a nastavení jejích barev.', [
    '`onCreate` se zavolá při vytvoření aktivity. `setContent` nastaví rozhraní napsané pomocí Compose.',
    '`MaterialTheme` nastaví tyrkysové ovládání, světlé pozadí a bílé karty pro celou aplikaci.',
    'Uvnitř tématu zavolá `InterestScreen`, která už řeší společný stav a obrazovky.'
], 'Aktivita spustí Compose a nastaví barevné téma. Vlastní obsah aplikace vytváří InterestScreen.', 'Princip a výpočty')
entry('SavingsState', 'Společná data aplikace', 'Jeden objekt obsahuje vstupy, nastavení a historii.', [
    'Sledované proměnné používají `mutableStateOf`. Při jejich změně Compose aktualizuje části rozhraní, které hodnotu čtou.',
    'Počáteční vklad, měsíční vklad, sazba a roky jsou `Float`, protože tento typ používá Slider. Pro výpočet je převádíme na `Double`.',
    'Tentýž objekt dostávají jednotlivé obrazovky, takže si předávají aktuální hodnoty bez jejich kopírování.',
    'Objekt vzniká pomocí `remember`; není uložený do databáze ani pomocí `rememberSaveable`. Nové spuštění nebo vytvoření aktivity obnoví výchozí stav.'
], 'Vstupy držím v jednom společném objektu. mutableStateOf propojuje změnu dat s aktualizací jejich zobrazení.', 'Princip a výpočty')
entry('totalAmount', 'Výpočet konečné sumy', 'Vybere výpočet podle režimu spoření a nastavení daně.', [
    'Zapnutá daň předá hodnotu 15 %, vypnutá hodnotu 0 %.',
    'Při pravidelných vkladech zavolá `monthlySavings`, jinak `compoundInterest`.',
    'Zapnutý měsíční režim zůstává měsíčním i při nastavení měsíčního vkladu na 0 Kč.'
], 'Způsob úročení vybírám podle přepínače režimu, ne podle toho, jestli je měsíční částka nulová.', 'Princip a výpočty')
entry('totalDeposits', 'Součet vložených peněz', 'Spočítá peníze vložené uživatelem bez přidaných úroků.', [
    'Začne počátečním vkladem.',
    'Pouze při zapnutých pravidelných vkladech přičte měsíční vklad × počet let × 12.',
    'Odčtením této hodnoty od konečné sumy se získají úroky.'
], 'Celkové vklady jsou počáteční částka a případně všechny měsíční platby. Úroky se do této hodnoty nepočítají.', 'Princip a výpočty')
entry('changeDepositStep', 'Změna kroku vkladu', 'Uloží nový krok a zaokrouhlí aktuální počáteční vklad.', [
    'Dělením vkladu krokem zjistí počet kroků a zaokrouhlí ho na celé číslo.',
    'Vynásobením zpět získá částku odpovídající novému kroku.',
    '`coerceIn` zajistí, že hodnota nepřekročí současné maximum.'
], 'Při změně kroku srovnám také aktuální vklad na platnou hodnotu.', 'Princip a výpočty')
entry('changeDepositMaximum', 'Změna maxima vkladu', 'Aktualizuje konec rozsahu a omezí současný vklad.', [
    'Nejdřív nastaví `depositMaximum`.',
    '`coerceIn(0f, maximum)` zmenší příliš velký vklad na nové maximum. Menší vklad zůstane stejný.'
], 'Tato metoda udržuje vstup v platném rozsahu i po jeho zmenšení.', 'Princip a výpočty')
entry('saveCalculation', 'Vytvoření snímku historie', 'Připraví hodnoty, sestaví text a přidá záznam do seznamu.', [
    'Konečnou částku vypočítá jednou. Pojmenované proměnné drží úroky, vklad, sazbu a období.',
    'Obyčejné podmínky určí popis úročení, skutečně používaný měsíční vklad a sazbu daně.',
    'Víceřádkový text obsahuje pouze připravené hodnoty. `trimIndent()` odstraní společné odsazení zdrojového kódu.',
    '`history = history + calculation` vytvoří seznam doplněný o nový hotový text. Původní záznamy se nemění.'
], 'Historii neukládám jako odkaz na vstupy. Uložím již vytvořený text, takže zachová hodnoty platné v okamžiku uložení.', 'Princip a výpočty')
entry('compoundInterest', 'Roční složené úročení', 'Počáteční vklad úročený jednou ročně bez dalších vkladů.', [
    '`netRate` převede roční procentní sazbu na desetinné číslo a případně ji sníží o podíl daně.',
    'Vzorec je počáteční vklad × (1 + čistá sazba) na počet let. `pow(years)` znamená umocnění.',
    'V této aplikaci je sazba daně volitelná: běžně 0 %, při zapnutém přepínači 15 %.'
], 'Používám vzorec složeného úroku. Po každém roce se úročí i dříve připsané úroky.', 'Princip a výpočty')
entry('monthlySavings', 'Měsíční úročení a vklady', 'Postupně nasimuluje každý měsíc spoření.', [
    'Čistou roční sazbu vydělí 12 a získá měsíční sazbu.',
    'Běžný cyklus `for` projde počet let × 12 měsíců.',
    'V každém kroku nejdřív přičte úrok násobením (1 + měsíční sazba), potom přidá vklad. Vklad je tedy na konci měsíce.',
    'Při nulové sazbě se pouze sčítají vklady; není potřeba větev kvůli dělení nulou.'
], 'Výpočet simuluje jednotlivé měsíce. Pořadí úrok, potom vklad znamená vklad na konci měsíce.', 'Princip a výpočty')
entry('money', 'Formátování korun', 'Převede číslo na čitelný český text s jednotkou Kč.', [
    '`NumberFormat` používá české oddělovače a skupiny tisíců.',
    '`maximumFractionDigits = 0` zobrazí celé koruny. Samotný výpočet dál pracuje s nezaokrouhlenými čísly.'
], 'Zaokrouhluji až text pro zobrazení. Ve výpočtu zůstává přesná hodnota typu Double.', 'Princip a výpočty')
entry('graphColor', 'Převod názvu na barvu', 'Z textové volby v nastavení získá Color pro kreslení grafu.', [
    'Obyčejná podmínka `if/else` rozliší zelenou, modrou a zbývající oranžovou.',
    'Stejný převod používá hlavní karta grafu i náhledy.'
], 'Nastavení drží čitelný název a tato funkce ho převede na barvu, kterou potřebuje graf.', 'Princip a výpočty')

for name, c in components.items():
    body = re.sub(r'^\s*(?:fun|class)\s+[^\n]*', '', c['code'], flags=re.M)
    c['children'] = [other for other in components if other != name and re.search(r'\b' + re.escape(other) + r'\s*\(', body)]
    c['parents'] = []
for name, c in components.items():
    for child in c['children']:
        components[child]['parents'].append(name)

def rect(bounds):
    return list(map(int, re.findall(r'\d+', bounds)))

def screen(slug, title, caption, default, parent_name):
    data = (ASSETS / (slug + '.png')).read_bytes()
    width, height = struct.unpack('>II', data[16:24])
    tree = ET.parse(ASSETS / (slug + '.xml')).getroot()
    parents = {child: parent for parent in tree.iter() for child in parent}
    nodes = list(tree.iter('node'))
    result = dict(id=slug, title=title, caption=caption, default=default, parent=parent_name,
                  image='data:image/png;base64,' + base64.b64encode(data).decode(), width=width, height=height, regions=[])
    def text(prefix):
        return next(n for n in nodes if (n.get('text') or '').startswith(prefix))
    def region(name, label, b, related=()):
        x1,y1,x2,y2 = b
        y1,y2=max(0,y1),min(height,y2)
        assert 0 <= x1 < x2 <= width and 0 <= y1 < y2 <= height
        result['regions'].append(dict(component=name, label=label, related=list(related),
            x=x1/width*100,y=y1/height*100,w=(x2-x1)/width*100,h=(y2-y1)/height*100))
    if slug == 'menu':
        boxes = [rect(parents[text(p)].get('bounds')) for p in ['Chart-Type','Settings','History']]
        region('AppMenu','Rozbalovací menu', [min(b[0] for b in boxes), min(b[1] for b in boxes)-20,
               max(b[2] for b in boxes), max(b[3] for b in boxes)+20])
        return result
    region('AppHeader', 'Hlavička', [0,120,1080,269])
    region('AppMenu', 'Menu', [913,143,1039,269])
    if slug.startswith('kalkulacka'):
        region('SavingsSummary','Výsledek',rect(parents[text('Naspořená suma:')].get('bounds')),['money'])
        controls = rect(parents[text('Měsíční vklad:')].get('bounds'))
        region('SavingsChartCard','Graf', [42,532,1038,controls[1]-32],['InterestChart', 'BarInterestChart' if slug.endswith('sloupce') else 'DonutInterestChart'])
        region('SavingsControls','Skupina posuvníků',controls)
        labels = ['Měsíční vklad:', 'Počáteční vklad:', 'Úrok:', 'Období:']
        for prefix in labels:
            n=text(prefix)
            y=rect(n.get('bounds'))[1]
            region('SavingsSlider',prefix[:-1],[74,y,1006,y+184],['SavingsControls'])
    elif slug == 'vyber-grafu':
        for prefix in ['Sloupcový graf','Prstencový graf']:
            region('ChartPreview',prefix,rect(parents[text(prefix)].get('bounds')),
                   ['BarInterestChart' if prefix.startswith('Sloupcový') else 'DonutInterestChart'])
    elif slug.startswith('nastaveni'):
        for name,prefix,label in [
            ('DepositStepSettings','Krok posuvníku','Krok vkladu'),
            ('DepositRangeSettings','Maximální počáteční','Maximum vkladu'),
            ('ChartColorSettings','Barva grafu','Barva grafu')]:
            region(name,label,rect(parents[text(prefix)].get('bounds')),['ChoiceRow'])
        tax=rect(parents[text('Daň z úroků')].get('bounds'))
        regular=rect(parents[text('Pravidelný měsíční')].get('bounds'))
        region('SavingsModeSettings','Režim spoření',[42,tax[1],1038,2319])
        region('Toggle','Daň',tax,['SavingsModeSettings'])
        region('Toggle','Měsíční vklady',regular,['SavingsModeSettings'])
    elif slug == 'historie':
        region('HistoryScreen','Uložit výpočet',rect(parents[text('Uložit aktuální')].get('bounds')),['saveCalculation'])
        region('HistoryEntry','Uložený záznam',rect(parents[text('Měsíční úročení')].get('bounds')),['HistoryList','Section'])
    return result

screens = [
    screen('kalkulacka','Kalkulačka','Pravidelné vklady · prstencový graf','SavingsSummary','CalculatorScreen'),
    screen('kalkulacka-sloupce','Sloupcový graf','Stejné vstupy, jiný způsob zobrazení','BarInterestChart','CalculatorScreen'),
    screen('menu','Menu','Otevřená nabídka nad kalkulačkou','AppMenu','InterestScreen'),
    screen('vyber-grafu','Výběr grafu','Dva klikatelné náhledy aktuálních hodnot','ChartPreview','ChartTypeScreen'),
    screen('nastaveni','Nastavení','Krok, maximum, barva a režim spoření','DepositStepSettings','SettingsScreen'),
    screen('nastaveni-rezim','Nastavení dole','Po posunutí je vidět i celý popis režimu','SavingsModeSettings','SettingsScreen'),
    screen('historie','Historie','Skutečný uložený záznam výchozího výpočtu','HistoryEntry','HistoryScreen'),
]
assert len([c for c in components.values() if c['code'].startswith('@Composable')]) == 24
assert all(r['component'] in components for s in screens for r in s['regions'])
payload = json.dumps(dict(components=components,screens=screens,source=source), ensure_ascii=False).replace('<','\\u003c')
template = (ASSETS / 'sablona.html').read_text(encoding='utf-8')
output = ROOT / 'pruvodce-aplikaci.html'
output.write_text(template.replace('__GUIDE_DATA__',payload), encoding='utf-8')
print(f'Hotovo: {output} / {len(components)} vysvětlení / {len(screens)} screenshotů')
