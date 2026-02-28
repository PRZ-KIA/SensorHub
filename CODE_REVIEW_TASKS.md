# Proponowane zadania po przeglądzie kodu

## 1) Zadanie: poprawić literówkę/pozostałość w przestrzeni nazw testów (`com.kia` -> `com.example`)
**Problem:** W repo nadal są pliki testowe i przykładowy kod w pakiecie `com.kia.sensorhub`, podczas gdy główna aplikacja działa na `com.example.sensorhub`. To wygląda na historyczną literówkę lub nie dokończony rebranding namespace i utrudnia onboarding oraz nawigację po kodzie.

**Zakres zadania:**
- Ujednolicić pakiety testów/przykładów do `com.example.sensorhub`.
- Przenieść pliki do zgodnych ścieżek katalogów i poprawić importy.
- Usunąć martwe duplikaty „template code”, jeśli nie są już używane.

**Kryteria akceptacji:**
- W `app/src/test` i `app/src/androidTest` nie ma już pakietu `com.kia.sensorhub`.
- Struktura katalogów odpowiada deklaracjom `package`.

---

## 2) Zadanie: usunąć błąd rejestracji launcherów uprawnień w runtime
**Problem:** `PermissionManager` wywołuje `registerForActivityResult(...)` wewnątrz metod `requestPermission`/`requestPermissions`. Rejestracja launcherów powinna odbywać się raz (np. podczas inicjalizacji), a nie przy każdym wywołaniu żądania; w przeciwnym razie łatwo o błąd lifecycle i niestabilne zachowanie.

**Zakres zadania:**
- Przenieść rejestrację launcherów do pól klasy (jednorazowo).
- Rozdzielić „rejestrację” od „launch”, tak aby metody request tylko uruchamiały istniejący launcher.
- Dodać testy jednostkowe/instrumentacyjne weryfikujące poprawny przepływ callbacków.

**Kryteria akceptacji:**
- Brak dynamicznej rejestracji launcherów przy każdym kliknięciu/akcji użytkownika.
- Scenariusze grant/deny działają stabilnie po wielokrotnych wywołaniach.

---

## 3) Zadanie: skorygować rozbieżność dokumentacji statusowej względem faktycznej bazy kodu
**Problem:** Pliki podsumowujące (`PROJECT_SUMMARY.md`, `MODULE_COMPLETION_SUMMARY.md`) podają historyczne statystyki liczby plików/linijek i „100% completion”, które nie odzwierciedlają obecnego stanu repozytorium (liczba plików Kotlin jest dziś wyraźnie większa).

**Zakres zadania:**
- Zaktualizować sekcje metryk i statusów do stanu bieżącego.
- Oznaczyć, które liczby są automatycznie liczone, a które szacunkowe.
- Dodać datę ostatniej aktualizacji i krótką metodologię liczenia.

**Kryteria akceptacji:**
- Liczby i statusy w dokumentacji zgadzają się z aktualnym drzewem projektu.
- Czytelnik nie dostaje sprzecznych informacji o „ukończeniu projektu”.

---

## 4) Zadanie: ulepszyć test `isRecent` pod kątem deterministyczności i granic
**Problem:** Testy `ValidationUtils.isRecent` bazują na `System.currentTimeMillis()`, co może powodować flaki na granicach czasowych i utrudnia precyzyjną walidację przypadków brzegowych.

**Zakres zadania:**
- Wstrzyknąć źródło czasu (np. `Clock`/provider) do logiki walidacji czasu.
- Przepisać testy tak, aby używały stałego czasu referencyjnego.
- Dodać przypadki graniczne (`delta == 0`, `delta == withinMs`, `withinMs < 0`).

**Kryteria akceptacji:**
- Testy nie zależą od rzeczywistego zegara systemowego.
- Pokryte są scenariusze pozytywne, negatywne i graniczne.
