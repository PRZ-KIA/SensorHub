# Code Review Report

## Zakres
Przegląd statyczny kodu aplikacji Android (`app/src/main`) pod kątem błędów logicznych, bezpieczeństwa i zgodności z wytycznymi Android.

## Najważniejsze ustalenia

### 1) Zbyt restrykcyjne sprawdzanie uprawnień lokalizacji (High)
- `hasLocationPermission()` wymaga **jednocześnie** `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` i (na Android Q+) `ACCESS_BACKGROUND_LOCATION`, bo bazuje na `locationPermissions` i `hasPermissions(...)`.
- W praktyce większość funkcji lokalizacyjnych w foreground wymaga tylko `FINE` lub `COARSE`; wymuszanie od razu background location może blokować podstawowe funkcje i pogarszać UX.
- Dodatkowo background location powinno być proszone etapowo (po uzasadnieniu i po przyznaniu foreground), zgodnie z dobrymi praktykami Android.

**Lokalizacja:** `app/src/main/java/com/example/sensorhub/utils/PermissionManager.kt`

### 2) Przestarzały zestaw uprawnień storage dla API 29–32 (Medium)
- Dla API < 33 kod zawsze żąda `READ_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE`.
- Na Android 10+ model pamięci masowej jest ograniczony (scoped storage), a `WRITE_EXTERNAL_STORAGE` jest przestarzałe i często ignorowane.
- Może to prowadzić do mylących promptów i niepotrzebnych odmów.

**Lokalizacja:** `app/src/main/java/com/example/sensorhub/utils/PermissionManager.kt`

### 3) Szeroki zakres FileProvider (`path="."`) dla katalogów wewnętrznych i cache (Medium)
- W `file_paths.xml` zdefiniowano `files-path` i `cache-path` z `path="."`, co obejmuje cały katalog.
- Jeśli URI zostanie nieprawidłowo udostępniony (np. z broad grant), zwiększa to ryzyko niezamierzonej ekspozycji plików.
- Bezpieczniej ograniczyć ekspozycję do konkretnych podkatalogów (np. `exports/`, `share/`).

**Lokalizacja:** `app/src/main/res/xml/file_paths.xml`

## Rekomendowane działania
1. Rozdzielić uprawnienia lokalizacji na foreground i background:
   - `hasForegroundLocationPermission()` (FINE/COARSE),
   - osobna ścieżka i moment na `ACCESS_BACKGROUND_LOCATION`.
2. Zaktualizować strategię storage per API level (scoped storage/MediaStore/SAF), usuwając bezwarunkowe żądanie `WRITE_EXTERNAL_STORAGE`.
3. Ograniczyć `FileProvider` do niezbędnych podkatalogów zamiast `path="."`.

## Uwagi o walidacji
- Próba uruchomienia `./gradlew test` zakończyła się błędem rozwiązywania pluginu Gradle (`com.android.application:8.3.2`) w tym środowisku, dlatego review wykonano statycznie.
