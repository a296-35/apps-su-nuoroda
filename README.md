# 📱 **Apps su nuoroda Generatorius** — Android programa

Android programėlė, kuri leidžia **kurti, redaguoti ir trinti** atskirus "appsu" (HTML puslapius) su nuorodomis į svetaines. Kiekvienas sugeneruotas puslapis yra nepriklausomas ir gali būti atidarytas kaip atskiras WebView arba naršyklėje.

---

## 📋 Turinys

- [Funkcijos](#-funkcijos)
- [Reikalavimai](#-reikalavimai)
- [Kaip paleisti](#-kaip-paleisti)
- [Projekto struktūra](#-projekto-struktūra)
- [Kaip veikia](#-kaip-veikia)
- [Kaip padaryti, kad appsas atrodytų kaip atskira programėlė (PWA)](#-kaip-padaryti-kad-appsas-atrodytų-kaip-atskira-programėlė-pwa)
- [Galimos problemos](#-galimos-problemos)

---

## ✅ Funkcijos

| Funkcija | Kaip veikia |
|---|---|
| **Sukurti naują appsą** | Įvedi pavadinimą, URL, aprašymą → sugeneruojamas HTML failas |
| **Atidaryti viduje** | Paspaudi ant apps'o → atsidaro WebView šioje programėlėje |
| **Atidaryti naršyklėje** | Ilgas paspaudimas → "Atidaryti naršyklėje" |
| **Redaguoti** | Ilgas paspaudimas → "Redaguoti" → pakeičia pavadinimą/URL/aprašymą |
| **Trinti** | Ilgas paspaudimas → "Trinti" |
| **Duomenys išlieka** | Visi duomenys saugomi SQLite + HTML failai vidiniame storage |
| **Nepriklausomi** | Kiekvienas appsas turi unikalų ID ir savo HTML failą |

---

## 📦 Reikalavimai

- **Android Studio** (Giraffe / Hedgehog / naujesnė)
- **JDK 17**
- **Android SDK 34**
- **Android 5.0+** (API 21+, veiks beveik visuose įrenginiuose)

---

## 🚀 Kaip paleisti

### 1. Atsisiųsk arba nukopijuok projektą

```bash
# Nukopijuok visą projektą į savo kompiuterį
# arba sukurk naują Android Studio projektą ir pakeisk failus
```

### 2. Atidaryk Android Studio

```
File → Open → Pasirink android-app-generator/ katalogą
```

### 3. Palauk, kol Gradle sinc'uosis

Android Studio automatiškai atsisiųs reikiamas bibliotekas.

### 4. Paleisk

```
Prijunk telefoną (USB debugging ON) arba naudok emuliatorių
Run → pasirink įrenginį
```

### 5. Naudok

```
Įvesk pavadinimą + URL → "Sukurti naują appsą"
Paspaudus ant apps'o → atsidaro WebView viduje
Ilgas paspaudimas → meniu (atidaryti naršyklėje / redaguoti / trinti)
```

---

## 📁 Projekto struktūra

```
android-app-generator/
├── build.gradle.kts              # Root Gradle config
├── settings.gradle.kts           # Projektų sąrašas
├── gradle.properties             # Gradle nustatymai
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts          # App modulio Gradle config
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/weblinkapp/
        │   ├── MainActivity.kt       # Pagrindinis langas
        │   ├── WebViewActivity.kt     # WebView peržiūrai
        │   ├── AppDatabaseHelper.kt   # SQLite DB valdymas
        │   └── App.kt                # Data klasė
        └── res/
            ├── layout/
            │   ├── activity_main.xml  # Pagrindinis UI
            │   └── activity_webview.xml # WebView UI
            ├── values/
            │   ├── strings.xml
            │   ├── colors.xml
            │   └── themes.xml
            └── drawable/
                └── edit_text_bg.xml   # Įvesties laukų fonas
```

---

## ⚙️ Kaip veikia

### Duomenų srautas

```
Vartotojas įveda: [Pavadinimas] [URL] [Aprašymas]
        │
        ▼
Sukuriame HTML failą: app_<timestamp>.html
        │
        ▼
Įrašome į SQLite DB: id | title | url | description | htmlPath
        │
        ▼
Atvaizduojame ListView sąraše
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

---

## 🌐 Kaip padaryti, kad appsas atrodytų kaip atskira programėlė (PWA)

Sugeneruoti HTML puslapiai gali būti paversti **Progressive Web Apps (PWA)**. Tada vartotojas gali juos įsirašyti į namų ekraną kaip atskiras programėles.

### 1. Pridėk manifest.json prie HTML

```html
<link rel="manifest" href="manifest.json">
```

### 2. Sukurk manifest.json šalia HTML failo

```json
{
  "name": "Mano Appsas",
  "short_name": "Appsas",
  "start_url": "app_123.html",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#667EEA",
  "icons": [
    {
      "src": "icon.png",
      "sizes": "192x192",
      "type": "image/png"
    }
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

Kai vartotojas atidaro HTML puslapį per naršyklę (ne per WebView), Chrome pasiūlys:
```
➕ Įdiegti į namų ekraną
```

### PWA alternatyva — serverio sprendimas

Jei nori, kad appsai veiktų iš bet kurio įrenginio:
- Įkelk HTML failus į serverį (pvz., `https://tavo-domenas.com/apps/app_123.html`)
- Nuoroda bus pasiekiama iš bet kur

---

## ⚠️ Galimos problemos

| Problema | Sprendimas |
|---|---|
| **WebView neįsikrauna** | Patikrink `AndroidManifest.xml` ar yra `<uses-permission android:name="android.permission.INTERNET" />` |
| **file:// nuorodos neveikia naršyklėje** | Nuo Android 10+ `file://` URI gali būti blokuojami. Naudok WebView vietoj to |
| **Duomenys dingsta uždarius** | Visi duomenys saugomi SQLite + failuose, jie neištrinami |
| **Gradle klaida** | Patikrink ar naudoji JDK 17 ir Android Studio naujausią versiją |
| **MinSdk problema** | Projektas naudoja API 21 (Android 5.0). Jei reikia senesnės versijos — pakeisk `app/build.gradle.kts` |
| **Neranda R.layout.activity_webview** | Įsitikink, kad `activity_webview.xml` sukurtas `res/layout/` kataloge |
| **WebViewActivity nepridėta į manifestą** | Pridėk į `AndroidManifest.xml`:

```xml
<activity android:name=".WebViewActivity" />
```

---

## 🔧 Pritaikymas

### Pakeisti programėlės pavadinimą
Failas: `res/values/strings.xml`
```xml
<string name="app_name">Mano vardas</string>
```

### Pakeisti spalvas
Failas: `res/values/colors.xml`

### Pridėti daugiau laukų
Pridėk `EditText` į `activity_main.xml` ir atitinkamą kintamąjį `MainActivity.kt`.

---

## 📜 Licencija

MIT — gali laisvai naudoti, modifikuoti ir platinti.
