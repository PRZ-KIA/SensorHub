# Proponowane zadania po przeglądzie kodu

## 1) Literówka / błąd nazewnictwa w instrukcji instalacji
**Problem:** W `README.md` komenda przejścia do katalogu używa nazwy `sensorhub`, podczas gdy nazwa repozytorium i katalogu projektu to `SensorHub` (różnica wielkości liter jest istotna np. na Linuksie).

**Zadanie:**
- Poprawić instrukcję instalacji w `README.md`:
  - z `cd sensorhub`
  - na `cd SensorHub`.

**Kryteria akceptacji:**
- Instrukcja clone + `cd` działa 1:1 po skopiowaniu komend na systemie z case-sensitive filesystem.

---

## 2) Usunięcie błędu logicznego w walidacji czasu
**Problem:** `ValidationUtils.isRecent(timestamp, withinMs)` uznaje przyszłe znaczniki czasu za „recent”, bo sprawdza tylko `now - timestamp <= withinMs`.

**Zadanie:**
- Zmienić implementację tak, aby `isRecent` zwracało `true` tylko dla timestampów z przeszłości i tylko w zadanym oknie czasu.
- Przykładowo: obliczyć `delta = now - timestamp` i wymagać `delta in 0..withinMs`.

**Kryteria akceptacji:**
- Przyszły timestamp zwraca `false`.
- Timestamp starszy niż `withinMs` zwraca `false`.
- Timestamp z przeszłości mieszczący się w oknie zwraca `true`.

---

## 3) Korekta rozbieżności dokumentacji względem kodu
**Problem:** `DOCUMENTATION.md` wymienia plik `SensorReading.kt`, ale encja `SensorReading` jest zadeklarowana w `SensorData.kt`.

**Zadanie:**
- Zaktualizować sekcję „Key Classes” w `DOCUMENTATION.md`, aby odzwierciedlała rzeczywisty układ:
  - `SensorData.kt` zawiera zarówno modele sensorów, jak i encję `SensorReading`.
- (Opcjonalnie) dodać notkę, że wydzielenie `SensorReading` do osobnego pliku może być przyszłym refaktorem.

**Kryteria akceptacji:**
- Lista plików i opisów w dokumentacji zgadza się z aktualną strukturą kodu.

---

## 4) Ulepszenie testów jednostkowych
**Problem:** `ExampleUnitTest` testuje wyłącznie `2 + 2 = 4`, co nie pokrywa logiki biznesowej aplikacji.

**Zadanie:**
- Zastąpić/rozszerzyć test przykładowy o testy realnych reguł, np. dla `ValidationUtils.isRecent` (scenariusze: przeszłość w oknie, przeszłość poza oknem, przyszłość).
- Upewnić się, że testy są deterministyczne (np. kontrola czasu przez wstrzykiwany zegar albo tolerancję czasową).

**Kryteria akceptacji:**
- Co najmniej 3 testy pokrywające granice i przypadki błędne dla logiki czasu.
- Usunięcie lub zdegradowanie znaczenia testu „2+2”.
