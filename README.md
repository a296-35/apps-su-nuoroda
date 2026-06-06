# 📱 **Apps su nuoroda Generatorius** — v1.1

Android programėlė, kuri leidžia **kurti, redaguoti ir trinti** atskirus "appsu" (HTML puslapius) su nuorodomis į svetaines. Kiekvienas sugeneruotas puslapis yra nepriklausomas ir gali būti atidarytas kaip atskiras WebView arba naršyklėje.

**🔗 GitHub:** https://github.com/a296-35/apps-su-nuoroda
**📦 APK:** GitHub Actions → Artifacts

---

## 📋 Turinys

- [Funkcijos](#-funkcijos)
- [Versijų istorija](#-versijų-istorija)
- [Reikalavimai](#-reikalavimai)
- [Kaip paleisti](#-kaip-paleisti)
- [Kaip naudotis](#-kaip-naudotis)
- [Projekto struktūra](#-projekto-struktūra)
- [Kaip veikia](#-kaip-veikia)
- [Auto-build su GitHub Actions](#-auto-build-su-github-actions)
- [PWA (atskira programėlė namų ekrane)](#-kaip-padaryti-kad-appsas-atrodytų-kaip-atskira-programėlė-pwa)
- [Galimos problemos](#-galimos-problemos)

---

## ✅ Funkcijos

| Funkcija | Kaip veikia |
|---|---|
| **Sukurti naują appsą** | Įvedi pavadinimą, URL, aprašymą → pasirenki ikoną → sugeneruojamas HTML |
| **10 ikonų** | Raudona, Žalia, Mėlyna, Oranžinė, Violetinė, Cian, Rožinė, Pilka, Ruda, Indigo |
| **Atidaryti viduje** | Paspaudi ant apps'o → atsidaro WebView šioje programėlėje |
| **Atidaryti naršyklėje** | Ilgas paspaudimas → "Atidaryti naršyklėje" |
| **Redaguoti** | Ilgas paspaudimas → "Redaguoti" → gali keist pavadinimą, URL, aprašymą, ikoną |
| **Trinti** | Ilgas paspaudimas → "Trinti" |
| **Duomenys išlieka** | Visi duomenys saugomi SQLite + HTML failai vidiniame storage |
| **Nepriklausomi** | Kiekvienas appsas turi unikalų ID ir savo HTML failą |
| **Klaidų pranešimai** | Visos klaidos rodomos Toast žinutėmis |
| **URL validacija** | Tikrina ar URL prasideda `http://` arba `https://` |
| **Tuščias sąrašas** | Rodo pranešimą kai nėra appsų |

---

## 📋 Versijų istorija

### v1.1 (2026-06-06)
- ✅ Pridėtos 10 spalvotų ikonų (pasirinkimas kuriant ir redaguojant)
- ✅ Error handling — visos klaidos rodomos Toast žinutėmis
- ✅ URL validacija — tikrina ar prasideda `http://` ar `https://`
- ✅ Ikonos rodomos sąraše šalia pavadinimo
- ✅ Tuščias sąrašas rodo pranešimą "Dar nėra sukurtų appsų"
- ✅ DB migracija — seni įrašai neištrinami atnaujinus (pridedamas icon stulpelis)
- ✅ Pataisyta XML sintaksė
- ✅ Pridėtas .gitignore

### v1.0 (2026-06-06)
- ✅ Pagrindinis langas su įvesties laukais
- ✅ HTML failų generavimas
- ✅ SQLite duomenų bazė (CRUD)
- ✅ WebView peržiūra
- ✅ Redagavimo / trynimo dialogai
- ✅ GitHub Actions auto-build

---

## 📦 Reikalavimai

- **Android Studio** (Giraffe / Hedgehog / naujesnė)
- **JDK 17**
- **Android SDK 34**
- **Android 5.0+** (API 21+, veiks beveik visuose įrenginiuose)

---

## 🚀 Kaip paleisti

### 1. Atsisiųsk APK (rekomenduojama)

Nueik į GitHub Actions → pasirink naujausią build'ą → **Artifacts** → `apps-su-nuoroda-debug.zip` → išarchyvuok → įdiek `app-debug.apk` į telefoną.

### 2. Arba atidaryk Android Studio

```
File → Open → Pasirink android-app-generator/ katalogą
```

Palauk kol Gradle sinc'uosis, tada `Run`.

---

## 📱 Kaip naudotis

```
1. Įvesk pavadinimą + URL + aprašymą
2. Paspausk "✨ Sukurti naują appsą"
3. Iššoka langas — pasirink ikoną (10 spalvų)
4. Appsas atsiranda sąraše
5. Paspaudus → atsidaro WebView viduje
6. Ilgas paspaudimas → meniu: atidaryti naršyklėje / redaguoti / trinti
```

---

## 📁 Projekto struktūra

```
android-app-generator/
├── .github/workflows/build-apk.yml  # Auto-build GitHub Actions
├── .gitignore
├── build.gradle.kts                 # Root Gradle config
├── settings.gradle.kts              # Projektų sąrašas
├── gradle.properties                # Gradle nustatymai
├── README.md                        # Šis failas
├── BUILD.md                         # Kaip push'inti į GitHub
└── app/
    ├── build.gradle.kts             # App modulio Gradle config
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/weblinkapp/
        │   ├── MainActivity.kt          # Pagrindinis langas + ikonų picker
        │   ├── WebViewActivity.kt        # WebView peržiūrai
        │   ├── AppDatabaseHelper.kt      # SQLite DB valdymas (v2 schema)
        │   ├── AppListAdapter.kt         # Custom adapter su ikonomis
        │   └── App.kt                   # Data klasė su ikonų mapping'u
        └── res/
            ├── layout/
            │   ├── activity_main.xml     # Pagrindinis UI + empty state
            │   └── activity_webview.xml  # WebView UI
            ├── values/
            │   ├── strings.xml
            │   ├── colors.xml
            │   └── themes.xml
            └── drawable/
                ├── edit_text_bg.xml      # Įvesties laukų fonas
                ├── ic_app_red.xml        # 10 ikonų (spalvoti apskritimai)
                ├── ic_app_green.xml
                ├── ic_app_blue.xml
                ├── ic_app_orange.xml
                ├── ic_app_purple.xml
                ├── ic_app_cyan.xml
                ├── ic_app_pink.xml
                ├── ic_app_grey.xml
                ├── ic_app_brown.xml
                └── ic_app_indigo.xml
```

---

## ⚙️ Kaip veikia

### Duomenų srautas

```
Vartotojas įveda: [Pavadinimas] [URL] [Aprašymas]
        │
        ▼
Iššoka ikonų pasirinkimo langas (10 spalvų)
        │
        ▼
Sukuriame HTML failą: app_<timestamp>.html
  (su parinkta ikonos spalva kaip fono gradiento spalva)
        │
        ▼
Įrašome į SQLite DB:
  id | title | url | description | icon | htmlPath
        │
        ▼
Atvaizduojame ListView sąraše su ikona + pavadinimu + URL
        │
        ▼
Paspaudus → WebViewActivity rodo HTML failą
Ilgas paspaudimas → meniu (naršyklė / redaguoti / trinti)
```

### HTML failo vieta

```
/data/data/com.example.weblinkapp/files/app_<timestamp>.html
```

Šie failai išlieka net uždarius programėlę ir yra unikalūs kiekvienam appsui.

### Ikonų spalvos

| Ikonos pavadinimas | Spalva |
|---|---|
| Raudona | `#FF6B6B` |
| Žalia | `#4CAF50` |
| Mėlyna | `#2196F3` |
| Oranžinė | `#FF9800` |
| Violetinė | `#9C27B0` |
| Cian | `#00BCD4` |
| Rožinė | `#E91E63` |
| Pilka | `#607D8B` |
| Ruda | `#795548` |
| Indigo | `#3F51B5` |

---

## 🤖 Auto-build su GitHub Actions

Kiekvieną kartą push'inus kodą į `main`, GitHub Actions automatiškai:
1. Atsisiunčia Gradle 8.5
2. Nustato Android SDK
3. Sukompiliuoja APK
4. Įkelia APK kaip artifact'ą

**Atsisiųsti APK:**
1. Nueik į https://github.com/a296-35/apps-su-nuoroda/actions
2. Pasirink naujausią run'ą
3. Skiltyje **Artifacts** atsisiųsk `apps-su-nuoroda-debug.zip`

---

## 🌐 Kaip padaryti, kad appsas atrodytų kaip atskira programėlė (PWA)

Sugeneruoti HTML puslapiai gali būti paversti **Progressive Web Apps (PWA)**.

### 1. Pridėk manifest.json prie HTML

```html
<link rel="manifest" href="manifest.json">
```

### 2. Sukurk manifest.json

```json
{
  "name": "Mano Appsas",
  "short_name": "Appsas",
  "start_url": "app_123.html",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#667EEA",
  "icons": [
    { "src": "icon.png", "sizes": "192x192", "type": "image/png" }
  ]
}
```

### 3. Pridėk Service Worker

```javascript
// sw.js
self.addEventListener('install', e => self.skipWaiting());
self.addEventListener('activate', e => e.waitUntil(clients.claim()));
```

### 4. Įdiek

Chrome pasiūlys **"Įdiegti į namų ekraną"**.

---

## ⚠️ Galimos problemos

| Problema | Sprendimas |
|---|---|
| **WebView neįsikrauna** | Patikrink `AndroidManifest.xml` ar yra `<uses-permission android:name="android.permission.INTERNET" />` |
| **file:// nuorodos neveikia naršyklėje** | Nuo Android 10+ `file://` URI gali būti blokuojami. Naudok WebView |
| **Gradle klaida** | Patikrink ar naudoji JDK 17 |
| **Po atnaujinimo seni appsai dingę** | DB migracija veikia — seni įrašai lieka. Jei problema — ištrink programėlę ir įdiek iš naujo |
| **Build'as krenta** | Pažiūrėk GitHub Actions log'us — ten matosi tiksli klaida |

---

## 🔧 Pritaikymas

### Pakeisti programėlės pavadinimą
```xml
<!-- res/values/strings.xml -->
<string name="app_name">Mano vardas</string>
```

### Pakeisti spalvas
Failas: `res/values/colors.xml`

### Pridėti daugiau ikonų
1. Sukurk naują vektorinę drawable ikoną `res/drawable/`
2. Pridėk į `iconResArray` masyvą `MainActivity.kt`
3. Pridėk į `getIconDrawableId()` `App.kt`

---

## 📜 Licencija

MIT — gali laisvai naudoti, modifikuoti ir platinti.
