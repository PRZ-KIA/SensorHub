# SensorHub

> Edukacyjna aplikacja Android do nauki integracji sensorów, architektury MVVM i nowoczesnego UI w Jetpack Compose.

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![MinSDK](https://img.shields.io/badge/MinSDK-26-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## Spis treści
- [O projekcie](#o-projekcie)
- [Najważniejsze funkcje](#najważniejsze-funkcje)
- [Architektura i przepływ danych](#architektura-i-przepływ-danych)
- [Struktura katalogów](#struktura-katalogów)
- [Wymagania](#wymagania)
- [Szybki start](#szybki-start)
- [Uruchamianie i testowanie](#uruchamianie-i-testowanie)
- [Jak rozwijać projekt](#jak-rozwijać-projekt)
- [Na co uważać](#na-co-uwazać)
- [Tech stack](#tech-stack)

## O projekcie

**SensorHub** to projekt edukacyjny, który pokazuje jak budować nowoczesną aplikację Android z wykorzystaniem:
- odczytów z fizycznych sensorów urządzenia,
- reaktywnego strumieniowania danych (`Flow`),
- trwałego zapisu historii pomiarów (`Room`),
- dependency injection (`Hilt`),
- UI opartego o `Jetpack Compose`.

Projekt jest dobrym punktem startowym dla osób uczących się Androida, bo zawiera pełny „pipeline” od warstwy sprzętowej aż po warstwę prezentacji.

## Najważniejsze funkcje

### Sensory
- Akcelerometr
- Żyroskop
- Magnetometr
- Czujnik światła
- GPS
- Czujnik zbliżeniowy
- Barometr

### Interakcje i UX
- Gesty (dotyk, przesuwanie, pinch/zoom)
- Rozpoznawanie głosu
- Haptyka (wibracje)

### Dane i analityka
- Historia pomiarów zapisywana lokalnie
- Podstawowe analizy trendów i porównań
- Eksport danych

## Architektura i przepływ danych

Aplikacja wykorzystuje architekturę **MVVM** z wyraźnym podziałem odpowiedzialności.

1. **`sensors/*Manager`**
   - Nasłuchują danych z Android Sensor API i emitują je jako `Flow`.
2. **`data/repository/SensorRepository`**
   - Udostępnia dane do ViewModeli.
   - Opcjonalnie zapisuje odczyty do bazy.
3. **`viewmodel/*ViewModel`**
   - Kolekcjonują strumienie, budują `uiState`, obsługują błędy.
4. **`ui/screens` + Compose**
   - Renderują aktualny stan i reagują na akcje użytkownika.

Taki podział upraszcza testowanie, rozwój i utrzymanie kodu.

## Struktura katalogów

```text
app/src/main/java/com/example/sensorhub/
├── affective/          # Funkcje związane z affective computing
├── analytics/          # Analityka i metryki
├── data/
│   ├── database/       # Room: DAO, Database, migracje
│   ├── export/         # Eksport danych
│   ├── model/          # Modele danych sensorów
│   ├── preferences/    # DataStore / ustawienia użytkownika
│   └── repository/     # Repozytorium łączące warstwy
├── di/                 # Moduły Hilt
├── notifications/      # Obsługa powiadomień
├── sensors/            # Managerowie sensorów
├── service/            # Foreground service
├── ui/
│   ├── components/     # Komponenty wielokrotnego użycia
│   ├── navigation/     # Route’y i struktura nawigacji
│   ├── screens/        # Ekrany Compose
│   ├── theme/          # Theme, kolory, typografia
│   └── animations/     # Animacje UI
├── utils/              # Narzędzia pomocnicze
├── viewmodel/          # ViewModele
├── workers/            # Zadania WorkManager
├── MainActivity.kt
└── SensorHubApplication.kt
```

## Wymagania

- Android Studio (najnowsza stabilna wersja)
- JDK 17+
- Android SDK (minSdk 26)
- Urządzenie fizyczne (zalecane do testów sensorów)

## Szybki start

```bash
git clone <URL_REPOZYTORIUM>
cd SensorHub
./gradlew clean
./gradlew assembleDebug
```

Po udanym buildzie APK znajdziesz w:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Uruchamianie i testowanie

### Build
```bash
./gradlew build
```

### Testy jednostkowe
```bash
./gradlew test
```

### Lint
```bash
./gradlew lint
```

### Testy instrumentacyjne
```bash
./gradlew connectedCheck
```

## Jak rozwijać projekt

### Dodanie nowego sensora (skrót)
1. Dodaj model danych w `data/model`.
2. Dodaj manager sensora w `sensors/` (emisja `Flow`).
3. Rozszerz `SensorRepository` o nowe API.
4. Dodaj ViewModel i ekran Compose.
5. Dodaj route w `ui/navigation` i wpis do `NavHost`.
6. W razie potrzeby dodaj persystencję do `Room`.

### Dobre praktyki
- Trzymaj logikę biznesową poza Composable.
- Nie czytaj sensorów bezpośrednio w UI.
- Każdy ekran powinien opierać się o jawny `uiState`.
- Ograniczaj rozmiar historii danych w pamięci.
- Sprawdzaj dostępność sensorów na urządzeniu.

## Na co uważać

- W repo istnieje również starsza/alternatywna przestrzeń nazw `com.kia.sensorhub`.
  Główna aplikacja działa na `com.example.sensorhub`.
- Część funkcji wymaga runtime permissions (np. lokalizacja, mikrofon, notyfikacje).
- Nie wszystkie urządzenia mają wszystkie sensory – aplikacja to obsługuje, ale warto testować na różnych modelach.

## Tech stack

- **Kotlin**
- **Jetpack Compose + Material 3**
- **MVVM (ViewModel + StateFlow)**
- **Coroutines + Flow**
- **Room**
- **Hilt**
- **WorkManager**
- **Navigation Compose**

---

Jeśli chcesz rozpocząć onboarding zespołu, najlepsza ścieżka to przejście przez jeden pełny przypadek: 
**`AccelerometerManager -> SensorRepository -> AccelerometerViewModel -> AccelerometerScreen`**.
