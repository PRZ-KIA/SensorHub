# Proponowane zadania po przeglądzie kodu

## 1) Zadanie: poprawić literówkę w instrukcji uruchomienia
**Problem:** W README użyto komendy `cd sensorhub`, a nazwa repozytorium/katalogu to `SensorHub`, co może psuć onboarding na systemach case-sensitive.

**Zakres zadania:**
- Zmienić `cd sensorhub` na `cd SensorHub` w sekcji instalacji.

**Kryteria akceptacji:**
- Użytkownik może skopiować komendy 1:1 z README i wejść do katalogu projektu bez ręcznej poprawki.

---

## 2) Zadanie: usunąć błąd logiczny w walidacji czasu
**Problem:** `ValidationUtils.isRecent(timestamp, withinMs)` zwraca `true` również dla timestampów z przyszłości, bo warunek sprawdza tylko `now - timestamp <= withinMs`.

**Zakres zadania:**
- Zmienić logikę na sprawdzanie dodatniego przedziału czasu, np. `delta in 0..withinMs`.

**Kryteria akceptacji:**
- Przyszły timestamp => `false`.
- Timestamp starszy niż okno => `false`.
- Timestamp z przeszłości w oknie => `true`.

---

## 3) Zadanie: skorygować rozbieżność dokumentacji względem kodu
**Problem:** `DOCUMENTATION.md` wskazuje osobny plik `SensorReading.kt`, ale encja `SensorReading` jest zdefiniowana w `SensorData.kt`.

**Zakres zadania:**
- Uaktualnić sekcję „Data Module / Key Classes” tak, by odpowiadała aktualnemu układowi plików.

**Kryteria akceptacji:**
- Opis klas i plików w dokumentacji pokrywa się ze stanem repozytorium.

---

## 4) Zadanie: ulepszyć test jednostkowy, który nie wnosi wartości
**Problem:** `ExampleUnitTest` sprawdza wyłącznie `2 + 2 = 4`, przez co nie testuje logiki aplikacji.

**Zakres zadania:**
- Zastąpić lub rozszerzyć test o przypadki dla `ValidationUtils.isRecent` (minimum: timestamp z przyszłości, z przeszłości w oknie, z przeszłości poza oknem).
- Zapewnić deterministykę testów czasu (np. zegar wstrzykiwany, helper czasu, albo kontrola delta).

**Kryteria akceptacji:**
- Co najmniej 3 asercje dla scenariuszy granicznych i negatywnych związanych z walidacją czasu.
- Test przykładowy `2+2` usunięty lub zdegradowany do roli pomocniczej.
