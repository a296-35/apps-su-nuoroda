# Apps su nuoroda — Android APK build

## 🚀 Kaip paleisti GitHub Actions build'ą

### 1. Sukurk GitHub repozitoriją

```bash
# Prisijunk prie GitHub ir sukurk naują repozitoriją (public arba private)
# Pvz.: https://github.com/<vartotojas>/apps-su-nuoroda
```

### 2. Įkelk kodą

```bash
cd /root/android-app-generator
git init
git add .
git commit -m "Pirmas commit'as - Android programa"
git remote add origin https://github.com/<vartotojas>/apps-su-nuoroda.git
git branch -M main
git push -u origin main
```

### 3. Paleisk build'ą

- GitHub Actions paleidžiamas **automatiškai** po push'o
- Arba eik į GitHub → Actions → "Build Android APK" → "Run workflow" (rankinis paleidimas)

### 4. Atsisiųsk APK

- Po build'o eik į Actions → pasirink run'ą → **Artifacts** skyrius
- Atsisiųsk `apps-su-nuoroda-debug.zip`
- Išarchyvuok → gausi `app-debug.apk`

### 5. Įdiek į telefoną

```bash
# Nusiųsk APK į telefoną (per USB arba cloud)
# Telefone: Nustatymai → Sauga → Leisti diegti iš nežinomų šaltinių
# Atidaryk APK failą → Įdiegti
```

---

## 📱 Programėlės veikimas telefone

1. Atsidaro pagrindinis langas su įvesties laukais
2. Įvesk pavadinimą + URL + aprašymą
3. Paspausk "✨ Sukurti naują appsą"
4. Sugeneruotas appsas atsiranda sąraše
5. Paspaudus → atsidaro WebView viduje
6. Ilgai palaikius → meniu (redaguoti / trinti / atidaryti naršyklėje)

---

## ⚡ Trumpas variantas (be GitHub)

Jei nori išbandyti greitai:

1. Nukopijuok `android-app-generator/` į savo Windows/Mac kompą
2. Atsisiųsk ir įdiek Android Studio
3. File → Open → pasirink `android-app-generator/`
4. Paspausk žalią **Run** mygtuką ▶️
