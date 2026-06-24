# Mind & Motion — Acoperirea baremului (Final project, țintă 10/10)

Document de referință care mapează **baremul oficial** primit pe ce avem deja în proiect și
ce mai lipsește. Pe scurt: aplicația a fost gândită „offline, fără cont, fără cloud", dar
baremul cere **autentificare** și **cereri HTTP** — două module care lipsesc complet și fără
care plafonul e ~6/10. Astea sunt prioritatea zero pentru nota 10.

## Baremul oficial

| Cerință | Punctaj | Stare | Unde / ce lipsește |
|---|---|---|---|
| Acces GIT | gate | ✅ | Repo GitHub, ambii membri commit-uiesc de pe contul lor |
| Activity, Screens & Navigation Component | 1.5p | ✅ | Single-Activity (`MainActivity`), ecrane Compose, `AppNavHost` + `BottomBar` (Navigation Compose). Rămâne doar cablarea ecranelor colegului în nav (MM-43) |
| **Autentificare (Login + Register)** | **2p** | ✅ | Epic 5 (MM-50..53): Room `users` (parolă SHA-256 + salt), `AuthRepository`, `AuthViewModel`, `LoginScreen`/`RegisterScreen`, sesiune în DataStore, gating în nav + Logout în Settings |
| Bază de date locală + listă scrollabilă | 2p | ✅ | Room (`tasks`, `journal_entries`); `TaskListScreen` = `LazyColumn` scrollabilă |
| Date în SharedPreferences/DataStore | 1p | ✅ | `Prefs` (DataStore): durate Pomodoro + temă |
| **Cereri HTTP (min. 2) + deserializare JSON în listă/DB** | **2p** | ❌ | **LIPSEȘTE complet** — aplicația nu are deloc networking |
| Aplicație user-friendly (arată bine, nu se suprapune, merge) | 1.5p | 🟡 | Material 3, arată ok; de verificat pe device că nimic nu se suprapune și toate ecranele merg |
| NO CRASH | -1p | 🟡 | De asigurat stabilitatea (vezi checklist mai jos) |

**Total acoperit acum: ~8p (Auth livrat în Epic 5). Mai lipsește 2p (HTTP — Epic 6, `[COLEG]`).**

## ❌ Gol 1 — Autentificare (Login + Register) — 2p

Nu există nimic. Trebuie un flux complet: ecran de **Register**, ecran de **Login**, sesiune
persistentă și „poartă" în navigație (nelogat → Login; logat → aplicația cu bottom bar).

**Abordare recomandată (locală, fără backend — fiabilă pentru demo, zero crash):**
- Tabel Room `users` (email unic + parolă **hash-uită**, ex. SHA-256 cu `MessageDigest`).
- `AuthRepository`: `register(email, parolă)`, `login(email, parolă)`, `logout()`.
- Sesiunea (id/email user logat) în **DataStore**, ca să rămână logat după restart.
- `AuthViewModel` cu `StateFlow` (stări: loading / eroare / succes).
- `LoginScreen` + `RegisterScreen` (folosesc componentele comune existente).
- Gating în `AppNavHost`: dacă nu e sesiune → graful de auth; altfel → aplicația.
- Buton **Logout** în Settings.

> Alternativă (dacă profesorul cere auth „real" / remote): Register/Login prin `reqres.in`
> sau `dummyjson.com/auth/login` — bonus, ar bifa și o parte din cerința HTTP. Dezavantaj:
> dependență de rețea la login → risc de crash dacă pică net-ul. **Întreabă profesorul dacă
> auth local e acceptat.** Implicit mergem pe local.

## ❌ Gol 2 — Cereri HTTP (min. 2) + JSON în listă — 2p

Aplicația e 100% offline. Trebuie minim **2 request-uri HTTP** care deserializează JSON și
**afișează datele într-o listă scrollabilă** (sau le salvează în Room).

**Abordare recomandată:**
- `Retrofit` + un convertor (`kotlinx-serialization` sau Moshi/Gson) + `OkHttp` (logging).
- Permisiunea `android.permission.INTERNET` în manifest.
- API public **fără cheie**, tematic potrivit pentru o app de productivitate/wellness:
  citate motivaționale. Ex. `https://dummyjson.com/quotes` (listă) +
  `https://dummyjson.com/quotes/random` (al 2-lea request). Fallback: `jsonplaceholder.typicode.com`.
- Ecran nou **„Inspirație"** (al 5-lea tab în bottom bar): listă scrollabilă de citate
  preluate online, cu stări **loading / empty / eroare** (esențial pentru NO CRASH).
- Opțional: cache-uiește citatele în Room (bifează și varianta „store into local DB").

Cele 2 request-uri = două endpointuri distincte (listă + citatul zilei), deci cerința „min. 2"
e acoperită clar.

## ⚠️ NO CRASH (-1p) — checklist de stabilitate

- Toate apelurile de rețea în `try/catch` → stare de eroare în UI, niciodată excepție netratată.
- Permisiuni cerute corect la runtime (POST_NOTIFICATIONS deja; INTERNET e normal).
- Stări goale prietenoase peste tot (deja avem `EmptyState`).
- Validare pe formularele de auth (email/parolă goale, parole care nu coincid la register).
- Smoke test pe device pentru fiecare ecran (parte din MM-43).

## Tichete noi propuse (de adăugat în TASKS.md)

**Epic 5 — Autentificare** *(propus `[TU]` — atinge nav + fundație)*

| ID | Titlu | Depinde de | Branch |
|---|---|---|---|
| MM-50 | `UserEntity` + `UserDao` + `AuthRepository` (Room, hash parolă) | MM-02 | `feat/auth-data` |
| MM-51 | `AuthViewModel` + sesiune în DataStore (login/register/logout) | MM-50 | `feat/auth-vm` |
| MM-52 | `LoginScreen` + `RegisterScreen` | MM-51, MM-05 | `feat/auth-ui` |
| MM-53 | Gating navigație (nelogat→Login) + Logout în Settings | MM-52, MM-03 | `feat/auth-nav` |

**Epic 6 — Conținut online / HTTP** *(propus `[COLEG]` — felie verticală nouă curată)*

| ID | Titlu | Depinde de | Branch |
|---|---|---|---|
| MM-60 | Retrofit + serializare + `ApiService` + DTO-uri (2 endpointuri) + permisiune INTERNET | MM-01 | `feat/net-setup` |
| MM-61 | `QuotesRepository` + `QuotesViewModel` (StateFlow loading/error/data, cache opțional Room) | MM-60 | `feat/net-vm` |
| MM-62 | `InspirationScreen` (listă scrollabilă, stări loading/empty/error) + tab nou în `BottomBar` | MM-61, MM-05 | `feat/net-ui` |

> Împărțirea `[TU]`/`[COLEG]` de mai sus e o propunere ca să păstrăm ~60/40 și felii verticale
> separate (conflicte minime pe Git). De discutat între voi.

## Riscuri / note

- Astea sunt **2 epicuri noi (4p)** — extind sensibil scope-ul față de planul inițial.
  Prioritizați-le înaintea finisajelor, pentru că fac diferența 6 → 10.
- Restul cerințelor sunt deja acoperite; nu rescrieți ce există.
- Confirmați cu profesorul: (a) dacă auth **local** e acceptat sau trebuie remote;
  (b) dacă cele 2 request-uri trebuie să fie GET-uri diferite sau e ok orice 2 apeluri.
