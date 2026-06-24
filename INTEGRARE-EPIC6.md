# Epic 6 — Conținut online / HTTP — pachet de integrare

## Ce e nou (copiezi direct peste structura proiectului)

```
app/src/main/java/com/mindandmotion/app/data/quotes/
├── QuoteDto.kt              MM-60 — DTO-uri (QuoteDto, QuotesResponseDto)
├── QuotesApiService.kt      MM-60 — interfața Retrofit, 2 endpointuri
├── QuotesApi.kt             MM-60 — construcția Retrofit + OkHttp (singleton lazy)
└── QuotesRepository.kt      MM-61 — Result<T>, fără excepții brute spre ViewModel

app/src/main/java/com/mindandmotion/app/ui/inspiration/
├── QuotesViewModel.kt       MM-61 — StateFlow<QuotesUiState> (loading/error/data)
└── InspirationScreen.kt     MM-62 — listă scrollabilă + citat featured + stări UI
```

## API folosit

**DummyJSON** (`https://dummyjson.com/quotes`) — gratuit, fără cheie/autentificare,
HTTPS. Cele 2 endpointuri cerute de MM-60:
- `GET quotes/random` → un citat → folosit ca "Citatul zilei", afișat primul în listă.
- `GET quotes?limit=&skip=` → listă paginată → restul listei scrollabile. La fiecare
  "Reîncarcă", se trimite un `skip` aleator (0–200) ca să nu vezi mereu aceeași
  primă pagină — altfel butonul de refresh n-ar avea niciun efect vizibil.

## Decizii deliberate (de discutat cu colegul, nu sunt greșeli)

- **Gson, nu kotlinx.serialization** — zero plugin Gradle nou, doar 3 dependențe.
  Mai simplu de explicat/depanat pentru un proiect de echipă de 2 persoane.
- **`Result<T>` în Repository**, nu excepții care ajung goale în ViewModel — așa
  `QuotesViewModel` poate face `.onSuccess { }.onFailure { }` fără try/catch.
- **Cache Room — omis intenționat** (ticketul îl marca "opțional"). Dacă vreți
  offline-first mai târziu, e un ticket separat curat: o tabelă `cached_quotes`,
  repository scrie în ea după fiecare fetch reușit, citește din ea dacă rețeaua pică.
- **Mesaj de eroare generic** în UI ("Nu am putut încărca citatele..."), nu
  `exception.message` brut — evită să arăți utilizatorului texte tehnice gen
  "Unable to resolve host".

## Diff pentru fișierele existente

Atașat `integrare.diff` — aplici cu:
```bash
git apply integrare.diff
```
din rădăcina proiectului (după ce ai copiat fișierele noi de mai sus). Dacă
`git apply` se plânge de conflicte (puțin probabil, dar posibil dacă ați mai
modificat manual vreunul din fișierele de mai jos), aplică manual modificările —
sunt mici și punctuale:

1. **`gradle/libs.versions.toml`** — 2 versiuni noi (`retrofit`, `okhttp`) + 3
   librării noi (`retrofit-core`, `retrofit-gson`, `okhttp-logging`).
2. **`app/build.gradle.kts`** — 3 linii noi în `dependencies { }`.
3. **`app/src/main/AndroidManifest.xml`** — `<uses-permission android:name="android.permission.INTERNET" />`.
4. **`app/src/main/res/values/strings.xml`** — `nav_inspiration`.
5. **`app/src/main/java/.../ui/navigation/Destinations.kt`** — un nou caz
   `INSPIRATION` în enum-ul `TopLevelDestination`. **`BottomBar.kt` nu se
   modifică** — iterează deja generic peste toate destinațiile.
6. **`app/src/main/java/.../di/AppContainer.kt`** — `val quotesRepository: QuotesRepository by lazy { QuotesRepository(QuotesApi.service) }`.
7. **`app/src/main/java/.../ui/navigation/AppNavHost.kt`** — instanțiere
   `quotesViewModel` + ruta `composable(TopLevelDestination.INSPIRATION.route) { InspirationScreen(...) }`.

## De testat după ce aplici

```bash
git checkout -b feat/net-setup   # sau direct pe o singura ramura pentru tot epicul
./gradlew.bat :app:assembleDebug
```
Pe emulator (**ai nevoie de internet activ pe emulator**):
- Tab nou "Inspiration" apare în BottomBar, între Pomodoro și Settings.
- La intrare: spinner → apoi un card "Citatul zilei" + listă de 20 de citate.
- Tragi de "Reîncarcă" din header → alt set de citate (skip aleator).
- Test de eroare: dezactivează WiFi/date pe emulator → Reîncarcă → vezi mesajul
  de eroare + buton "Încearcă din nou" (nu un crash, nu un ecran alb).
